package nl.utwente.di.visol1.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import nl.utwente.di.visol1.models.Berth;
import nl.utwente.di.visol1.models.Port;
import nl.utwente.di.visol1.models.Schedule;
import nl.utwente.di.visol1.models.Terminal;
import nl.utwente.di.visol1.models.Vessel;
import nl.utwente.di.visol1.type_adapters.TimeAdapter;
import nl.utwente.di.visol1.type_adapters.TimestampAdapter;

public class PortDao extends GenericDao {
	public static Port getPort(int portId) {
		try (Query query = Query.prepared("SELECT * FROM port WHERE id = ?", stmt -> stmt.setInt(1, portId))) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return new Port(
				rs.getInt("id"),
				rs.getString("name")
			);
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static Map<Integer, Port> getPorts() {
		Map<Integer, Port> result = new HashMap<>();
		try (Query query = Query.simple("SELECT * FROM port")) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				result.put(
					rs.getInt("id"),
					new Port(
						rs.getInt("id"),
						rs.getString("name")
					)
				);
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return result;
	}

	public static int replacePort(int portId, Port port) {
		if (port == null) return -1;
		try (Update update = Update.prepared("UPDATE port SET name = ? WHERE id = ?", stmt -> {
			stmt.setString(1, port.getName());
			stmt.setInt(2, portId);
		})) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}


	public static Port createPort(Port port) {
		if (port == null) return null;
		try (Query query = Query.prepared("INSERT INTO port (name) VALUES (?) RETURNING *", stmt -> stmt.setString(1, port.getName()))) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return new Port(
				rs.getInt("id"),
				rs.getString("name")
			);
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static int deletePort(int portId) {
		try (Update update = Update.prepared("DELETE FROM port WHERE id = ?", stmt -> stmt.setInt(1, portId))) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	public static String exportPort(int id, Timestamp fromTime, Timestamp toTime) {
		try (Query query = Query.prepared("SELECT json FROM dump_port(?, tsrange(?, ?, '[]'))", stmt -> {
			stmt.setInt(1, id);
			stmt.setTimestamp(2, fromTime);
			stmt.setTimestamp(3, toTime);
		})) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return rs.getString("json");
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static Port importPort(JsonNode json) {
		if (json.get("version").asInt() == 1) {
			return PortDumpParserV1.parse(json);
		}

		// Unknown version, will result in a 406
		return null;
	}

	private static class PortDumpParserV1 {
		private static final Function<JsonNode, Timestamp> timestampParser = node -> node.isNull() ? null : TimestampAdapter.unadapt(node.asText());
		private static final Function<JsonNode, Time> timeParser = node -> node.isNull() ? null : TimeAdapter.unadapt(node.asText());

		public static Port parse(JsonNode json) {
			// Build port from dump
			Port port = PortDao.createPort(new Port(-1, json.get("name").asText()));

			// Create terminals
			Map<String, Integer> terminalPlaceholders = new HashMap<>();
			json.get("terminals").fields().forEachRemaining(entry -> {
				Terminal terminal = TerminalDao.createTerminal(new Terminal(
					-1, port.getId(), entry.getValue().get("name").asText()
				));
				terminalPlaceholders.put(entry.getKey(), terminal.getId());
			});

			// Create berths
			Map<String, Integer> berthPlaceholders = new HashMap<>();
			json.get("berths").fields().forEachRemaining(entry -> {
				Berth berth = BerthDao.createBerth(new Berth(
					-1, terminalPlaceholders.get(entry.getValue().get("terminal").asText()),
					timeParser.apply(entry.getValue().get("open")),
					timeParser.apply(entry.getValue().get("close")),
					entry.getValue().get("unload_speed").asDouble(),
					entry.getValue().get("length").asInt(),
					entry.getValue().get("width").asInt(),
					entry.getValue().get("depth").asInt()
				));
				berthPlaceholders.put(entry.getKey(), berth.getId());
			});

			// Create vessels
			Map<String, Integer> vesselPlaceholders = new HashMap<>();
			json.get("vessels").fields().forEachRemaining(entry -> {
				Vessel vessel = VesselDao.createVessel(new Vessel(
					-1, entry.getValue().get("name").asText(),
					timestampParser.apply(entry.getValue().get("arrival")),
					timestampParser.apply(entry.getValue().get("deadline")),
					entry.getValue().get("containers").asInt(),
					entry.getValue().get("cost_per_hour").asDouble(),
					terminalPlaceholders.get(entry.getValue().get("destination").asText()),
					entry.getValue().get("length").asInt(),
					entry.getValue().get("width").asInt(),
					entry.getValue().get("depth").asInt()
				));
				vesselPlaceholders.put(entry.getKey(), vessel.getId());
			});

			// Create schedules
			json.get("schedules").elements().forEachRemaining(scheduleNode -> {
				Schedule schedule = ScheduleDao.replaceSchedule(
					vesselPlaceholders.get(scheduleNode.get("vessel").asText()),
					new Schedule(
						vesselPlaceholders.get(scheduleNode.get("vessel").asText()),
						berthPlaceholders.get(scheduleNode.get("berth").asText()),
						scheduleNode.get("manual").asBoolean(),
						timestampParser.apply(scheduleNode.get("start")),
						timestampParser.apply(scheduleNode.get("expected_end"))
					)
				);
			});

			return port;
		}
	}
}
