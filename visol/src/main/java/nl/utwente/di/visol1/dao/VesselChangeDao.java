package nl.utwente.di.visol1.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.utwente.di.visol1.dao.GenericDao.Query;
import nl.utwente.di.visol1.dao.GenericDao.Update;
import nl.utwente.di.visol1.models.Vessel;
import nl.utwente.di.visol1.models.VesselChange;

public class VesselChangeDao {
	public static VesselChange createVesselChange(VesselChange vesselChange) {
		try (Query query = Query.prepared(
			"INSERT INTO vesselchange (author, reason, vessel, old, new, undo) VALUES(?, ?, ?, ?::jsonb, ?::jsonb, ?) RETURNING *;",
			stmt -> {
				stmt.setString(1, vesselChange.getAuthor());
				stmt.setString(2, vesselChange.getReason());
				stmt.setInt(3, vesselChange.getVessel());
				stmt.setString(4, Vessel.toString(vesselChange.getOldVessel()));
				stmt.setString(5, Vessel.toString(vesselChange.getNewVessel()));
				stmt.setBoolean(6, vesselChange.isUndo());
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

	public static List<VesselChange> getVesselChangesByVessel(int vesselId, Timestamp from, Timestamp to) {
		List<VesselChange> result = new ArrayList<>();
		try (Query query = Query.prepared("SELECT * FROM vesselchange WHERE vessel = ? AND tsrange(?, ?, '[]') @> date", stmt -> {
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


	public static Map<Integer, List<VesselChange>> getVesselChanges(Timestamp from, Timestamp to) {
		Map<Integer, List<VesselChange>> result = new HashMap<>();
		try (Query query = Query.prepared("SELECT * FROM vesselchange WHERE tsrange(?, ?, '[]') @> date", stmt -> {
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

	public static VesselChange getVesselChangeByDate(int vesselId, Timestamp date) {
		try (Query query = Query.prepared("SELECT * FROM vesselchange WHERE vessel = ? AND date = ?", stmt -> {
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

	public static int deleteVesselChangeByDate(int vesselId, Timestamp date) {
		try (Update update = Update.prepared("DELETE FROM vesselchange WHERE vessel = ? AND date = ?", stmt -> {
			stmt.setInt(1, vesselId);
			stmt.setTimestamp(2, date);
		})) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	public static int deleteVesselChangeByAuthor(String employeeEmail, Timestamp date){
		try (Update update = Update.prepared("DELETE FROM vesselchange WHERE author = ? AND date = ?", stmt -> {
			stmt.setString(1, employeeEmail);
			stmt.setTimestamp(2, date);
		})) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	public static int deleteVesselChanges(int vesselId, Timestamp from, Timestamp to) {
		try (Update update = Update.prepared("DELETE FROM vesselchange WHERE vessel = ? AND tsrange(?, ?, '[]') @> date", stmt -> {
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

	private static VesselChange fromResultSet(ResultSet rs) throws SQLException {
		return new VesselChange(
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
