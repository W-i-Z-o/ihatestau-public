package de.tinf15b4.ihatestau.camera.api;

import java.io.IOException;

public interface CameraImageMasker {
	byte[] applyMask(byte[] cameraImage, byte[] maskImage) throws IOException;
}
