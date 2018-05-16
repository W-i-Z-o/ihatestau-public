package de.tinf15b4.ihatestau.kamerascraper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.tinf15b4.ihatestau.kamerascraper.downloader.CameraDownloaderRunnable;

public class CameraScraperApp {
	public static void main(String[] args) {
		CameraScraperApp app = new CameraScraperApp();
		app.start();
	}

	private void start() {
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
		executorService.scheduleAtFixedRate(new CameraDownloaderRunnable(), 0L, 1L, TimeUnit.MINUTES);
	}
}
