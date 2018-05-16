package de.tinf15b4.ihatestau.rest.services;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import de.tinf15b4.ihatestau.camera.api.CameraSpotTrafficEvaluationService;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfigEntity;
import de.tinf15b4.ihatestau.persistence.PersistenceBean;
import de.tinf15b4.ihatestau.rest.exceptions.NoDataCachedException;
import de.tinf15b4.ihatestau.util.ExceptionUtil;

@Path("/info")
public class InfoService {

	@Inject
	private CameraSpotTrafficEvaluationService evaluationService;

	@Inject
	private PersistenceBean persistenceBean;

	private byte[] defaultImage;
	public InfoService() {
		try {
			defaultImage = IOUtils.toByteArray(InfoService.class.getResource("no-image.png"));
		} catch (IOException e) {
			ExceptionUtil.throwUnchecked(e);
		}
	}

	@GET
	@Path("/html")
	@Produces(MediaType.TEXT_HTML)
	public Response htmlSummary() {
		StringBuilder response = new StringBuilder();

		response.append("<!DOCTYPE html>");
		response.append("<meta http-equiv=Refresh content=30>");
		response.append("<title>iHateStau summary</title>");

		List<CameraSpotConfigEntity> spots = persistenceBean.selectAll(CameraSpotConfigEntity.class);
		spots.sort(Comparator.comparing(CameraSpotConfigEntity::getName));

		for (CameraSpotConfigEntity spot : spots) {
			response.append(String.format("<h1>%s</h1>", spot.getName()));
			response.append("<table border><tr>");
			response.append(String.format("<td><a href=%s/frontimg><img src=%s/frontimg width=320 height=240></a>",
					spot.getId(), spot.getId()));
			response.append(String.format("<td><a href=%s/backimg><img src=%s/backimg width=320 height=240></a>",
					spot.getId(), spot.getId()));

			float jam = evaluationService.getJamProbabilitySmooth(spot);
			float jamRaw = evaluationService.getJamProbabilityRaw(spot);
			String probabilityText = String.format("Jam Probability: %f<br><small>Raw: %f</small>", jam, jamRaw);

			if (jam > 0.5)
				response.append(
						String.format("<td style='color: white; background-color: red;'>%s", probabilityText));
			else
				response.append(
						String.format("<td style='color: black; background-color: green;'>%s", probabilityText));

			response.append("</table>");
		}

		return Response.ok(response.toString()).build();
	}

	@GET
	@Path("/{spot}/frontimg")
	@Produces("image/png")
	public Response frontImage(@PathParam("spot") String spot) {
		try {
			return Response.ok(evaluationService.getImageFront(persistenceBean.selectById(CameraSpotConfigEntity.class, spot))).build();
		} catch (NoDataCachedException e) {
			return Response.ok(defaultImage).build();
		}
	}

	@GET
	@Path("/{spot}/backimg")
	@Produces("image/png")
	public Response backImage(@PathParam("spot") String spot) {
		try {
			return Response.ok(evaluationService.getImageBack(persistenceBean.selectById(CameraSpotConfigEntity.class, spot))).build();
		} catch (NoDataCachedException e) {
			return Response.ok(defaultImage).build();
		}
	}
}
