package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.utwente.di.visol1.type_adapters.TimestampAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Schedule implements Comparable<Schedule> {
	private int vessel;
	private int berth;
	/**
	 * Whether the vessel is manually planned by the vessel planner or planned automatically
	 */
	boolean manual;
	Timestamp start;
	@JsonProperty("expected_end")
	@XmlElement(name = "expected_end")
	Timestamp expectedEnd;


	public Schedule(int vessel, int berth, boolean manual, Timestamp start, Timestamp expectedEnd) {
		this.vessel = vessel;
		this.berth = berth;
		this.manual = manual;
		this.start = start;
		this.expectedEnd = expectedEnd;
	}

	public Schedule() {
		//empty constructor
	}

	public static String toString(Schedule schedule) {
		return Optional.ofNullable(schedule).map(Object::toString).orElse(null);
	}

	public int getVessel() {
		return vessel;
	}

	public void setVessel(int vessel) {
		this.vessel = vessel;
	}

	public int getBerth() {
		return berth;
	}

	public void setBerth(int berth) {
		this.berth = berth;
	}

	public boolean isManual() {
		return manual;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}

	public Timestamp getStart() {
		return start;
	}

	public void setStart(Timestamp start) {
		this.start = start;
	}

	public Timestamp getExpectedEnd() {
		return expectedEnd;
	}

	public void setExpectedEnd(Timestamp expectedEnd) {
		this.expectedEnd = expectedEnd;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Schedule)) return false;
		Schedule schedule = (Schedule) o;
		return vessel == schedule.vessel && berth == schedule.berth && manual == schedule.manual && Objects.equals(start, schedule.start)
		       && Objects.equals(expectedEnd, schedule.expectedEnd);
	}

	@Override
	public int hashCode() {
		return Objects.hash(vessel, berth, manual, start, expectedEnd);
	}

	@Override
	public int compareTo(Schedule other) {
		return Integer.compare(vessel, other.vessel);
	}

	@Override
	public String toString() {
		JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(false);
		ObjectNode scheduleObject = factory.objectNode();
		scheduleObject.set("vessel", factory.numberNode(vessel));
		scheduleObject.set("berth", factory.numberNode(berth));
		scheduleObject.set("manual", factory.booleanNode(manual));
		scheduleObject.set("start", factory.textNode(TimestampAdapter.adapt(start)));
		scheduleObject.set("expected_end", factory.textNode(TimestampAdapter.adapt(expectedEnd)));
		return scheduleObject.toString();
	}
}
