package de.tinf15b4.ihatestau.camera.api;

import java.io.IOException;

import de.tinf15b4.ihatestau.persistence.CameraSpotConfigEntity;
import de.tinf15b4.ihatestau.rest.exceptions.NoDataCachedException;

public interface CameraSpotTrafficEvaluationService {
	byte[] getImageFront(CameraSpotConfigEntity c) throws NoDataCachedException;

	byte[] getImageBack(CameraSpotConfigEntity c) throws NoDataCachedException;

	float getJamProbabilityRaw(CameraSpotConfigEntity c);
	float getJamProbabilitySmooth(CameraSpotConfigEntity e);

	void processImage(CameraSpotConfigEntity spotEntity, byte[] imageFront, byte[] imageBack) throws IOException;
}
