package de.tinf15b4.ihatestau.camera;

import java.io.IOException;

import javax.ws.rs.core.GenericType;

import de.tinf15b4.ihatestau.camera.api.CameraImageMasker;
import de.tinf15b4.ihatestau.image.PrepareImageRequest;
import de.tinf15b4.ihatestau.rest.client.RestClient;
import de.tinf15b4.ihatestau.rest.client.RestResponse;

public class CameraImageMaskerImpl implements CameraImageMasker {

	private static String getPreparationApiUrl() {
		return System.getProperty("ihatestau.imagepreparationurl", "http://localhost:8741/ihatestau");
	}

	private RestClient client = new RestClient(getPreparationApiUrl());

	@Override
	public byte[] applyMask(byte[] cameraImage, byte[] maskImage) throws IOException {
		RestResponse<byte[]> res = client.postJson("/imagepreparation/", new PrepareImageRequest(cameraImage, maskImage),
				new GenericType<byte[]>() {
				});

		if (res.hasError()) {
			throw new IOException("Failed to mask camera image: " + res.getErrorBody());
		}

		return res.getBody();
	}
}
