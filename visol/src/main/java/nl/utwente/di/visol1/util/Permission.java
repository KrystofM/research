package nl.utwente.di.visol1.util;

import java.util.Objects;
import java.util.function.Supplier;

import nl.utwente.di.visol1.dao.BerthDao;
import nl.utwente.di.visol1.dao.EmployeeDao;
import nl.utwente.di.visol1.dao.PortDao;
import nl.utwente.di.visol1.dao.TerminalDao;
import nl.utwente.di.visol1.dao.VesselDao;
import nl.utwente.di.visol1.models.Berth;
import nl.utwente.di.visol1.models.Employee;
import nl.utwente.di.visol1.models.EmployeeInput;
import nl.utwente.di.visol1.models.Port;
import nl.utwente.di.visol1.models.Role;
import nl.utwente.di.visol1.models.Schedule;
import nl.utwente.di.visol1.models.Terminal;
import nl.utwente.di.visol1.models.Vessel;

public abstract class Permission {
	public static Supplier<Permission> terminal(int terminal) {
		return () -> new TerminalPermission(terminal);
	}

	public static Supplier<Permission> schedule(int schedule) {
		return () -> new SchedulePermission(schedule);
	}

	public static Supplier<Permission> vessel(int vessel) {
		return () -> new VesselPermission(vessel);
	}

	public static Supplier<Permission> berth(int berth) {
		return () -> new BerthPermission(berth);
	}

	public static Supplier<Permission> employee(String employee) {
		return () -> new EmployeePermission(employee);
	}

	public static Supplier<Permission> port(int port) {
		return () -> new PortPermission(port);
	}

	public static Supplier<Permission> terminal(Terminal terminal) {
		return () -> new TerminalPermission(terminal);
	}

	public static Supplier<Permission> schedule(Schedule schedule) {
		return () -> new SchedulePermission(schedule);
	}

	public static Supplier<Permission> vessel(Vessel vessel) {
		return () -> new VesselPermission(vessel);
	}

	public static Supplier<Permission> berth(Berth berth) {
		return () -> new BerthPermission(berth);
	}

	public static Supplier<Permission> employee(Employee employee) {
		return () -> new EmployeePermission(employee);
	}

	public static Supplier<Permission> employee(EmployeeInput employee) {
		return () -> new EmployeePermission(employee);
	}

	public static Supplier<Permission> port(Port port) {
		return () -> new PortPermission(port);
	}

	public static Supplier<Permission> role(Role role) {
		return () -> new RolePermission(role);
	}

	public boolean check(Employee employee) {
		if (Configuration.Authorization.BYPASS.getAsBoolean()) {
			return true;
		}
		if (employee == null) {
			return false;
		}
		return checkInternal(employee);
	}

	abstract boolean checkInternal(Employee employee);

	private static class SchedulePermission extends VesselPermission {
		private SchedulePermission(int vessel) {
			super(vessel);
		}

		private SchedulePermission(Schedule schedule) {
			super(schedule.getVessel());
		}
	}

	private static class VesselPermission extends TerminalPermission {
		private VesselPermission(int vessel) {
			this(VesselDao.getVessel(vessel));
		}

		private VesselPermission(Vessel vessel) {
			super(Objects.requireNonNull(vessel).getDestination());
		}
	}

	private static class BerthPermission extends TerminalPermission {
		private BerthPermission(int berth) {
			this(BerthDao.getBerth(berth));
		}

		private BerthPermission(Berth berth) {
			super(Objects.requireNonNull(berth).getTerminalId());
		}
	}

	private static class TerminalPermission extends Permission {
		private final Terminal terminal;

		private TerminalPermission(int terminal) {
			this(TerminalDao.getTerminal(terminal));
		}

		private TerminalPermission(Terminal terminal) {
			this.terminal = Objects.requireNonNull(terminal);
		}

		@Override
		boolean checkInternal(Employee employee) {
			if (employee.getRole() == Role.RESEARCHER) {
				return true;
			} else if (employee.getRole() == Role.PORT_AUTHORITY) {
				return terminal.getPortId() == employee.getPort();
			} else if (employee.getRole() == Role.TERMINAL_MANAGER || employee.getRole() == Role.VESSEL_PLANNER) {
				return terminal.getId() == employee.getTerminal();
			}

			return false;
		}
	}

	private static class PortPermission extends Permission {
		private final Port port;

		private PortPermission(int port) {
			this(PortDao.getPort(port));
		}

		private PortPermission(Port port) {
			this.port = Objects.requireNonNull(port);
		}

		@Override
		boolean checkInternal(Employee employee) {
			if (employee.getRole() == Role.RESEARCHER) {
				return true;
			} else if (employee.getRole() == Role.PORT_AUTHORITY) {
				return employee.getPort() == port.getId();
			} else if (employee.getRole() == Role.TERMINAL_MANAGER || employee.getRole() == Role.VESSEL_PLANNER) {
				return false;
			}

			return false;
		}
	}

	private static class RolePermission extends Permission {
		private final Role role;

		private RolePermission(Role role) {
			this.role = role;
		}

		@Override
		boolean checkInternal(Employee employee) {
			return Role.compare(employee.getRole(), role) >= 0;
		}
	}

	private static class EmployeePermission extends Permission {
		private final Employee employee;

		private EmployeePermission(String email) {
			this(EmployeeDao.getEmployee(email));
		}

		private EmployeePermission(Employee employee) {
			this.employee = Objects.requireNonNull(employee);
		}

		private EmployeePermission(EmployeeInput employee) {
			this.employee = new Employee(employee.getEmail(), null, null, employee.getRole(), employee.getTerminal(), employee.getPort());
		}

		@Override
		boolean checkInternal(Employee employee) {
			// Return true if the employee is the same as the one we are checking for OR
			// if the employee has a higher clearance within the same region
			if (this.employee.equals(employee)) {
				return true;
			}
			if (this.employee.getRole() == Role.RESEARCHER) {
				return false;
			} else if (this.employee.getRole() == Role.PORT_AUTHORITY) {
				return employee.getRole() == Role.RESEARCHER;
			} else if (this.employee.getRole() == Role.TERMINAL_MANAGER) {
				return employee.getRole() == Role.RESEARCHER || (
					employee.getRole() == Role.PORT_AUTHORITY &&
					employee.getPort() == Objects.requireNonNull(TerminalDao.getTerminal(this.employee.getTerminal())).getPortId()
				);
			} else if (this.employee.getRole() == Role.VESSEL_PLANNER) {
				return employee.getRole() == Role.RESEARCHER || (
					employee.getRole() == Role.PORT_AUTHORITY &&
					employee.getPort() == Objects.requireNonNull(TerminalDao.getTerminal(this.employee.getTerminal())).getPortId()
				) || (
					       employee.getRole() == Role.TERMINAL_MANAGER &&
					       Objects.equals(employee.getTerminal(), this.employee.getTerminal())
				       );
			}

			return false;
		}
	}
}
