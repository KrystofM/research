package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Objects;

import nl.utwente.di.visol1.util.EncryptionUtil.SaltedKey;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Employee implements Comparable<Employee> {
	private String email;
	private SaltedKey key;
	private Role role;
	private Integer terminal;
	private Integer port;

	public Employee(String email, byte[] keyHash, byte[] keySalt, Role role, Integer terminal, Integer port) {
		this.email = email;
		this.key = new SaltedKey(keyHash, keySalt);
		this.role = role;
		this.terminal = terminal;
		this.port = port;
	}

	public Employee() {
		// Empty constructor
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public SaltedKey getKey() {
		return key;
	}

	public void setKey(SaltedKey key) {
		this.key = key;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Employee)) return false;
		Employee other = (Employee) o;
		return Objects.equals(email, other.email) &&
		       Objects.equals(key, other.key) &&
		       role == other.role &&
		       Objects.equals(port, other.port) &&
		       Objects.equals(terminal, other.terminal);
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, key, role, port, terminal);
	}

	@Override
	public int compareTo(Employee other) {
		return email.compareTo(other.email);
	}
}
