package com.lionpig.webui.http;

public class NotAllowedException extends FunctionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5083658787244554842L;

	public NotAllowedException(int errorCode, String msg) {
		super(errorCode, msg);
	}

}
