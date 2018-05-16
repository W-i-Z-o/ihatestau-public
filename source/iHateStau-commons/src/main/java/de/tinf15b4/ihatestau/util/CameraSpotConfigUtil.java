package de.tinf15b4.ihatestau.util;

import java.util.List;

import javax.ws.rs.core.GenericType;

import de.tinf15b4.ihatestau.persistence.CameraSpotConfig;
import de.tinf15b4.ihatestau.rest.client.RestClient;
import de.tinf15b4.ihatestau.rest.exceptions.UnknownSpotException;

public class CameraSpotConfigUtil {

	private CameraSpotConfigUtil() {
	}

	private static String getApiUrl() {
		return System.getProperty("ihatestau.apiurl", "http://localhost:8080/ihatestau");
	}

	public static List<CameraSpotConfig> getAllSpots() {
		RestClient client = new RestClient(getApiUrl());
		return client.get("/spots", new GenericType<List<CameraSpotConfig>>() {
		}).getBody();
	}

	public static List<CameraSpotConfig> getAllSpotsSorted() {
		List<CameraSpotConfig> list = getAllSpots();
		list.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
		return list;
	}

	public static CameraSpotConfig getSpotForId(String spotId) throws UnknownSpotException {
		return getAllSpots().stream()//
				.filter(s -> s.getId().equals(spotId))//
				.findFirst()//
				.orElseThrow(() -> new UnknownSpotException("Spot " + spotId + " does not exist"));
	}
}
