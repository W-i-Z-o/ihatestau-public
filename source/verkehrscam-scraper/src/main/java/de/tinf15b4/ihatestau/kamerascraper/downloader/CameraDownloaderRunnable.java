package de.tinf15b4.ihatestau.kamerascraper.downloader;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tinf15b4.ihatestau.camera.ProcessImageRequest;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.rest.client.RestClient;
import de.tinf15b4.ihatestau.rest.client.RestResponse;
import de.tinf15b4.ihatestau.util.CameraSpotConfigUtil;

public class CameraDownloaderRunnable implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(CameraDownloaderRunnable.class);

	private static String getApiUrl() {
		return System.getProperty("ihatestau.apiurl", "http://localhost:8080/ihatestau");
	}

	@Override
	public void run() {
		// ScheduledExecutorService will murder us if any exception leaks here
		try {
			RestClient client = new RestClient(getApiUrl());
			CameraDownloader downloader = new CameraDownloader();

			List<CameraSpotConfig> spots = CameraSpotConfigUtil.getAllSpots();
			for (CameraSpotConfig spot : spots) {
				try {
					byte[] imageFront = downloader.getImage(spot.getCameraNameFront());
					byte[] imageBack = downloader.getImage(spot.getCameraNameBack());
					ProcessImageRequest req = new ProcessImageRequest(imageFront, imageBack);

					RestResponse<?> response = client.postJson("/images/" + spot.getId(), req, null);
					if (response.hasError())
						logger.error(response.getErrorBody());

				} catch (HttpException | IOException e) {
					logger.error("", e);
				}
			}
		} catch (Throwable t) {
			logger.error("Unexcepted Exception while scraping camera pics: ", t);
		}
	}
}
