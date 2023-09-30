package nl.utwente.di.visol1.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.utwente.di.visol1.models.Schedule;

public class ScheduleDao extends GenericDao {
	public static Schedule getScheduleByVessel(int vessel) {
		try (Query query = Query.prepared("SELECT * FROM schedule WHERE vessel = ?", stmt -> stmt.setInt(1, vessel))) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return new Schedule(
				rs.getInt("vessel"),
				rs.getInt("berth"),
				rs.getBoolean("manual"),
				rs.getTimestamp("start"),
				rs.getTimestamp("expected_end")
			);
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static List<Schedule> getSchedulesByBerth(int berthId, Timestamp from, Timestamp to) {
		List<Schedule> result = new ArrayList<>();
		try (Query query = Query.prepared("SELECT * FROM schedule WHERE berth = ? AND start >= ? AND start <= ?", stmt -> {
			stmt.setInt(1, berthId);
			stmt.setTimestamp(2, from);
			stmt.setTimestamp(3, to);
		})) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				result.add(new Schedule(
					rs.getInt("vessel"),
					rs.getInt("berth"),
					rs.getBoolean("manual"),
					rs.getTimestamp("start"),
					rs.getTimestamp("expected_end")
				));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return result;
	}

	public static Map<Integer, Map<Integer, List<Schedule>>> getSchedulesByPort(int portId, Timestamp from, Timestamp to) {
		Map<Integer, Map<Integer, List<Schedule>>> result = new HashMap<>();
		try (Query query = Query.prepared(
			"SELECT s.*, b.terminal "
			+ "FROM schedule s, berth b, terminal t " +
			"WHERE s.berth = b.id "
			+ "AND b.terminal = t.id "
			+ "AND t.port = ? "
			+ "AND ((s.start >= ? AND s.start <= ?) OR (s.expected_end >= ? AND s.expected_end <= ?))",
			stmt -> {
				stmt.setInt(1, portId);
				stmt.setTimestamp(2, from);
				stmt.setTimestamp(3, to);
				stmt.setTimestamp(4, from);
				stmt.setTimestamp(5, to);
			}
		)) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				result.computeIfAbsent(rs.getInt("terminal"), k -> new HashMap<>())
					.computeIfAbsent(rs.getInt("berth"), k -> new ArrayList<>())
					.add(new Schedule(
						rs.getInt("vessel"),
						rs.getInt("berth"),
						rs.getBoolean("manual"),
						rs.getTimestamp("start"),
						rs.getTimestamp("expected_end")
					));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return result;
	}

	public static Map<Integer, List<Schedule>> getSchedulesByTerminal(int terminalId, Timestamp from, Timestamp to) {
		Map<Integer, List<Schedule>> result = new HashMap<>();
		try (Query query = Query.prepared(
			"SELECT s.* FROM schedule s, berth b " +
			"WHERE s.berth = b.id AND b.terminal = ? " +
			"AND ((s.start >= ? AND s.start <= ?) OR (s.expected_end >= ? AND s.expected_end <= ?))", stmt -> {
				stmt.setInt(1, terminalId);
				stmt.setTimestamp(2, from);
				stmt.setTimestamp(3, to);
				stmt.setTimestamp(4, from);
				stmt.setTimestamp(5, to);
			}
		)) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				result.computeIfAbsent(rs.getInt("berth"), k -> new ArrayList<>()).add(new Schedule(
					rs.getInt("vessel"),
					rs.getInt("berth"),
					rs.getBoolean("manual"),
					rs.getTimestamp("start"),
					rs.getTimestamp("expected_end")
				));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return result;
	}


	public static int deleteScheduleByVessel(int vesselId) {
		try (Update update = Update.prepared("DELETE FROM schedule WHERE vessel = ?", stmt -> stmt.setInt(1, vesselId))) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	public static Schedule replaceSchedule(int vesselId, Schedule schedule) {
		if (schedule == null) return null;
		try (Query query = Query.prepared(
			"INSERT INTO schedule (vessel, berth, manual, start) VALUES (?, ?, ?, ?) " +
			"ON CONFLICT (vessel) DO UPDATE SET berth = ?, manual = ?, start = ? RETURNING *",
			stmt -> {
				stmt.setInt(1, vesselId);
				stmt.setInt(2, schedule.getBerth());
				stmt.setBoolean(3, schedule.isManual());
				stmt.setTimestamp(4, schedule.getStart());
				stmt.setInt(5, schedule.getBerth());
				stmt.setBoolean(6, schedule.isManual());
				stmt.setTimestamp(7, schedule.getStart());
			}
		)) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return new Schedule(
				rs.getInt("vessel"),
				rs.getInt("berth"),
				rs.getBoolean("manual"),
				rs.getTimestamp("start"),
				rs.getTimestamp("expected_end")
			);
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

}
