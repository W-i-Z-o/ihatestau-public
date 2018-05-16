package de.tinf15b4.ihatestau.image.services;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tinf15b4.ihatestau.image.ImagePreparator;
import de.tinf15b4.ihatestau.image.PrepareImageRequest;

@Path("/imagepreparation")
public class ImagePreparationService {

	private static final Logger logger = LoggerFactory.getLogger(ImagePreparationService.class);

	@Inject
	private ImagePreparator imagePreparator;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("image/png")
	public Response postImage(PrepareImageRequest request) throws IOException {
		try {
			byte[] image = imagePreparator.prepare(request.getImage(), request.getMask());
			return Response.ok(image).build();
		} catch (IOException e) {
			logger.error("Masking image failed. Mask does not exist", e);
			return Response.status(422).entity("Mask does not exist").build();
		}
	}
}
