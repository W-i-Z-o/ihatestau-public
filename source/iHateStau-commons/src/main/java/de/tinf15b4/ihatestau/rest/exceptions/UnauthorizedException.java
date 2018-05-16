package de.tinf15b4.ihatestau.rest.exceptions;

public class UnauthorizedException extends BusinessException {

	private static final long serialVersionUID = -8058620483855679953L;

	public UnauthorizedException(String method, String resource) {
		super(String.format("You are not authorized to access resource at %s with method %s", resource, method));
	}

}
