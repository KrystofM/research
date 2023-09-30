package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Port implements Comparable<Port> {
	@XmlTransient
	@JsonIgnore
	private int id;
	private String name;

	public Port(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public Port() {
		// Empty constructor
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Port)) return false;
		Port port = (Port) o;
		return id == port.id && Objects.equals(name, port.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public int compareTo(Port other) {
		return Integer.compare(id, other.id);
	}
}
