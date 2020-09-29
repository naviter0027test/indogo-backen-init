package com.lionpig.webui.http.struct;

public class LogInfo {
	public static final int LOG_ERROR = 1;
	public static final int LOG_WARNING = 2;
	public static final int LOG_LOGIN = 8;
	public static final int LOG_DEBUG = 9;
	public static final int LOG_TRACE = 10;
	
	private int verbose;
	private String message;
	private int userRowId;
	private String userName;
	private String sessionId;
	private String relayId;
	private String relayUrl;
	private String functionName;
	private String className;
	
	public LogInfo(int userRowId, String userName, String sessionId, String functionName) {
		this.verbose = LOG_DEBUG;
		this.userRowId = userRowId;
		this.userName = userName;
		this.sessionId = sessionId;
		this.functionName = functionName;
	}
	
	public int getVerbose() {
		return verbose;
	}
	public String getMessage() {
		return message;
	}
	public int getUserRowId() {
		return userRowId;
	}
	public String getUserName() {
		return userName;
	}
	public String getSessionId() {
		return sessionId;
	}
	public String getRelayId() {
		return relayId;
	}
	public String getRelayUrl() {
		return relayUrl;
	}
	public String getFunctionName() {
		return functionName;
	}
	public String getClassName() {
		return className;
	}
	
	public void setVerbose(int verbose) {
		this.verbose = verbose;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setRelayId(String relayId) {
		this.relayId = relayId;
	}
	public void setRelayUrl(String relayUrl) {
		this.relayUrl = relayUrl;
	}
	public void setClassName(String className) {
		this.className = className;
	}
}
