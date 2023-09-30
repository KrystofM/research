package nl.utwente.di.visol1.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import nl.utwente.di.visol1.dao.EmployeeDao;
import nl.utwente.di.visol1.models.Employee;
import nl.utwente.di.visol1.util.EncryptionUtil;
import nl.utwente.di.visol1.util.TokenUtil;

@Path("/token")
public class TokenResource {
	@Path("obtain")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response obtain(@Context HttpServletRequest request, LoginRequest login) {
		// First, check if the user exists
		Employee employee = EmployeeDao.getEmployee(login.email);
		if (employee == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		// Then, check if the password is correct
		if (EncryptionUtil.validate(login.password, employee.getKey())) {
			// If so, generate a new token and return it
			String token = TokenUtil.generateToken(employee);
			return Response.ok(JsonNodeFactory.withExactBigDecimals(false).objectNode().put("token", token)).build();
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@Path("verify")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response verify(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(requestEmployee).build());
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class LoginRequest {
		private String email;
		private String password;

		public LoginRequest() {
			// Empty constructor
		}

		public LoginRequest(String email, String password) {
			this.email = email;
			this.password = password;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
}
