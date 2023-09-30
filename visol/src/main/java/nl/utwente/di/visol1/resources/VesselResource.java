package nl.utwente.di.visol1.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Objects;

import nl.utwente.di.visol1.dao.BerthDao;
import nl.utwente.di.visol1.dao.ScheduleChangeDao;
import nl.utwente.di.visol1.dao.ScheduleDao;
import nl.utwente.di.visol1.dao.VesselChangeDao;
import nl.utwente.di.visol1.dao.VesselDao;
import nl.utwente.di.visol1.models.Berth;
import nl.utwente.di.visol1.models.Infeasibility;
import nl.utwente.di.visol1.models.Schedule;
import nl.utwente.di.visol1.models.ScheduleChange;
import nl.utwente.di.visol1.models.Vessel;
import nl.utwente.di.visol1.models.VesselChange;
import nl.utwente.di.visol1.optimise.OptimiseSchedule;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;


public class VesselResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	int id;

	public VesselResource(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = Integer.parseInt(id);
	}

	@DELETE
	public Response deleteVessel(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Vessel deletedVessel = VesselDao.getVessel(id);
			if (deletedVessel != null && VesselDao.deleteVessel(id) > 0) {
				if (requestEmployee != null) {
					VesselChangeDao.createVesselChange(new VesselChange(
						requestEmployee.getEmail(), request.getHeader("Reason"), deletedVessel
					));
				} else {
					VesselChangeDao.createVesselChange(new VesselChange(
						null, request.getHeader("Reason"), deletedVessel
					));
				}
				return Response.noContent().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		}, Permission.vessel(id));
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response replaceVessel(@Context HttpServletRequest request, Vessel vessel) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Vessel oldVessel = VesselDao.getVessel(id);
			int i = VesselDao.replaceVessel(id, vessel);
			if (i == -1) {
				return Response.notAcceptable(List.of()).build();
			} else if (i == 0) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				Vessel newVessel = VesselDao.getVessel(id);
				if (!Objects.equals(oldVessel, newVessel)) {
					if (requestEmployee != null) {
						VesselChangeDao.createVesselChange(new VesselChange(
							requestEmployee.getEmail(), request.getHeader("Reason"), oldVessel, newVessel
						));
					} else {
						VesselChangeDao.createVesselChange(new VesselChange(
							null, request.getHeader("Reason"), oldVessel, newVessel
						));
					}
				}
				return Response.ok(newVessel).build();
			}
		}, Permission.vessel(id));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVessel(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Vessel vessel = VesselDao.getVessel(id);
			return (vessel != null ? Response.ok(vessel) : Response.status(Response.Status.NOT_FOUND)).build();
		}, Permission.vessel(id));
	}

	@Path("/schedule")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSchedule(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Schedule schedule = ScheduleDao.getScheduleByVessel(id);
			return (schedule != null ? Response.ok(schedule) : Response.status(Response.Status.NOT_FOUND)).build();
		}, Permission.schedule(id));
	}

	@Path("/schedule")
	@DELETE
	public Response deleteSchedule(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Schedule deletedSchedule = ScheduleDao.getScheduleByVessel(id);
			if (deletedSchedule != null && ScheduleDao.deleteScheduleByVessel(id) > 0) {
				if (requestEmployee != null) {
					ScheduleChangeDao.createScheduleChange(new ScheduleChange(
						requestEmployee.getEmail(), request.getHeader("Reason"), deletedSchedule
					));
				} else {
					ScheduleChangeDao.createScheduleChange(new ScheduleChange(
						null, request.getHeader("Reason"), deletedSchedule
					));
				}
				return Response.noContent().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		}, Permission.schedule(id));
	}

	@Path("/schedule")
	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response replaceSchedule(@Context HttpServletRequest request, Schedule schedule) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Schedule oldSchedule = ScheduleDao.getScheduleByVessel(id);
			Vessel vessel = VesselDao.getVessel(id);
			Berth berth = BerthDao.getBerth(schedule.getBerth());
			if (schedule.isManual()) {
				if (vessel == null || berth == null || schedule.getStart() == null ||
				    berth.getTerminalId() != vessel.getDestination()) {
					return Response.notAcceptable(List.of()).build();
				}
			}
			Schedule newSchedule = ScheduleDao.replaceSchedule(
				id, !schedule.isManual() && vessel != null
				    ? OptimiseSchedule.planAutomaticVessel(vessel, oldSchedule)
				    : schedule
			);
			if (newSchedule != null) {
				if (!Objects.equals(oldSchedule, newSchedule)) {
					if (requestEmployee != null) {
						ScheduleChangeDao.createScheduleChange(new ScheduleChange(
							requestEmployee.getEmail(), request.getHeader("Reason"), oldSchedule, newSchedule
						));
					} else {
						ScheduleChangeDao.createScheduleChange(new ScheduleChange(
							null, request.getHeader("Reason"), oldSchedule, newSchedule
						));
					}
				}
				return Response.ok(newSchedule).build();
			} else {
				return Response.notAcceptable(List.of()).build();
			}
		}, Permission.schedule(id));
	}
}
