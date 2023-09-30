package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.utwente.di.visol1.type_adapters.TimestampAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Vessel implements Comparable<Vessel> {
	@XmlTransient
	@JsonIgnore
	private int id;
	private String name;
	private int containers;
	/**
	 * Destination terminal of the vessel
	 */
	private int destination;
	/**
	 * When the vessel arrives in the port
	 */
	private Timestamp arrival;
	/**
	 * The time by which the vessel has to leave the port
	 */
	private Timestamp deadline;
	@JsonProperty("cost_per_hour")
	@XmlElement(name = "cost_per_hour")
	/**
	 * Cost per hour of the vessel being in the port
	 */
	private double costPerHour;
	private int width;
	private int length;
	private int depth;

	public Vessel(int id, String name, Timestamp arrival, Timestamp deadline, int containers, double costPerHour, int destination, int length,
	              int width, int depth) {
		this.id = id;
		this.name = name;
		this.containers = containers;
		this.destination = destination;
		this.arrival = arrival;
		this.deadline = deadline;
		this.width = width;
		this.length = length;
		this.depth = depth;
		this.costPerHour = costPerHour;
	}

	public Vessel() {
		// Empty constructor
	}

	public static String toString(Vessel vessel) {
		return Optional.ofNullable(vessel).map(Object::toString).orElse(null);
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

	public int getContainers() {
		return containers;
	}

	public void setContainers(int containers) {
		this.containers = containers;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public Timestamp getArrival() {
		return arrival;
	}

	public void setArrival(Timestamp arrival) {
		this.arrival = arrival;
	}

	public Timestamp getDeadline() {
		return deadline;
	}

	public void setDeadline(Timestamp deadline) {
		this.deadline = deadline;
	}

	public double getCostPerHour() {
		return costPerHour;
	}

	public void setCostPerHour(double costPerHour) {
		this.costPerHour = costPerHour;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Vessel)) return false;

		Vessel vessel = (Vessel) o;
		return id == vessel.id && containers == vessel.containers && destination == vessel.destination
		       && Double.compare(costPerHour, vessel.costPerHour) == 0 && width == vessel.width && length == vessel.length && depth == vessel.depth
		       && Objects.equals(name, vessel.name) && Objects.equals(arrival, vessel.arrival) && Objects.equals(deadline, vessel.deadline);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, containers, destination, arrival, deadline, costPerHour, width, length, depth);
	}

	@Override
	public int compareTo(Vessel other) {
		return Integer.compare(id, other.id);
	}

	@Override
	public String toString() {
		JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(false);
		ObjectNode vesselObject = factory.objectNode();
		vesselObject.set("name", factory.textNode(name));
		vesselObject.set("containers", factory.numberNode(containers));
		vesselObject.set("destination", factory.numberNode(destination));
		vesselObject.set("arrival", factory.textNode(TimestampAdapter.adapt(arrival)));
		vesselObject.set("deadline", factory.textNode(TimestampAdapter.adapt(deadline)));
		vesselObject.set("cost_per_hour", factory.numberNode(costPerHour));
		vesselObject.set("width", factory.numberNode(width));
		vesselObject.set("length", factory.numberNode(length));
		vesselObject.set("depth", factory.numberNode(depth));
		return vesselObject.toString();
	}
}
