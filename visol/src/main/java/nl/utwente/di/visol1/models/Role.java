package nl.utwente.di.visol1.models;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonValue;

@XmlType
@XmlEnum
public enum Role {
	@XmlEnumValue("vessel planner") VESSEL_PLANNER(1, "vessel planner"),
	@XmlEnumValue("terminal manager") TERMINAL_MANAGER(2, "terminal manager"),
	@XmlEnumValue("port authority") PORT_AUTHORITY(3, "port authority"),
	@XmlEnumValue("researcher") RESEARCHER(4, "researcher");

	private final int clearance;
	private final String value;

	Role(int clearance, String value) {
		this.clearance = clearance;
		this.value = value;
	}

	public static Role fromValue(String value) {
		for (Role role : Role.values()) {
			if (role.getValue().equals(value)) {
				return role;
			}
		}
		throw new IllegalArgumentException(value);
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	public static int compare(Role role1, Role role2) {
		return Integer.compare(role1.clearance, role2.clearance);
	}
}
