package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Terminal implements Comparable<Terminal> {
	@XmlTransient
	@JsonIgnore
	private int id;


	private String name;
	@JsonProperty("port")
	@XmlElement(name = "port")
	private int portId;

	public Terminal(int id, int portId, String name) {
		this.id = id;
		this.portId = portId;
		this.name = name;
	}

	public Terminal() {
		// Empty constructor
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPortId() {
		return portId;
	}

	public void setPortId(int portId) {
		this.portId = portId;
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
		if (!(o instanceof Terminal)) return false;
		Terminal terminal = (Terminal) o;
		return id == terminal.id && portId == terminal.portId && terminal.name.equals(name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, portId);
	}

	@Override
	public int compareTo(Terminal other) {
		return Integer.compare(id, other.id);
	}
}
