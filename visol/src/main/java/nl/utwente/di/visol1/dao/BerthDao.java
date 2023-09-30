package nl.utwente.di.visol1.dao;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.utwente.di.visol1.models.Berth;

public class BerthDao extends GenericDao {
	public static Berth getBerth(int berth) {
		try (Query query = Query.prepared("SELECT * FROM berth WHERE id = ?", stmt -> stmt.setInt(1, berth))) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return new Berth(
				rs.getInt("id"),
				rs.getInt("terminal"),
				rs.getTime("open"),
				rs.getTime("close"),
				rs.getDouble("unload_speed"),
				rs.getInt("length"),
				rs.getInt("width"),
				rs.getInt("depth")
			);
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static Map<Integer, Berth> getBerthsInArray(List<Integer> berthIds) {
		Map<Integer, Berth> berthMap = new HashMap<>();
		Array berths;
		try {
			berths = getConnection().createArrayOf("int", berthIds.toArray());
		} catch (SQLException e) {
			return null;
		}
		try (
			Query query = Query.prepared("SELECT * FROM berth WHERE id = ANY(?)", stmt -> {
				stmt.setArray(1, berths);
			})) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				berthMap.put(rs.getInt("id"), new Berth(
					rs.getInt("id"),
					rs.getInt("terminal"),
					rs.getTime("open"),
					rs.getTime("close"),
					rs.getDouble("unload_speed"),
					rs.getInt("length"),
					rs.getInt("width"),
					rs.getInt("depth"))
				);
			}
			return berthMap;
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}


	public static Map<Integer, Berth> getBerthsByTerminal(int terminalId) {
		Map<Integer, Berth> results = new HashMap<>();
		try (Query query = Query.prepared("SELECT * FROM berth WHERE terminal = ?", stmt -> stmt.setInt(1, terminalId))) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				results.put(
					rs.getInt("id"),
					new Berth(
						rs.getInt("id"),
						rs.getInt("terminal"),
						rs.getTime("open"),
						rs.getTime("close"),
						rs.getDouble("unload_speed"),
						rs.getInt("length"),
						rs.getInt("width"),
						rs.getInt("depth")
					)
				);
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return results;
	}


	public static int deleteBerth(int berthId) {
		try (Update update = Update.prepared("DELETE FROM berth WHERE id = ?", stmt -> stmt.setInt(1, berthId))) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	public static Berth createBerth(Berth berth) {
		if (berth == null) return null;
		try (Query query = Query.prepared(
			"INSERT INTO berth (terminal, open, close, unload_speed, length, width, depth) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING *",
			stmt -> {
				stmt.setInt(1, berth.getTerminalId());
				stmt.setTime(2, berth.getOpen());
				stmt.setTime(3, berth.getClose());
				stmt.setDouble(4, berth.getUnloadSpeed());
				stmt.setInt(5, berth.getLength());
				stmt.setInt(6, berth.getWidth());
				stmt.setInt(7, berth.getDepth());
			}
		)) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return new Berth(
				rs.getInt("id"),
				rs.getInt("terminal"),
				rs.getTime("open"),
				rs.getTime("close"),
				rs.getDouble("unload_speed"),
				rs.getInt("length"),
				rs.getInt("width"),
				rs.getInt("depth"));
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static int replaceBerth(int berthId, Berth berth) {
		if (berth == null) return -1;
		try (Update update = Update.prepared(
			"UPDATE berth SET terminal = ?, open = ?, close = ?, unload_speed = ?, length = ?, width = ?, depth = ? WHERE id = ?",
			stmt -> {
				stmt.setInt(1, berth.getTerminalId());
				stmt.setTime(2, berth.getOpen());
				stmt.setTime(3, berth.getClose());
				stmt.setDouble(4, berth.getUnloadSpeed());
				stmt.setInt(5, berth.getLength());
				stmt.setInt(6, berth.getWidth());
				stmt.setInt(7, berth.getDepth());
				stmt.setInt(8, berthId);
			}
		)) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}
}
