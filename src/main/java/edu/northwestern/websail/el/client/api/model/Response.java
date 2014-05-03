package edu.northwestern.websail.el.client.api.model;

public class Response<T> {
	private Integer status;
	private T response;
	private String message;
	
	public Response(Integer status, T response, String message) {
		this.status = status;
		this.response = response;
		this.message = message;
	}
	
	public Integer getStatus() {
		return status;
	}
	public T getResponse() {
		return response;
	}
	public String getMessage() {
		return message;
	}
	
}
