package de.tinf15b4.ihatestau.image;

public class PrepareImageRequest {

	private byte[] image;

	private byte[] mask;

	/**
	 * <b>THIS IS NOT MEANT TO BE USED</b>
	 */
	public PrepareImageRequest() {
		// needed for serialization
	}

	public PrepareImageRequest(byte[] image, byte[] mask) {
		super();
		this.image = image;
		this.mask = mask;
	}

	public byte[] getImage() {
		return image;
	}

	public byte[] getMask() {
		return mask;
	}

}
