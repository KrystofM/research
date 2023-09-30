package nl.utwente.di.visol1.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import nl.utwente.di.visol1.dao.EmployeeDao;
import nl.utwente.di.visol1.models.Employee;
import nl.utwente.di.visol1.models.EmployeeInput;
import nl.utwente.di.visol1.models.Role;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

@Path("/employees")
public class EmployeesResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response createEmployee(@Context HttpServletRequest request, EmployeeInput employee) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Employee createdEmployee = EmployeeDao.createEmployee(employee.generate());
			return (
				createdEmployee != null
				? Response.created(URI.create("employees/" + createdEmployee.getEmail())).entity(createdEmployee)
				: Response.notAcceptable(List.of())
			).build();
		}, Permission.employee(employee));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEmployees(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(EmployeeDao.getEmployees()).build(), Permission.role(Role.RESEARCHER));
	}


	@Path("{employee_email}")
	public EmployeeResource getPort(@PathParam("employee_email") String email) {
		return new EmployeeResource(uriInfo, request, email);
	}
}
