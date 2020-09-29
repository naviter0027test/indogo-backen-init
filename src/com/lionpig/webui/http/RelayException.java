package com.lionpig.webui.http;

public class RelayException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3044161909392517359L;
	
	private int code;
	private String stack;
	
	public RelayException(int code, String msg, String stack) {
		super(msg);
		this.code = code;
		this.stack = stack;
	}
	
	public int getCode() {
		return code;
	}
	public String getStack() {
		return stack;
	}
}
