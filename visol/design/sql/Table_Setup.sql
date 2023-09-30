--- DELETE ---

DROP TYPE IF EXISTS role CASCADE;

DROP TABLE IF EXISTS port CASCADE;
DROP TABLE IF EXISTS terminal CASCADE;
DROP TABLE IF EXISTS berth CASCADE;
DROP TABLE IF EXISTS vessel CASCADE;
DROP TABLE IF EXISTS schedule CASCADE;
DROP TABLE IF EXISTS employee CASCADE;
DROP TABLE IF EXISTS schedulechange CASCADE;
DROP TABLE IF EXISTS vesselchange CASCADE;

DROP FUNCTION IF EXISTS calculate_end_time CASCADE;
DROP FUNCTION IF EXISTS dump_port CASCADE;

--- CREATE ---

CREATE TYPE role AS enum ('vessel planner', 'terminal manager', 'port authority', 'researcher');

CREATE TABLE port (
	id   serial CONSTRAINT port_pk PRIMARY KEY,
	name varchar(64) NOT NULL
);

CREATE TABLE terminal (
	id   serial CONSTRAINT terminal_pk PRIMARY KEY,
	name varchar(64) NOT NULL,
	port int         NOT NULL,
	CONSTRAINT terminal_port_id_fk FOREIGN KEY (port) REFERENCES port (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE berth (
	id           serial CONSTRAINT berth_pk PRIMARY KEY,
	terminal     int     NOT NULL,
	open         time(0) NOT NULL,
	close        time(0) NOT NULL,
	unload_speed float   NOT NULL,
	length       int     NOT NULL,
	width        int     NOT NULL,
	depth        int     NOT NULL,
	CONSTRAINT berth_terminal_id_fk FOREIGN KEY (terminal) REFERENCES terminal (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT berth_unload_speed_check CHECK (unload_speed > 0),
	CONSTRAINT berth_length_width_depth_check CHECK (length > 0 AND width > 0 AND depth > 0)
);

CREATE TABLE vessel (
	id            serial CONSTRAINT vessel_pk PRIMARY KEY,
	name          varchar(64)  NOT NULL,
	arrival       timestamp(0) NOT NULL,
	deadline      timestamp(0),
	containers    int          NOT NULL,
	cost_per_hour float        NOT NULL,
	destination   int          NOT NULL,
	length        int          NOT NULL,
	width         int          NOT NULL,
	depth         int          NOT NULL,
	CONSTRAINT vessel_terminal_id_fk FOREIGN KEY (destination) REFERENCES terminal (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT vessel_arrival_deadline_check CHECK (arrival < deadline),
	CONSTRAINT vessel_containers_check CHECK (containers > 0),
	CONSTRAINT vessel_length_width_depth_check CHECK (length > 0 AND width > 0 AND depth > 0)
);

CREATE TABLE schedule (
	vessel       int CONSTRAINT schedule_pk PRIMARY KEY,
	berth        int          NOT NULL,
	manual       boolean      NOT NULL,
	start        timestamp(0) NOT NULL,
	expected_end timestamp(0) NOT NULL,
	CONSTRAINT schedule_vessel_id_fk FOREIGN KEY (vessel) REFERENCES vessel (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT schedule_berth_id_fk FOREIGN KEY (berth) REFERENCES berth (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT schedule_start_end_check CHECK (start < expected_end)
);

CREATE TABLE employee (
	email    varchar(64) CONSTRAINT employee_pk PRIMARY KEY,
	key_hash bytea NOT NULL,
	key_salt bytea NOT NULL,
	role     role  NOT NULL,
	terminal int,
	port     int,
	CONSTRAINT employee_terminal_id_fk FOREIGN KEY (terminal) REFERENCES terminal (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT employee_port_id_fk FOREIGN KEY (port) REFERENCES port (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT employee_email_check_valid CHECK (email ~* '^[A-Za-z0-9._+%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'),
	CONSTRAINT employee_password_check_size CHECK (octet_length(key_hash) = 64), -- SHA-512 hash, 512 / 8 = 64
	CONSTRAINT employee_salt_check_size CHECK (octet_length(key_salt) = 32)      -- generated salt, 32 bytes
);

CREATE TABLE schedulechange (
	id     bigserial PRIMARY KEY,
	author varchar(64),
	date   timestamp(0) DEFAULT timezone('UTC'::text, CURRENT_TIMESTAMP(0)),
	reason varchar(255),
	undo   boolean NOT NULL DEFAULT false,
	vessel int,
	old    jsonb,
	new    jsonb,
	CONSTRAINT schedulechange_employee_email_fk FOREIGN KEY (author) REFERENCES employee (email)
		ON UPDATE CASCADE
		ON DELETE SET NULL,
	CONSTRAINT schedulechange_vessel_id_fk FOREIGN KEY (vessel) REFERENCES vessel (id)
		ON UPDATE CASCADE
		ON DELETE SET NULL,
	CONSTRAINT schedulechange_check_valid CHECK(old IS NOT NULL OR new IS NOT NULL)
);

CREATE TABLE vesselchange (
	id     bigserial PRIMARY KEY,
	author varchar(64),
	date   timestamp(0) DEFAULT timezone('UTC'::text, CURRENT_TIMESTAMP(0)),
	reason varchar(255),
	undo   boolean NOT NULL DEFAULT false,
	vessel int,
	old    jsonb,
	new    jsonb,
	CONSTRAINT vesselchange_employee_email_fk FOREIGN KEY (author) REFERENCES employee (email)
		ON UPDATE CASCADE
		ON DELETE SET NULL,
	CONSTRAINT vesselchange_vessel_id_fk FOREIGN KEY (vessel) REFERENCES vessel (id)
		ON UPDATE CASCADE
		ON DELETE SET NULL,
	CONSTRAINT vesselchange_check_valid CHECK(old IS NOT NULL OR new IS NOT NULL)
);

CREATE OR REPLACE FUNCTION calculate_end_time()
	RETURNS trigger
	LANGUAGE plpgsql
AS
$$
DECLARE
	end_time timestamp;
BEGIN
	SELECT to_timestamp(extract(EPOCH FROM new.start) + ((v.containers / b.unload_speed * 3600000)::bigint / 1000) + 60*15)::timestamptz AT TIME ZONE 'UTC'
	INTO end_time
	FROM vessel v,
	     berth b
	WHERE v.id = new.vessel
	  AND new.berth = b.id;
	new.expected_end := end_time;
	RETURN new;
END;
$$;

CREATE TRIGGER expected_end_trigger
	BEFORE INSERT OR UPDATE
	ON schedule
	FOR EACH ROW
EXECUTE PROCEDURE calculate_end_time();


CREATE OR REPLACE FUNCTION dump_port(port_id integer, time_range tsrange)
	RETURNS table (
		json jsonb
	)
AS
$$
BEGIN
	RETURN QUERY
		SELECT jsonb_build_object(
			       'version', 1,
			       'name', port.name,
			       'terminals', terminals.json,
			       'berths', berths.json,
			       'vessels', vessels.json,
			       'schedules', schedules.json
			       ) AS json
		FROM port,
		     (SELECT coalesce(
			             jsonb_object_agg('terminal' || terminal.id, jsonb_build_object(
				             'name', terminal.name,
				             'port', 'port' || terminal.port
				             )), jsonb_build_object()) AS json
		      FROM terminal
		      WHERE terminal.port = port_id) AS terminals,
		     (SELECT coalesce(
			             jsonb_object_agg('berth' || berth.id, jsonb_build_object(
				             'terminal', 'terminal' || berth.terminal,
				             'open', to_char(berth.open, 'HH24:MI:SS'),
				             'close', to_char(berth.close, 'HH24:MI:SS'),
				             'unload_speed', berth.unload_speed,
				             'length', berth.length,
				             'width', berth.width,
				             'depth', berth.depth
				             )), jsonb_build_object()) AS json
		      FROM berth
		      WHERE berth.terminal IN (SELECT id FROM terminal WHERE port = port_id)) AS berths,
		     (SELECT coalesce(
			             jsonb_object_agg('vessel' || vessel.id, jsonb_build_object(
				             'name', vessel.name,
				             'arrival', to_char(vessel.arrival, 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
				             'deadline', to_char(vessel.deadline, 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
				             'containers', vessel.containers,
				             'cost_per_hour', round(vessel.cost_per_hour::numeric, 2),
				             'destination', 'terminal' || vessel.destination,
				             'length', vessel.length,
				             'width', vessel.width,
				             'depth', vessel.depth
				             )), jsonb_build_object()) AS json
		      FROM vessel
		      WHERE vessel.destination IN (SELECT id FROM terminal WHERE port = port_id)
			    AND tsrange(vessel.arrival, vessel.deadline, '[]') && time_range) AS vessels,
		     (SELECT coalesce(
			             jsonb_agg(DISTINCT jsonb_build_object(
				             'vessel', 'vessel' || schedule.vessel,
				             'berth', 'berth' || schedule.berth,
				             'manual', schedule.manual,
				             'start', to_char(schedule.start, 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
				             'expected_end', to_char(schedule.expected_end, 'YYYY-MM-DD"T"HH24:MI:SS"Z"')
				             )), jsonb_build_array()) AS json
		      FROM schedule
		      WHERE schedule.berth IN (SELECT id FROM berth WHERE terminal IN (SELECT id FROM terminal WHERE port = port_id))
			    AND schedule.vessel IN (SELECT id
			                            FROM vessel
			                            WHERE destination IN (SELECT id FROM terminal WHERE port = port_id)
				                          AND tsrange(vessel.arrival, vessel.deadline, '[]') && time_range)
			    AND tsrange(schedule.start, schedule.expected_end, '[]') &> time_range) AS schedules
		WHERE port.id = port_id
		GROUP BY port.id, terminals.json, berths.json, vessels.json, schedules.json;
END;
$$ LANGUAGE plpgsql;
