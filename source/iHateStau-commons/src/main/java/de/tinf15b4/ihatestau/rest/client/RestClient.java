package de.tinf15b4.ihatestau.rest.client;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClient {

	private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

	private WebTarget target;

	public RestClient(String basePath, String username, String password) {
		Client c = ClientBuilder.newClient();
		c = c.register(JacksonFeature.class);
		c = c.register(MultiPartFeature.class);

		if (username != null && password != null) {
			c.register(HttpAuthenticationFeature.basic(username, password));
		}

		target = c.target(basePath);
	}

	public RestClient(String basePath) {
		this(basePath, null, null);
	}

	public <T> RestResponse<T> get(String resource, GenericType<T> expectedResult) {
		Response response = target.path(resource)//
				.request()//
				.get();
		return wrapResponse(response, expectedResult);
	}

	public <T> RestResponse<T> postJson(String resource, Object body, GenericType<T> expectedResult) {
		Response response = target.path(resource)//
				.request()//
				.post(Entity.json(body));
		return wrapResponse(response, expectedResult);
	}

	public <T> RestResponse<T> postRaw(String resource, Entity<?> body, GenericType<T> expectedResult) {
		Response response = target.path(resource)//
				.request()//
				.post(body);
		return wrapResponse(response, expectedResult);
	}

	private <T> RestResponse<T> wrapResponse(Response response, GenericType<T> expectedResult) {
		int status = response.getStatus();
		if (status == 200) {
			try {
				response.bufferEntity();
				if (expectedResult == null)
					return new RestResponse<>(status, null, null);
				return new RestResponse<>(status, response.readEntity(expectedResult), null);
			} catch (ProcessingException e) {
				logger.error("Parsing result for rest call failed", e);
				status = 500;
			}
		}
		return new RestResponse<>(status, null, response.readEntity(String.class));
	}

}
