package de.tinf15b4.ihatestau.rest.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

	private static final Logger logger = LoggerFactory.getLogger(BusinessExceptionMapper.class);

	@Override
	public Response toResponse(BusinessException e) {
		logger.error(e.getMessage());
		int code = 500;

		if (e instanceof UnknownSpotException) {
			code = 422;
		}
		if (e instanceof UnauthorizedException) {
			code = 403;
		}
		return Response.status(code)//
				.entity(e.getMessage())//
				.type(MediaType.TEXT_PLAIN)//
				.build();
	}

}
