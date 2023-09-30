package nl.utwente.di.visol1.resources;

import javax.servlet.http.HttpServletRequest;
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
import nl.utwente.di.visol1.models.Role;
import nl.utwente.di.visol1.type_adapters.TimestampAdapter;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

@Path("/changes/schedules")
public class ScheduleChangesResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScheduleChanges(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(ScheduleChangeDao.getScheduleChanges(
			TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
			TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME)
		)).build(), Permission.role(Role.RESEARCHER));
	}

	@Path("{vessel_id}")
	public ScheduleChangeResource getScheduleChange(@PathParam("vessel_id") String id) {
		return new ScheduleChangeResource(uriInfo, request, id);
	}
}
