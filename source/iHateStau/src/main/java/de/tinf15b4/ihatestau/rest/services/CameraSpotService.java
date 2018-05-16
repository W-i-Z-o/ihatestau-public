package de.tinf15b4.ihatestau.rest.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfigEntity;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfigEntity;
import de.tinf15b4.ihatestau.persistence.PersistenceBean;
import de.tinf15b4.ihatestau.persistence.StreetEntity;

@Path("/spots")
public class CameraSpotService {

	@Inject
	private PersistenceBean persistenceBean;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllSpots() {
		List<CameraSpotConfig> spots = persistenceBean.selectAll(CameraSpotConfigEntity.class)//
				.stream()//
				.map(CameraSpotConfigEntity::toDTO)//
				.collect(Collectors.toList());
		return Response.ok(spots).build();
	}

	@GET
	@Path("{id}")
	@Produces("application/json")
	public CameraSpotConfig getById(@PathParam("id") String id) {
		CameraSpotConfigEntity e = persistenceBean.selectById(CameraSpotConfigEntity.class, id);
		if (e == null)
			throw new WebApplicationException(404);

		return e.toDTO();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("admin")
	public CameraSpotConfig addSpot(CameraSpotConfig config) {
		CameraSpotConfigEntity e = new CameraSpotConfigEntity();
		e.setId(config.getId());
		e.setCameraLat(config.getCameraLat());
		e.setCameraLon(config.getCameraLon());
		e.setCameraNameBack(config.getCameraNameBack());
		e.setCameraNameFront(config.getCameraNameFront());
		e.setName(config.getName());
		e.setMaskBack(config.getMaskBack());
		e.setMaskFront(config.getMaskFront());

		StreetEntity se = persistenceBean.selectById(StreetEntity.class, config.getStreet());
		if (se == null)
			throw new WebApplicationException(
					Response.status(400).entity("Street '" + config.getStreet() + "' not found.").build());

		e.setStreet(se);

		Set<ExitSpotConfigEntity> sesce = new HashSet<>();
		for (String eid : config.getLastAlternatives()) {
			ExitSpotConfigEntity esce = persistenceBean.selectById(ExitSpotConfigEntity.class, eid);
			if (esce == null)
				throw new WebApplicationException(Response.status(400).entity("Exit '" + eid + "' not found.").build());

			sesce.add(esce);
		}

		e.setLastAlternativeExits(sesce);

		CameraSpotConfigEntity sc = persistenceBean.selectById(CameraSpotConfigEntity.class, config.getSisterId());
		if (sc == null)
			throw new WebApplicationException(
					Response.status(400).entity("Sister cmaera '" + config.getSisterId() + "' not found.").build());

		e.setSister(sc);
		e = persistenceBean.merge(e);

		return e.toDTO();
	}

}
