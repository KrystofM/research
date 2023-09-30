package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.sql.Timestamp;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Change {
	@XmlTransient
	@JsonIgnore
	private long id;
	private String author;
	private Timestamp date;
	private String reason;
	/**
	 * Whether the change was an undo command
	 */
	private boolean undo;
	private Integer vessel;
	@JsonProperty("vessel_name")
	@XmlElement(name = "vessel_name")
	private String vesselName;
	/**
	 * Type of change, either a vesselChange or a scheduleChange
	 */
	private String type;
	@JsonProperty("old")
	@XmlElement(name = "old")
	private String oldObject;
	@JsonProperty("new")
	@XmlElement(name = "new")
	private String newObject;

	public Change() {
		// Empty constructor
	}

	public Change(long id, String author, Timestamp date, String reason, boolean undo, Integer vessel, String vesselName, String type,
	              String oldObject, String newObject) {
		this.id = id;
		this.author = author;
		this.date = date;
		this.reason = reason;
		this.undo = undo;
		this.vessel = vessel;
		this.vesselName = vesselName;
		this.type = type;
		this.oldObject = oldObject;
		this.newObject = newObject;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public boolean isUndo() {
		return undo;
	}

	public void setUndo(boolean undo) {
		this.undo = undo;
	}

	public Integer getVessel() {
		return vessel;
	}

	public void setVessel(Integer vessel) {
		this.vessel = vessel;
	}

	public String getVesselName() {
		return vesselName;
	}

	public void setVesselName(String vesselName) {
		this.vesselName = vesselName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOldObject() {
		return oldObject;
	}

	public void setOldObject(String oldObject) {
		this.oldObject = oldObject;
	}

	public String getNewObject() {
		return newObject;
	}

	public void setNewObject(String newObject) {
		this.newObject = newObject;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Change change = (Change) o;
		return id == change.id && undo == change.undo && Objects.equals(author, change.author) && Objects.equals(date, change.date)
		       && Objects.equals(reason, change.reason) && Objects.equals(vessel, change.vessel) && Objects.equals(vesselName, change.vesselName)
		       && Objects.equals(type, change.type) && Objects.equals(oldObject, change.oldObject) && Objects.equals(newObject, change.newObject);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, author, date, reason, undo, vessel, vesselName, type, oldObject, newObject);
	}
}
