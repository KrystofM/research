package nl.utwente.di.visol1.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nl.utwente.di.visol1.dao.GenericDao;
import nl.utwente.di.visol1.dao.PortDao;
import nl.utwente.di.visol1.dao.ScheduleDao;
import nl.utwente.di.visol1.dao.TerminalDao;
import nl.utwente.di.visol1.dao.VesselDao;
import nl.utwente.di.visol1.models.Performance;
import nl.utwente.di.visol1.models.Port;
import nl.utwente.di.visol1.models.Schedule;
import nl.utwente.di.visol1.models.Terminal;
import nl.utwente.di.visol1.models.Vessel;
import nl.utwente.di.visol1.type_adapters.TimestampAdapter;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

public class PortResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	int id;

	public PortResource(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = Integer.parseInt(id);
	}

	@DELETE
	public Response deletePort(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> PortDao.deletePort(id) > 0
		                                                     ? Response.noContent().build()
		                                                     : Response.status(Response.Status.NOT_FOUND).build(),
		                       Permission.port(id));
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response replacePort(@Context HttpServletRequest request, Port port) {
		return TokenUtil.check(request, (requestEmployee) -> {
			int i = PortDao.replacePort(id, port);
			if (i == -1) {
				return Response.notAcceptable(List.of()).build();
			} else if (i == 0) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.ok(PortDao.getPort(id)).build();
			}
		}, Permission.port(id));

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPort(@Context HttpServletRequest request) {
		return TokenUtil.check(
			request,
			(requestEmployee) -> Optional.ofNullable(PortDao.getPort(id)).map(Response::ok)
				.orElse(Response.status(Response.Status.NOT_FOUND)).build(),
			Permission.port(id)
		);
	}

	@Path("/schedules")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSchedules(@Context HttpServletRequest request, @QueryParam("from") String
		from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(ScheduleDao.getSchedulesByPort(
			id,
			TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
			TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME)
		)).build(), Permission.port(id));
	}

	@Path("/export")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response exportPort(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> Optional.ofNullable(PortDao.exportPort(
			id,
			TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
			TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME)
		)).map(Response::ok).orElse(Response.status(Response.Status.NOT_FOUND)).build(), Permission.port(id));
	}

	@Path("/terminals")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTerminals(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(TerminalDao.getTerminalsByPort(id)).build(), Permission.port(id));
	}

	@Path("/performance")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPerformance(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Timestamp fromTime = TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME);
			Timestamp toTime = TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME);
			Map<Integer, Terminal> terminalMap = TerminalDao.getTerminalsByPort(id);
			int unscheduledVessels = 0;
			int scheduledVessels = 0;
			double totalCost = 0;
			for (int terminal : terminalMap.keySet()) {
				Map<Integer, Vessel> vesselMap = VesselDao.getVesselsByTerminal(terminal, fromTime, toTime);
				unscheduledVessels += VesselDao.getCountUnscheduledVesselsByTerminal(terminal, fromTime, toTime);
				scheduledVessels += vesselMap.size() - unscheduledVessels;

				totalCost = 0;
				for (int vessel : vesselMap.keySet()) {
					Schedule schedule = ScheduleDao.getScheduleByVessel(vessel);
					if (schedule == null) continue;
					double hours = (schedule.getExpectedEnd().getTime() - vesselMap.get(vessel).getArrival().getTime()) / 3600000.0;
					totalCost += hours * vesselMap.get(vessel).getCostPerHour();
				}
			}

			return Response.ok(new Performance(totalCost, scheduledVessels, unscheduledVessels)).build();
		}, Permission.port(id));
	}
}
