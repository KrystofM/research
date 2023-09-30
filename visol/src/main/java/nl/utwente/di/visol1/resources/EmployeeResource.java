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
import java.util.Optional;

import nl.utwente.di.visol1.dao.EmployeeDao;
import nl.utwente.di.visol1.models.Employee;
import nl.utwente.di.visol1.models.EmployeeInput;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

public class EmployeeResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	String email;

	public EmployeeResource(UriInfo uriInfo, Request request, String email) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.email = email;
	}

	@DELETE
	public Response deleteEmployee(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> EmployeeDao.deleteEmployee(email) > 0
		                                                     ? Response.noContent().build()
		                                                     : Response.status(Response.Status.NOT_FOUND).build(),
		                       Permission.employee(email));
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response replaceEmployee(@Context HttpServletRequest request, EmployeeInput employee) {
		return TokenUtil.check(request, (requestEmployee) -> {
			int i = EmployeeDao.replaceEmployee(email, employee.generate());
			if (i == -1) {
				return Response.notAcceptable(List.of()).build();
			} else if (i == 0) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.ok(EmployeeDao.getEmployee(email)).build();
			}
		}, Permission.employee(email));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEmployee(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Employee employee = EmployeeDao.getEmployee(email);
			if (employee != null) {
				return Response.ok(employee).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		}, Permission.employee(email));
	}

	// TODO add in API doc

	@Path("/gravatar")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGravatar(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> Optional.ofNullable(
			EmployeeDao.getGravatar(email)
		).map(Response::ok).orElse(Response.status(Response.Status.NOT_FOUND)).build());
	}
}
