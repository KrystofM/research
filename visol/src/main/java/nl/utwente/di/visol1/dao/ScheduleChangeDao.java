package nl.utwente.di.visol1.dao;

import javax.swing.event.ChangeEvent;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.utwente.di.visol1.models.Change;
import nl.utwente.di.visol1.models.Schedule;
import nl.utwente.di.visol1.models.ScheduleChange;
import nl.utwente.di.visol1.models.Vessel;
import nl.utwente.di.visol1.models.VesselChange;

public class ScheduleChangeDao extends GenericDao {
	public static ScheduleChange createScheduleChange(ScheduleChange scheduleChange) {
		try (Query query = Query.prepared(
			"INSERT INTO schedulechange (author, reason, vessel, old, new, undo) VALUES(?, ?, ?, ?::jsonb, ?::jsonb, ?) RETURNING *;",
			stmt -> {
				stmt.setString(1, scheduleChange.getAuthor());
				stmt.setString(2, scheduleChange.getReason());
				stmt.setInt(3, scheduleChange.getVessel());
				stmt.setString(4, Schedule.toString(scheduleChange.getOldSchedule()));
				stmt.setString(5, Schedule.toString(scheduleChange.getNewSchedule()));
				stmt.setBoolean(6, scheduleChange.isUndo());
			}
		)) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return fromResultSet(rs);
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static List<ScheduleChange> getScheduleChangesByVessel(int vesselId, Timestamp from, Timestamp to) {
		List<ScheduleChange> result = new ArrayList<>();
		try (Query query = Query.prepared("SELECT * FROM schedulechange WHERE vessel = ? AND tsrange(?, ?, '[]') @> date", stmt -> {
			stmt.setInt(1, vesselId);
			stmt.setTimestamp(2, from);
			stmt.setTimestamp(3, to);
		})) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				result.add(fromResultSet(rs));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return result;
	}

	public static ScheduleChange getScheduleChangeByDate(int vesselId, Timestamp date) {
		try (Query query = Query.prepared("SELECT * FROM schedulechange WHERE vessel = ? AND date = ?", stmt -> {
			stmt.setInt(1, vesselId);
			stmt.setTimestamp(2, date);
		})) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return fromResultSet(rs);
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static Map<Integer, List<ScheduleChange>> getScheduleChanges(Timestamp from, Timestamp to) {
		Map<Integer, List<ScheduleChange>> result = new HashMap<>();
		try (Query query = Query.prepared("SELECT * FROM schedulechange WHERE tsrange(?, ?, '[]') @> date", stmt -> {
			stmt.setTimestamp(1, from);
			stmt.setTimestamp(2, to);
		})) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				result.computeIfAbsent(rs.getInt("vessel"), k -> new ArrayList<>()).add(fromResultSet(rs));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return result;
	}

	public static int deleteScheduleChanges(int vesselId, Timestamp from, Timestamp to) {
		try (Update update = Update.prepared("DELETE FROM schedulechange WHERE vessel = ? AND tsrange(?, ?, '[]') @> date", stmt -> {
			stmt.setInt(1, vesselId);
			stmt.setTimestamp(2, from);
			stmt.setTimestamp(3, to);
		})) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	public static int deleteScheduleChangeByDate(int vesselId, Timestamp date) {
		try (Update update = Update.prepared("DELETE FROM schedulechange WHERE vessel = ? AND date = ?", stmt -> {
			stmt.setInt(1, vesselId);
			stmt.setTimestamp(2, date);
		})) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	public static int deleteScheduleChangeByAuthor(String employeeEmail, Timestamp date) {
		try (Update update = Update.prepared("DELETE FROM schedulechange WHERE author = ? AND date = ?", stmt -> {
			stmt.setString(1, employeeEmail);
			stmt.setTimestamp(2, date);
		})) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	private static ScheduleChange fromResultSet(ResultSet rs) throws SQLException {
		return new ScheduleChange(
			rs.getLong("id"),
			rs.getString("author"),
			rs.getTimestamp("date"),
			rs.getString("reason"),
			rs.getInt("vessel"),
			rs.getString("old"),
			rs.getString("new"),
			rs.getBoolean("undo")
		);
	}

}
