package de.tinf15b4.ihatestau.camera;

public class ProcessImageRequest {

	private byte[] imageFront;

	private byte[] imageBack;

	/**
	 * <b>THIS IS NOT MEANT TO BE USED</b>
	 */
	public ProcessImageRequest() {
		// needed for serialization
	}

	public ProcessImageRequest(byte[] imageFront, byte[] imageBack) {
		super();
		this.imageFront = imageFront;
		this.imageBack = imageBack;
	}

	public byte[] getImageBack() {
		return imageBack;
	}

	public byte[] getImageFront() {
		return imageFront;
	}

}
