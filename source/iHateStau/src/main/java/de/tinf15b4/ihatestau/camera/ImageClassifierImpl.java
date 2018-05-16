package de.tinf15b4.ihatestau.camera;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import de.tinf15b4.ihatestau.camera.api.ImageClassifier;
import de.tinf15b4.ihatestau.rest.client.RestClient;
import de.tinf15b4.ihatestau.rest.client.RestResponse;

public class ImageClassifierImpl implements ImageClassifier {
	private static class ClassifierResult {
		public List<Integer> classes;
		public List<List<Float>> probabilities;
	}

	private static String getClassifierUrl() {
		return System.getProperty("ihatestau.classifierurl", "http://localhost:8742");
	}

	private final RestClient client = new RestClient(getClassifierUrl());

	@Override
	public float getJamProbability(byte[] imageA, byte[] imageB) {
		FormDataMultiPart mp = new FormDataMultiPart();
		mp.bodyPart(new FormDataBodyPart(
				FormDataContentDisposition.name("image-a").fileName("a.png").size(imageA.length).build(), imageA,
				new MediaType("image", "png")));
		mp.bodyPart(new FormDataBodyPart(
				FormDataContentDisposition.name("image-b").fileName("b.png").size(imageB.length).build(), imageB,
				new MediaType("image", "png")));

		RestResponse<ClassifierResult> res = client.postRaw("/classify", Entity.entity(mp, mp.getMediaType()),
				new GenericType<ClassifierResult>() {
				});

		if (res.hasError()) {
			throw new RuntimeException("Failed to classify camera image: " + res.getErrorBody());
		}

		return res.getBody().probabilities.get(0).get(0);
	}
}
