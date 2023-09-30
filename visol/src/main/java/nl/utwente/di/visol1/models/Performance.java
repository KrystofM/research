package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Performance {
	@JsonProperty("total_cost")
	@XmlElement(name = "total_cost")
	/**
	 * Sum of costs of all vessels planned with cost per vessel = cost_per_hour of vessel * (leaving time - arrival time)
	 */
		double totalCost;
	@JsonProperty("unscheduled_vessels")
	@XmlElement(name = "unscheduled_vessels")
	/**
	 * Amount of unscheduled vessels
	 */
		int unscheduledVessels;
	@JsonProperty("scheduled_vessels")
	@XmlElement(name = "scheduled_vessels")
	/**
	 * Amount of scheduled vessels
	 */
		int scheduledVessels;

	public Performance(){

	}

	public Performance(double totalCost, int scheduledVessels, int unscheduledVessels) {
		this.totalCost = totalCost;
		this.unscheduledVessels = unscheduledVessels;
		this.scheduledVessels = scheduledVessels;
	}

	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}

	public int getUnscheduledVessels() {
		return unscheduledVessels;
	}

	public void setUnscheduledVessels(int unscheduledVessels) {
		this.unscheduledVessels = unscheduledVessels;
	}

	public int getScheduledVessels() {
		return scheduledVessels;
	}

	public void setScheduledVessels(int scheduledVessels) {
		this.scheduledVessels = scheduledVessels;
	}
}
