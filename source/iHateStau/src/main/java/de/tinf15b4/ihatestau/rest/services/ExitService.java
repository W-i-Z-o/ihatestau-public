package de.tinf15b4.ihatestau.rest.services;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfigEntity;
import de.tinf15b4.ihatestau.persistence.PersistenceBean;
import de.tinf15b4.ihatestau.persistence.StreetEntity;

@Path("exit")
public class ExitService {
	@Inject
	private PersistenceBean persistenceBean;

	@GET
	@Path("{id}")
	@Produces("application/json")
	public ExitSpotConfig getExitById(@PathParam("id") String id) {
		ExitSpotConfigEntity e = persistenceBean.selectById(ExitSpotConfigEntity.class, id);
		if (e == null)
			throw new WebApplicationException(404);

		return e.toDTO();
	}

	@GET
	@Produces("application/json")
	public List<ExitSpotConfig> getAllExits() {
		List<ExitSpotConfig> r = new ArrayList<>();
		for (ExitSpotConfigEntity e : persistenceBean.selectAll(ExitSpotConfigEntity.class)) {
			r.add(e.toDTO());
		}

		return r;
	}

	@POST
	@Consumes("application/json")
	@RolesAllowed("admin")
	public void addExit(ExitSpotConfig c) {
		ExitSpotConfigEntity e = new ExitSpotConfigEntity();
		e.setId(c.getId());
		e.setName(c.getName());
		e.setExitLat(c.getExitLat());
		e.setExitLon(c.getExitLon());

		StreetEntity se = persistenceBean.selectById(StreetEntity.class, c.getStreet());
		if (se == null)
			throw new WebApplicationException(
					Response.status(400).entity("Street '" + c.getStreet() + "' not found").build());

		e.setStreet(se);

		persistenceBean.merge(e);
	}
}
