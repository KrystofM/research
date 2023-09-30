package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.sql.Time;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Berth implements Comparable<Berth> {
	@XmlTransient
	@JsonIgnore
	private int id;
	@XmlElement(name = "terminal")
	@JsonProperty("terminal")
	private int terminalId;
	private Time open;
	private Time close;
	@JsonProperty("unload_speed")
	@XmlElement(name = "unload_speed")
	/**
	 * Unload speed of berth in containers per hour
	 */
	private double unloadSpeed;
	private int width;
	private int depth;
	private int length;

	public Berth(int id, int terminalId, Time open, Time close, double unloadSpeed, int length, int width, int depth) {
		this.id = id;
		this.terminalId = terminalId;
		this.open = open;
		this.close = close;
		this.unloadSpeed = unloadSpeed;
		this.width = width;
		this.depth = depth;
		this.length = length;
	}

	public Berth() {
		// Empty constructor
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(int terminalId) {
		this.terminalId = terminalId;
	}

	public Time getOpen() {
		return open;
	}

	public void setOpen(Time open) {
		this.open = open;
	}

	public Time getClose() {
		return close;
	}

	public void setClose(Time close) {
		this.close = close;
	}

	public double getUnloadSpeed() {
		return unloadSpeed;
	}

	public void setUnloadSpeed(double unloadSpeed) {
		this.unloadSpeed = unloadSpeed;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean fits(Vessel vessel) {
		return vessel.getWidth() <= getWidth() && vessel.getDepth() <= getDepth() && vessel.getLength() <= getLength();
	}


	public boolean isClosedDuring(Schedule schedule) {
		Time start = new Time(schedule.getStart().getTime());
		start = Time.valueOf(start.toString());

		Time end = new Time(schedule.getExpectedEnd().getTime());
		end = Time.valueOf(end.toString());

		return !(!start.before(open) && !end.after(close)) && !(!end.before(open) && !end.after(end));
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Berth)) return false;
		Berth berth = (Berth) o;
		return id == berth.id && terminalId == berth.terminalId && Double.compare(berth.unloadSpeed, unloadSpeed) == 0 && width == berth.width
		       && depth == berth.depth && length == berth.length && Objects.equals(open, berth.open) && Objects.equals(close, berth.close);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, terminalId, open, close, unloadSpeed, width, depth, length);
	}

	@Override
	public int compareTo(Berth other) {
		return Integer.compare(id, other.id);
	}
}
