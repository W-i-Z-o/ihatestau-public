package de.tinf15b4.ihatestau.kamerascraper.downloader;

import java.time.Instant;

public class DownloadUtil {
    public static String generateJsTimestamp() {
        return ""+Instant.now().toEpochMilli();
    }
}
