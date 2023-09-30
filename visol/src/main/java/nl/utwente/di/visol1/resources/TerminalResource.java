package nl.utwente.di.visol1.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.utwente.di.visol1.dao.BerthDao;
import nl.utwente.di.visol1.dao.ChangeDao;
import nl.utwente.di.visol1.dao.GenericDao;
import nl.utwente.di.visol1.dao.ScheduleDao;
import nl.utwente.di.visol1.dao.TerminalDao;
import nl.utwente.di.visol1.dao.VesselDao;
import nl.utwente.di.visol1.models.Berth;
import nl.utwente.di.visol1.models.Infeasibility;
import nl.utwente.di.visol1.models.Performance;
import nl.utwente.di.visol1.models.Schedule;
import nl.utwente.di.visol1.models.Terminal;
import nl.utwente.di.visol1.models.Vessel;
import nl.utwente.di.visol1.optimise.OptimiseSchedule;
import nl.utwente.di.visol1.type_adapters.TimestampAdapter;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

public class TerminalResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	int id;

	public TerminalResource(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = Integer.parseInt(id);
	}

	private static Infeasibility getOtherInfeasibility(Schedule schedule, Vessel vessel, Berth berth) {
		if (berth == null || vessel == null) {
			return null;
		}
		if (!berth.fits(vessel)) {
			return new Infeasibility(false, "Vessel doesn't fit in berth", Infeasibility.OTHER);
		}
		if (vessel.getArrival().after(schedule.getStart())) {
			return new Infeasibility(false, "Vessel arrives after start of schedule", Infeasibility.OTHER);
		}
		if (vessel.getDeadline() != null && vessel.getDeadline().before(schedule.getExpectedEnd())) {
			return new Infeasibility(false, "Vessel deadline before end of schedule", Infeasibility.OTHER);
		}

		return new Infeasibility(true, null, "");
	}

	@DELETE
	public Response deleteTerminal(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> TerminalDao.deleteTerminal(id) > 0
		                                                     ? Response.noContent().build()
		                                                     : Response.status(Response.Status.NOT_FOUND).build(),
		                       Permission.terminal(id));
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response replaceTerminal(@Context HttpServletRequest request, Terminal terminal) {
		return TokenUtil.check(request, (requestEmployee) -> {
			int i = TerminalDao.replaceTerminal(id, terminal);
			if (i == -1) {
				return Response.notAcceptable(List.of()).build();
			} else if (i == 0) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.ok(TerminalDao.getTerminal(id)).build();
			}
		}, Permission.terminal(id));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTerminal(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> TerminalDao.getTerminal(id) != null
		                                                     ? Response.ok(TerminalDao.getTerminal(id)).build()
		                                                     : Response.status(Response.Status.NOT_FOUND).build(),
		                       Permission.terminal(id));
	}

	@Path("/schedules")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSchedules(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(ScheduleDao.getSchedulesByTerminal(
			id,
			TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
			TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME)
		)).build(), Permission.terminal(id));
	}

	@Path("/schedules/valid")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInfeasibilities(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> {
			                       Map<Integer, Infeasibility> infeasibilityMap = new HashMap<>(); //vesselid -> infeasibility
			                       Map<Integer, List<Schedule>> schedules = ScheduleDao.getSchedulesByTerminal(id, TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
			                                                                                                   TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME));
			                       Map<Integer, Schedule> scheduleMap = new HashMap<>();

			                       List<Integer> vesselIds = new ArrayList<>();
			                       for (int k : schedules.keySet()) {
				                       for (Schedule s : schedules.get(k)) {
					                       scheduleMap.put(s.getVessel(), s);
					                       vesselIds.add(s.getVessel());
				                       }
			                       }
			                       Map<Integer, Berth> berthMap = BerthDao.getBerthsInArray(new ArrayList<>(schedules.keySet()));
			                       Map<Integer, Vessel> vesselMap = VesselDao.getVesselsInArray(vesselIds);

			                       Infeasibility overlapInf = new Infeasibility(false, "Overlapping with another vessel", Infeasibility.OVERLAPPING);
			                       for (int k : schedules.keySet()) {
				                       for (Schedule s : schedules.get(k)) {
					                       List<Schedule> scheduleCopy = new ArrayList<>(schedules.get(k));
					                       for (Schedule s2 : scheduleCopy) {
						                       if (s.getVessel() == s2.getVessel()) continue;
						                       if ((!s2.getStart().after(s.getStart()) && s2.getExpectedEnd().after(s.getStart()))
						                           || (s2.getStart().before(s.getExpectedEnd()) && !s2.getExpectedEnd().before(s.getExpectedEnd()))
						                           || (!s2.getStart().before(s.getStart()) && !s2.getExpectedEnd().after(s.getExpectedEnd()))) {
							                       infeasibilityMap.put(s2.getVessel(), overlapInf);
							                       infeasibilityMap.put(s.getVessel(), overlapInf);
						                       }
					                       }

				                       }
			                       }

			                       for (int vesselId : vesselMap.keySet()) {
				                       if (infeasibilityMap.containsKey(vesselId)) continue;
				                       Infeasibility inf = getOtherInfeasibility(scheduleMap.get(vesselId), vesselMap.get(vesselId),
				                                                                 berthMap.get(scheduleMap.get(vesselId).getBerth()));
				                       if (!inf.isValid()) infeasibilityMap.put(vesselId, inf);
			                       }

			                       return Response.ok(infeasibilityMap).build();
		                       }
			, Permission.terminal(id));
	}

	@Path("/unscheduled")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUnscheduledVessels(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> {
			List<Vessel> vessels = VesselDao.getUnscheduledVesselsByTerminal(id, TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
			                                                                 TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME));
			Map<Integer, Vessel> unscheduled = new HashMap<>();
			for (Vessel v : vessels) unscheduled.put(v.getId(), v);
			return Response.ok(unscheduled).build();
		}, Permission.terminal(id));
	}

	@Path("/berths")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBerths(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(BerthDao.getBerthsByTerminal(id)).build(), Permission.terminal(id));
	}


	@Path("/schedules/optimise")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOptimalSchedules(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to,
	                                    @QueryParam("unscheduled") String unscheduledString, @QueryParam("manual") String manualString) {
		return TokenUtil.check(request, (requestEmployee) -> {
			OptimiseSchedule.optimisePlanning(
				TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
				TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME),
				id,
				Boolean.parseBoolean(unscheduledString),
				Boolean.parseBoolean(manualString),
				requestEmployee.getEmail()
			);
			return Response.noContent().build();
		}, Permission.terminal(id));


	}

	@Path("/performance")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPerformance(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Timestamp fromTime = TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME);
			Timestamp toTime = TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME);
			Map<Integer, Vessel> vesselMap = VesselDao.getVesselsByTerminal(id, fromTime, toTime);
			int unscheduledVessels = VesselDao.getCountUnscheduledVesselsByTerminal(id, fromTime, toTime);
			int scheduledVessels = vesselMap.size() - unscheduledVessels;

			double totalCost = 0;
			for (int vessel : vesselMap.keySet()) {
				Schedule schedule = ScheduleDao.getScheduleByVessel(vessel);
				if (schedule == null) continue;
				double hours = (schedule.getExpectedEnd().getTime() - vesselMap.get(vessel).getArrival().getTime()) / 3600000.0;
				totalCost += hours * vesselMap.get(vessel).getCostPerHour();
			}

			return Response.ok(new Performance(totalCost, scheduledVessels, unscheduledVessels)).build();
		}, Permission.terminal(id));
	}

	@Path("/vessels")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVessels(@Context HttpServletRequest request, @QueryParam("deadline_after") String deadline_after,
	                           @QueryParam("arrival_before") String arrival_before) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(VesselDao.getVesselsByTerminal(
			id,
			TimestampAdapter.unadaptOrElse(deadline_after, GenericDao.MIN_TIME),
			TimestampAdapter.unadaptOrElse(arrival_before, GenericDao.MAX_TIME)
		)).build(), Permission.terminal(id));
	}

	@Path("/changes")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChanges(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Timestamp fromTime = TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME);
			Timestamp toTime = TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME);
			return Response.status(Response.Status.OK).entity(ChangeDao.getChangesByTerminal(id, fromTime, toTime)).build();
		}, Permission.terminal(id));
	}
}
