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
import nl.utwente.di.visol1.dao.VesselChangeDao;
import nl.utwente.di.visol1.models.VesselChange;
import nl.utwente.di.visol1.type_adapters.TimestampAdapter;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

public class VesselChangeResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	int vessel;

	public VesselChangeResource(UriInfo uriInfo, Request request, String vessel) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.vessel = Integer.parseInt(vessel);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVesselChanges(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(VesselChangeDao.getVesselChangesByVessel(
			vessel,
			TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
			TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME)
		)).build(), Permission.vessel(vessel));
	}

	@DELETE
	public Response deleteVesselChanges(@Context HttpServletRequest request, @QueryParam("from") String from, @QueryParam("to") String to) {
		return TokenUtil.check(request, (requestEmployee) -> (
			VesselChangeDao.deleteVesselChanges(
				vessel,
				TimestampAdapter.unadaptOrElse(from, GenericDao.MIN_TIME),
				TimestampAdapter.unadaptOrElse(to, GenericDao.MAX_TIME)
			) > 0 ? Response.noContent() : Response.status(Response.Status.NOT_FOUND)
		).build(), Permission.vessel(vessel));
	}

	@Path("{date}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVesselChangeByDate(@Context HttpServletRequest request, @PathParam("date") String date) {
		return TokenUtil.check(request, (requestEmployee) -> {
			VesselChange vesselChange = VesselChangeDao.getVesselChangeByDate(vessel, TimestampAdapter.unadapt(date));
			return (vesselChange != null
			        ? Response.ok(vesselChange)
			        : Response.status(Response.Status.NOT_FOUND)
			).build();
		}, Permission.vessel(vessel));
	}

	@Path("{date}")
	@DELETE
	public Response deleteVesselChangeByDate(@Context HttpServletRequest request, @PathParam("date") String date) {
		return TokenUtil.check(request, (requestEmployee) -> (
			VesselChangeDao.deleteVesselChangeByDate(vessel, TimestampAdapter.unadapt(date)) > 0
			? Response.noContent()
			: Response.status(Response.Status.NOT_FOUND)
		).build(), Permission.vessel(vessel));
	}
}
