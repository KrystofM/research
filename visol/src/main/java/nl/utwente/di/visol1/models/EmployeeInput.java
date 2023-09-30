package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

import nl.utwente.di.visol1.util.EncryptionUtil;
import nl.utwente.di.visol1.util.EncryptionUtil.SaltedKey;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EmployeeInput implements Comparable<EmployeeInput> {
	private String email;
	private String password;
	private Role role;
	private Integer terminal;
	private Integer port;

	public EmployeeInput(String email, String password, Role role, Integer terminal, Integer port) {
		this.email = email;
		this.password = password;
		this.role = role;
		this.terminal = terminal;
		this.port = port;
	}

	public EmployeeInput() {
		// Empty constructor
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Integer getTerminal() {
		return terminal;
	}

	public void setTerminal(Integer terminal) {
		this.terminal = terminal;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Employee generate() {
		SaltedKey key = EncryptionUtil.generateKey(password);
		return new Employee(email, key.hash, key.salt, role, terminal, port);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EmployeeInput)) return false;
		EmployeeInput other = (EmployeeInput) o;
		return Objects.equals(email, other.email) &&
		       Objects.equals(password, other.password) &&
		       role == other.role &&
		       Objects.equals(port, other.port) &&
		       Objects.equals(terminal, other.terminal);
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, password, role, port, terminal);
	}

	@Override
	public int compareTo(EmployeeInput other) {
		return email.compareTo(other.email);
	}
}
