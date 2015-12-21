package fp.forevo.manager;

import java.awt.image.BufferedImage;

public class TafException extends Exception {

	private static final long serialVersionUID = 8136405078104165055L;

	public TafException(String message, BufferedImage image) {
		super(message);
	}

	public TafException(String message) {
		super(message);
	}

}