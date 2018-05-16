package de.tinf15b4.ihatestau.rest.client;

public class RestResponse<T> {

	private T body;
	private int status;
	private String errorBody;

	public RestResponse(int status, T body, String errorBody) {
		this.status = status;
		this.body = body;
		this.errorBody = errorBody;
	}

	public T getBody() {
		return body;
	}

	public String getErrorBody() {
		return errorBody;
	}

	public boolean hasError() {
		return status < 200 || status >= 300;
	}

	public int getStatus() {
		return status;
	}

}
