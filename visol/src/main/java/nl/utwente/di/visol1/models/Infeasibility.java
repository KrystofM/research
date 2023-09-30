package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Infeasibility {
	public static final String OVERLAPPING = "OVERLAPPING";
	public static final String OTHER = "OTHER";
	private boolean valid;
	/**
	 * Type of infeasibility either OVERLAPPING or OTHER, since OVERLAPPING is handled differently
	 */
	private String type;
	private String reason;


	public Infeasibility() {
		// Empty constructor
	}

	public Infeasibility(boolean valid, String reason, String type) {
		this.valid = valid;
		this.reason = reason;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
