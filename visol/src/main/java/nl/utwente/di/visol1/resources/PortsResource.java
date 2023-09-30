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
import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.utwente.di.visol1.dao.PortDao;
import nl.utwente.di.visol1.models.Port;
import nl.utwente.di.visol1.models.Role;
import nl.utwente.di.visol1.util.Permission;
import nl.utwente.di.visol1.util.TokenUtil;

@Path("/ports")
public class PortsResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response createPort(@Context HttpServletRequest request, Port port) {
		return TokenUtil.check(request, (requestEmployee) -> {
			Port createdPort = PortDao.createPort(port);
			return (
				createdPort != null
				? Response.created(URI.create("ports/" + createdPort.getId())).entity(createdPort)
				: Response.notAcceptable(List.of())
			).build();
		}, Permission.role(Role.RESEARCHER));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPorts(@Context HttpServletRequest request) {
		return TokenUtil.check(request, (requestEmployee) -> Response.ok(PortDao.getPorts()).build(), Permission.role(Role.RESEARCHER));
	}

	@Path("/import")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response importPort(@Context HttpServletRequest request, String portDump) {
		return TokenUtil.check(request, (requestEmployee) -> {
			try {
				// Parse dump as JSON
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(portDump);

				Port port = PortDao.importPort(rootNode);

				if (port != null) {
					return Response.created(URI.create("ports/" + port.getId())).entity(port).build();
				} else {
					return Response.notAcceptable(List.of()).build();
				}
			} catch (IOException exception) {
				return Response.notAcceptable(List.of()).build();
			}
		}, Permission.role(Role.RESEARCHER));
	}

	@Path("{port_id}")
	public PortResource getPort(@PathParam("port_id") String id) {
		return new PortResource(uriInfo, request, id);
	}
}
