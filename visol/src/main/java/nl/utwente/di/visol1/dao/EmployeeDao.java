package nl.utwente.di.visol1.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import nl.utwente.di.visol1.models.Employee;
import nl.utwente.di.visol1.models.Role;
import nl.utwente.di.visol1.util.EncryptionUtil;

public class EmployeeDao extends GenericDao {
	public static Employee getEmployee(String email) {
		try (Query query = Query.prepared("SELECT * FROM employee WHERE email = ?", stmt -> stmt.setString(1, email))) {
			ResultSet rs = query.getResultSet();
			if (!rs.next()) return null;
			return fromResultSet(rs);
		} catch (SQLException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static List<Employee> getEmployees() {
		List<Employee> result = new ArrayList<>();
		try (Query query = Query.simple("SELECT * FROM employee")) {
			ResultSet rs = query.getResultSet();
			while (rs.next()) {
				result.add(fromResultSet(rs));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return result;
	}

	public static int replaceEmployee(String email, Employee employee) {
		if (email == null || employee == null) return -1;
		try (Update update = Update.prepared(
			"UPDATE employee SET key_hash = ?, key_salt = ?, role = ?::role, terminal = ?, port = ? WHERE email = ?",
			stmt -> {
				stmt.setBytes(1, employee.getKey().hash);
				stmt.setBytes(2, employee.getKey().salt);
				stmt.setString(3, employee.getRole().getValue());
				stmt.setObject(4, employee.getTerminal(), Types.INTEGER);
				stmt.setObject(5, employee.getPort(), Types.INTEGER);
			}
		)) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}


	public static Employee createEmployee(Employee employee) {
		if (employee == null) return null;
		try (Query query = Query.prepared(
			"INSERT INTO employee (email, key_hash, key_salt, role, terminal, port) VALUES (?, ?, ?, ?::role, ?, ?) RETURNING *",
			stmt -> {
				stmt.setString(1, employee.getEmail());
				stmt.setBytes(2, employee.getKey().hash);
				stmt.setBytes(3, employee.getKey().salt);
				stmt.setString(4, employee.getRole().getValue());
				stmt.setObject(5, employee.getTerminal(), Types.INTEGER);
				stmt.setObject(6, employee.getPort(), Types.INTEGER);
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

	public static int deleteEmployee(String email) {
		try (Update update = Update.prepared("DELETE FROM employee WHERE email = ?", stmt -> stmt.setString(1, email))) {
			return update.getRowsChanged();
		} catch (SQLException exception) {
			exception.printStackTrace();
			return -1;
		}
	}


	private static Employee fromResultSet(ResultSet rs) throws SQLException {
		return new Employee(
			rs.getString("email"),
			rs.getBytes("key_hash"),
			rs.getBytes("key_salt"),
			Role.fromValue(rs.getString("role")),
			getInteger(rs, "terminal"),
			getInteger(rs, "port")
		);
	}

	private static Integer getInteger(ResultSet rs, String label) throws SQLException {
		int result = rs.getInt(label);
		return rs.wasNull() ? null : result;
	}

	public static JsonNode getGravatar(String email) {
		// First, get the MD5 hash of the email address
		byte[] hash = EncryptionUtil.MD5.digest(email.getBytes());
		// Then, convert the hash to a hex string
		String hex = EncryptionUtil.bytesToHex(hash);
		// Then, construct the object
		JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(false);
		return factory.objectNode()
			.put("profile", "https://en.gravatar.com/" + hex + ".json")
			.put("picture", "https://gravatar.com/avatar/" + hex);
	}
}
