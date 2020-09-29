package com.lionpig.webui.http;

public class FunctionException extends Exception {
	private static final long serialVersionUID = 4989270401914104786L;
	
	private int errorCode;
	
	public FunctionException(int errorCode, String msg) {
		super(msg);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
