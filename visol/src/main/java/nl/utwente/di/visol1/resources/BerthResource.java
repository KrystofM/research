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
import java.util.List;

import nl.utwente.di.visol1.dao.BerthDao;
import nl.utwente.di.visol1.dao.GenericDao;
import nl.utwente.di.visol1.dao.ScheduleDao;
import nl.utwente.di.visol1.models.Berth;
import nl.utwente.di.visol1.type_adapters.TimestampAdapter;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

public class BerthResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	int id;

	public BerthResource(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = Integer.parseInt(id);
	}

	@DELETE
	public Response deleteBerth(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> BerthDao.deleteBerth(id) > 0
		                                                     ? Response.noContent().build()
		                                                     : Response.status(Response.Status.NOT_FOUND).build(),
		                       Permission.berth(id));
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response replaceBerth(@Context HttpServletRequest request, Berth berth) {
		return TokenUtil.check(request, (requestEmployee) -> {
			int i = BerthDao.replaceBerth(id, berth);
			if (i == -1) {
				return Response.notAcceptable(List.of()).build();
			} else if (i == 0) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.ok(BerthDao.getBerth(id)).build();
			}
		}, Permission.berth(berth));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBerth(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Berth berth = BerthDao.getBerth(id);
			if (berth == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.ok(berth).build();
			}
		}, Permission.berth(id));
	}

	/**
	 * @return return the schedule of the berth in a specific timeframe
	 */
	@Path("/schedules")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSchedules(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(ScheduleDao.getSchedulesByBerth(
			id,
			TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
			TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME)
		)).build(), Permission.berth(id));
	}
}
