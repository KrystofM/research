package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.utwente.di.visol1.type_adapters.CustomJacksonJsonProvider;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class VesselChange {
	private long id;
	private String author;
	private Timestamp date;
	private String reason;
	/**
	 * Whether the change was an undo performed by the user
	 */
	private boolean undo = false;
	private Integer vessel;
	@JsonProperty("old")
	@XmlElement(name = "old")
	private Vessel oldVessel;
	@JsonProperty("new")
	@XmlElement(name = "new")
	private Vessel newVessel;

	public VesselChange() {
		// Empty Constructor
	}

	public VesselChange(String author, String reason, Vessel deletedVessel) {
		// Constructor to call when deleting a schedule
		this.author = author;
		this.reason = reason == null || reason.equals("") ? "Vessel deleted" : reason;
		this.vessel = deletedVessel.getId();
		this.oldVessel = deletedVessel;
	}

	public VesselChange(String author, String reason, Vessel oldVessel, Vessel newVessel){
		// Constructor to call with replacing or creating a vessel
		this.author = author;
		this.reason = reason;
		this.vessel = newVessel == null ? null : newVessel.getId();
		this.oldVessel = oldVessel;
		this.newVessel = newVessel;
	}

	public VesselChange(long id, String author, Timestamp date, String reason, int vessel, String oldVessel, String newVessel, boolean undo) {
		this.id = id;
		this.author = author;
		this.date = date;
		this.reason = reason;
		this.vessel = vessel;
		this.undo = undo;
		try {
			ObjectMapper mapper = CustomJacksonJsonProvider.MAPPER;
			this.oldVessel = oldVessel == null || oldVessel.equals("") ? null : mapper.readValue(oldVessel, Vessel.class);
			this.newVessel = newVessel == null || newVessel.equals("") ? null : mapper.readValue(newVessel, Vessel.class);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
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

	public Integer getVessel() {
		return vessel;
	}

	public void setVessel(Integer vessel) {
		this.vessel = vessel;
	}

	public Vessel getOldVessel() {
		return oldVessel;
	}

	public void setOldVessel(Vessel oldVessel) {
		this.oldVessel = oldVessel;
	}

	public Vessel getNewVessel() {
		return newVessel;
	}

	public void setNewVessel(Vessel newVessel) {
		this.newVessel = newVessel;
	}

	public boolean isUndo() {
		return undo;
	}

	public void setUndo(boolean undo) {
		this.undo = undo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VesselChange that = (VesselChange) o;
		return id == that.id && undo == that.undo && Objects.equals(author, that.author) && Objects.equals(date, that.date) && Objects.equals(reason,
		                                                                                                                                      that.reason)
		       && Objects.equals(vessel, that.vessel) && Objects.equals(oldVessel, that.oldVessel) && Objects.equals(newVessel, that.newVessel);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, author, date, reason, undo, vessel, oldVessel, newVessel);
	}
}
