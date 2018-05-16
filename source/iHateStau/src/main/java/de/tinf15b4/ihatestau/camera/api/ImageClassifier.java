package de.tinf15b4.ihatestau.camera.api;

public interface ImageClassifier {
	float getJamProbability(byte[] imageA, byte[] imageB);
}
