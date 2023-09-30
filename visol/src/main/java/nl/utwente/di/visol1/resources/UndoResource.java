package nl.utwente.di.visol1.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.utwente.di.visol1.dao.ChangeDao;
import nl.utwente.di.visol1.util.TokenUtil;

@Path("/changes")
public class UndoResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@POST
	@Path("/undo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response undoLastChange(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> {
			if (requestEmployee == null) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			int i = ChangeDao.undoLastChange(requestEmployee.getEmail());
			if (i == -1) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			} else if (i == 0) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.status(Response.Status.NO_CONTENT).build();
			}
		});
	}

	@POST
	@Path("/redo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response redoLastChange(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> {
			if (requestEmployee == null) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
			int i = ChangeDao.redoLastChange(requestEmployee.getEmail());
			if (i == -1) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			} else if (i == 0) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.status(Response.Status.NO_CONTENT).build();
			}

		});
	}
}
