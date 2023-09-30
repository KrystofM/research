package nl.utwente.di.visol1.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import nl.utwente.di.visol1.models.Terminal;

public class TerminalDao extends GenericDao {
	public static Terminal getTerminal(int terminalId) {
		try (Query query = Query.prepared("SELECT * FROM terminal WHERE id = ?", stmt -> stmt.setInt(1, terminalId))) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return new Terminal(
				rs.getInt("id"),
				rs.getInt("port"),
				rs.getString("name")
			);
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static int deleteTerminal(int terminalId) {
		try (Update update = Update.prepared("DELETE FROM terminal WHERE id = ?", stmt -> stmt.setInt(1, terminalId))) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	public static int replaceTerminal(int terminalId, Terminal terminal) {
		if (terminal == null) return -1;
		try (Update update = Update.prepared("UPDATE terminal SET port = ?, name = ? WHERE id = ?", stmt -> {
			stmt.setInt(1, terminal.getPortId());
			stmt.setString(2, terminal.getName());
			stmt.setInt(3, terminalId);
		})) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}

	public static Map<Integer, Terminal> getTerminalsByPort(int portId) {
		Map<Integer, Terminal> result = new HashMap<>();
		try (Query query = Query.prepared("SELECT * FROM terminal WHERE port = ?", stmt -> stmt.setInt(1, portId))) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				result.put(
					rs.getInt("id"),
					new Terminal(
						rs.getInt("id"),
						rs.getInt("port"),
						rs.getString("name")
					)
				);
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return result;
	}

	public static Terminal createTerminal(Terminal terminal) {
		if (terminal == null) return null;
		try (Query query = Query.prepared("INSERT INTO terminal (port, name) VALUES (?, ?) RETURNING *", stmt -> {
			stmt.setInt(1, terminal.getPortId());
			stmt.setString(2, terminal.getName());
		})) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return new Terminal(
				rs.getInt("id"),
				rs.getInt("port"),
				rs.getString("name")
			);
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}
}
