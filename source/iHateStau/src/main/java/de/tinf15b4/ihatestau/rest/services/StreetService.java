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

import de.tinf15b4.ihatestau.persistence.PersistenceBean;
import de.tinf15b4.ihatestau.persistence.Street;
import de.tinf15b4.ihatestau.persistence.StreetEntity;

@Path("street")
public class StreetService {
	@Inject
	private PersistenceBean persistenceBean;

	@Path("{id}")
	@GET
	@Produces("application/json")
	public Street getById(@PathParam("id") String id) {
		StreetEntity e = persistenceBean.selectById(StreetEntity.class, id);
		if (e == null)
			throw new WebApplicationException(404);

		Street r = new Street();
		r.setId(e.getId());
		r.setName(e.getName());

		return r;
	}

	@POST
	@Consumes("application/json")
	@RolesAllowed("admin")
	public void addStreet(Street s) {
		StreetEntity e = new StreetEntity();

		e.setId(s.getId());
		e.setName(s.getName());

		persistenceBean.merge(e);
	}

	@GET
	@Produces("application/json")
	public List<Street> getAllStreets() {
		List<Street> r = new ArrayList<>();

		for (StreetEntity e : persistenceBean.selectAll(StreetEntity.class)) {
			Street s = new Street();
			s.setId(e.getId());
			s.setName(e.getName());
			r.add(s);
		}

		return r;
	}
}
