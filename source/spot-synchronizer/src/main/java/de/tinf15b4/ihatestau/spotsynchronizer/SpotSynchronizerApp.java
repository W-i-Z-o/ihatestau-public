package de.tinf15b4.ihatestau.spotsynchronizer;

import java.io.IOException;

import javax.ws.rs.client.Entity;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.persistence.ExitSpotConfig;
import de.tinf15b4.ihatestau.persistence.Street;
import de.tinf15b4.ihatestau.rest.client.RestClient;
import de.tinf15b4.ihatestau.rest.client.RestResponse;

public class SpotSynchronizerApp {
	private static final Logger logger = LoggerFactory.getLogger(SpotSynchronizerApp.class);

	private static String getApiUrl() {
		return System.getProperty("ihatestau.apiurl", "http://localhost:8080/ihatestau");
	}

	private static String getUsername() {
		return System.getProperty("ihatestau.username", System.getenv("IHATESTAU_USERNAME"));
	}

	private static String getPassword() {
		return System.getProperty("ihatestau.password", System.getenv("IHATESTAU_PASSWORD"));
	}

	public static void main(String[] args) throws IOException {
		SpotSynchronizerApp app = new SpotSynchronizerApp();
		app.start();
	}

	private void start() throws IOException {
		RestClient client = new RestClient(getApiUrl(), getUsername(), getPassword());

		Street[] streets = new ObjectMapper().readValue(getClass().getResource("streets.json"), Street[].class);
		for (Street s : streets) {
			RestResponse<?> response = client.postJson("/street", s, null);
			if (response.hasError())
				logger.error("HTTP " + response.getStatus() + ": " + response.getErrorBody());
		}

		ExitSpotConfig[] exits = new ObjectMapper().readValue(getClass().getResource("exits.json"), ExitSpotConfig[].class);
		for (ExitSpotConfig e : exits) {
			RestResponse<?> response = client.postJson("/exit", e, null);
			if (response.hasError())
				logger.error("HTTP " + response.getStatus() + ": " + response.getErrorBody());
		}

		// HACK! need to upload spots without sisters first
		CameraSpotConfig[] spots = new ObjectMapper().readValue(getClass().getResource("spots.json"),
				CameraSpotConfig[].class);
		for (CameraSpotConfig e : spots) {
			e.setSisterId(null);
			RestResponse<?> response = client.postJson("/spots", e, null);
			if (response.hasError())
				logger.error("HTTP " + response.getStatus() + ": " + response.getErrorBody());
		}

		spots = new ObjectMapper().readValue(getClass().getResource("spots.json"),
				CameraSpotConfig[].class);
		for (CameraSpotConfig e : spots) {
			RestResponse<?> response = client.postJson("/spots", e, null);
			if (response.hasError())
				logger.error("HTTP " + response.getStatus() + ": " + response.getErrorBody());

			if (e.getMaskBack() != null) {
				byte[] i = IOUtils.toByteArray(getClass().getResource(e.getMaskBack() + ".png"));
				RestResponse<?> r = client.postRaw("/mask/" + e.getMaskBack(), Entity.entity(i, "image/png"), null);
				if (r.hasError())
					logger.error("HTTP " + r.getStatus() + ": " + r.getErrorBody());
			}
			if (e.getMaskFront() != null) {
				byte[] i = IOUtils.toByteArray(getClass().getResource(e.getMaskFront() + ".png"));
				RestResponse<?> r = client.postRaw("/mask/" + e.getMaskFront(), Entity.entity(i, "image/png"), null);
				if (r.hasError())
					logger.error("HTTP " + r.getStatus() + ": " + r.getErrorBody());
			}
		}

	}
}
