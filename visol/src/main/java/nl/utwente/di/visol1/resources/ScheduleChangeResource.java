package nl.utwente.di.visol1.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.utwente.di.visol1.dao.GenericDao;
import nl.utwente.di.visol1.dao.ScheduleChangeDao;
import nl.utwente.di.visol1.type_adapters.TimestampAdapter;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

public class ScheduleChangeResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	int vessel;

	public ScheduleChangeResource(UriInfo uriInfo, Request request, String vessel) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.vessel = Integer.parseInt(vessel);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScheduleChanges(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(ScheduleChangeDao.getScheduleChangesByVessel(
			vessel,
			TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
			TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME)
		)).build(), Permission.schedule(vessel));
	}

	@DELETE
	public Response deleteScheduleChanges(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> ScheduleChangeDao.deleteScheduleChanges(
			vessel,
			TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
			TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME)
		) > 0 ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build(), Permission.schedule(vessel));
	}

	@Path("{date}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScheduleChangeByDate(@Context HttpServletRequest request, @PathParam("date") String date) {
		return TokenUtil.check(
			request,
			(requestEmployee) -> ScheduleChangeDao.getScheduleChangeByDate(vessel, TimestampAdapter.unadapt(date)) != null
			                     ? Response.ok(ScheduleChangeDao.getScheduleChangeByDate(vessel, TimestampAdapter.unadapt(date))).build()
			                     : Response.status(Response.Status.NOT_FOUND).build(),
			Permission.schedule(vessel)
		);
	}

	@Path("{date}")
	@DELETE
	public Response deleteScheduleChangeByDate(@Context HttpServletRequest request, @PathParam("date") String date) {
		return TokenUtil.check(
			request,
			(requestEmployee) -> ScheduleChangeDao.deleteScheduleChangeByDate(vessel, TimestampAdapter.unadapt(date)) > 0
			                     ? Response.noContent().build()
			                     : Response.status(Response.Status.NOT_FOUND).build(),
			Permission.schedule(vessel)
		);
	}
}
