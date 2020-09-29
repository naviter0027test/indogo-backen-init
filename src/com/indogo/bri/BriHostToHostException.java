package com.indogo.bri;

public class BriHostToHostException extends Exception {
	
	private String statusCode;

	public BriHostToHostException(String statusCode, String message) {
		super(message);
		
		this.statusCode = statusCode;
	}
	
	public String getStatusCode() {
		return statusCode;
	}
	
}
