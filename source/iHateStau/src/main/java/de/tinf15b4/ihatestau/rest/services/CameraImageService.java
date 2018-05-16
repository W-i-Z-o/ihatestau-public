package de.tinf15b4.ihatestau.rest.services;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.tinf15b4.ihatestau.camera.ProcessImageRequest;
import de.tinf15b4.ihatestau.camera.api.CameraSpotTrafficEvaluationService;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfigEntity;
import de.tinf15b4.ihatestau.persistence.PersistenceBean;
import de.tinf15b4.ihatestau.util.CameraSpotConfigUtil;

@Path("/images")
public class CameraImageService {

	@Inject
	private CameraSpotTrafficEvaluationService evaluationService;

	@Inject
	private PersistenceBean persistenceBean;

	@POST
	@Path("/{spot}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postImages(@PathParam("spot") String spotId, ProcessImageRequest request) throws IOException {
		CameraSpotConfigEntity spotEntity = persistenceBean.selectById(CameraSpotConfigEntity.class, spotId);
		evaluationService.processImage(spotEntity, request.getImageFront(), request.getImageBack());
		return Response.ok().build();
	}
}
