package com.lionpig.webui.http.struct.admin;

public class User {
	private int userRowId;
	private String userName;
	private boolean disabled;
	private String sessionId;
	
	public User(int userRowId, String userName, boolean disabled, String sessionId) {
		this.userRowId = userRowId;
		this.userName = userName;
		this.disabled = disabled;
		this.sessionId = sessionId;
	}
	
	public int getUserRowId() {
		return userRowId;
	}
	public String getUserName() {
		return userName;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public String getSessionId() {
		return sessionId;
	}
}
