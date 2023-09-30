package nl.utwente.di.visol1.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.Timestamp;
import java.util.List;

import nl.utwente.di.visol1.dao.VesselChangeDao;
import nl.utwente.di.visol1.dao.VesselDao;
import nl.utwente.di.visol1.models.Vessel;
import nl.utwente.di.visol1.models.VesselChange;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

@Path("/vessels")
public class VesselsResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response createVessel(@Context HttpServletRequest request, Vessel vessel) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Vessel createdVessel = VesselDao.createVessel(vessel);
			if (createdVessel != null) {
				if (requestEmployee != null) {
					VesselChangeDao.createVesselChange(new VesselChange(
						requestEmployee.getEmail(), request.getHeader("Reason"), null, createdVessel
					));
				} else {
					VesselChangeDao.createVesselChange(new VesselChange(
						null, request.getHeader("Reason"), null, createdVessel
					));
				}
				return Response.created(URI.create("vessels/" + createdVessel.getId())).entity(createdVessel).build();
			} else {
				return Response.notAcceptable(List.of()).build();
			}
		}, Permission.terminal(vessel.getDestination()));
	}

	@Path("{vessel_id}")
	public VesselResource getVessel(@PathParam("vessel_id") String id) {
		return new VesselResource(uriInfo, request, id);
	}
}
