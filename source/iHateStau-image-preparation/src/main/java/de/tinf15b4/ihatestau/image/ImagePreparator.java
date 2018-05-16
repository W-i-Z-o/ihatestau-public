package de.tinf15b4.ihatestau.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;

@ApplicationScoped
public class ImagePreparator {

	public ImagePreparator() {
	}

	public byte[] prepare(byte[] image, byte[] mask) throws IOException {
		BufferedImage maskImage = ImageIO.read(new ByteArrayInputStream(mask));
		BufferedImage imageImage = ImageIO.read(new ByteArrayInputStream(image));
		BufferedImage result = new BufferedImage(imageImage.getWidth(), imageImage.getHeight(), imageImage.getType());
		Graphics2D graphics = result.createGraphics();
		graphics.drawImage(imageImage, 0, 0, null);
		graphics.drawImage(maskImage, 0, 0, null);

		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			ImageIO.write(result, "png", os);
			return os.toByteArray();
		}
	}

}
