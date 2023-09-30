package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.utwente.di.visol1.type_adapters.CustomJacksonJsonProvider;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ScheduleChange {
	@XmlTransient
	@JsonIgnore
	private long id;
	private String author;
	private Timestamp date;
	private String reason;
	/**
	 * Whether the change was an undo performed by a user
	 */
	private boolean undo = false;
	private Integer vessel;
	@JsonProperty("old")
	@XmlElement(name = "old")
	private Schedule oldSchedule;
	@JsonProperty("new")
	@XmlElement(name = "new")
	private Schedule newSchedule;

	public ScheduleChange() {
		//Empty Constructor
	}

	public ScheduleChange(String author, String reason, Schedule deletedSchedule) {
		// Constructor to call when deleting a schedule
		this.author = author;
		this.reason = reason == null || reason.equals("") ? "Schedule deleted" : reason;
		this.vessel = deletedSchedule == null ? null : deletedSchedule.getVessel();
		this.oldSchedule = deletedSchedule;
	}


	public ScheduleChange(String author, String reason, Schedule oldSchedule, Schedule newSchedule) {
		// Constructor to call with replacing or creating a schedule
		this.author = author;
		this.reason = reason;
		this.vessel = newSchedule == null ? null : newSchedule.getVessel();
		this.oldSchedule = oldSchedule;
		this.newSchedule = newSchedule;
	}

	public ScheduleChange(long id, String author, Timestamp date, String reason, int vessel, String oldSchedule, String newSchedule, boolean undo) {
		this.id = id;
		this.author = author;
		this.date = date;
		this.reason = reason;
		this.vessel = vessel;
		this.undo = undo;
		try {
			ObjectMapper mapper = CustomJacksonJsonProvider.MAPPER;
			this.oldSchedule = oldSchedule == null || oldSchedule.equals("") ? null : mapper.readValue(oldSchedule, Schedule.class);
			this.newSchedule = newSchedule == null || newSchedule.equals("") ? null : mapper.readValue(newSchedule, Schedule.class);
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

	public Schedule getOldSchedule() {
		return oldSchedule;
	}

	public void setOldSchedule(Schedule oldSchedule) {
		this.oldSchedule = oldSchedule;
	}

	public Schedule getNewSchedule() {
		return newSchedule;
	}

	public void setNewSchedule(Schedule newSchedule) {
		this.newSchedule = newSchedule;
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
		ScheduleChange that = (ScheduleChange) o;
		return id == that.id && undo == that.undo && Objects.equals(author, that.author) && Objects.equals(date, that.date) && Objects.equals(reason,
		                                                                                                                                      that.reason)
		       && Objects.equals(vessel, that.vessel) && Objects.equals(oldSchedule, that.oldSchedule) && Objects.equals(newSchedule,
		                                                                                                                 that.newSchedule);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, author, date, reason, undo, vessel, oldSchedule, newSchedule);
	}
}
