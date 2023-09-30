package nl.utwente.di.visol1.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

import nl.utwente.di.visol1.dao.BerthDao;
import nl.utwente.di.visol1.models.Berth;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

@Path("/berths")
public class BerthsResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response createBerth(@Context HttpServletRequest request, Berth berth) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Berth createdBerth = BerthDao.createBerth(berth);
			return (
				createdBerth != null
				? Response.created(URI.create("berths/" + createdBerth.getId())).entity(createdBerth)
				: Response.notAcceptable(List.of())
			).build();
		}, Permission.berth(berth));
	}

	@Path("{berth_id}")
	public BerthResource getBerth(@PathParam("berth_id") String id) {
		return new BerthResource(uriInfo, request, id);
	}
}
