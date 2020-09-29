package com.lionpig.webui.http.struct.admin;

public class PasswordConfirmation {
	private int userRowId;
	private String emailAddress;
	private String confirmId;
	
	public PasswordConfirmation(int userRowId, String emailAddress, String confirmId) {
		this.userRowId = userRowId;
		this.emailAddress = emailAddress;
		this.confirmId = confirmId;
	}
	
	public int getUserRowId() {
		return userRowId;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	
	public String getConfirmId() {
		return confirmId;
	}
}
