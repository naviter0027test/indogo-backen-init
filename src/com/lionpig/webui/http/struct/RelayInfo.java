package com.lionpig.webui.http.struct;

public class RelayInfo {
	private String relayId;
	public String getRelayId() {
		return relayId;
	}
	
	private String url;
	public String getUrl() {
		return url;
	}
	
	private String loginName;
	public String getLoginName() {
		return loginName;
	}
	
	private String loginPass;
	public String getLoginPass() {
		return loginPass;
	}
	
	private String relayType;
	public String getRelayType() {
		return relayType;
	}
	
	private String className;
	public String getClassName() {
		return className;
	}
	
	public RelayInfo(String relayId, String url, String loginName, String loginPass, String relayType, String className) {
		this.relayId = relayId;
		this.url = url;
		this.loginName = loginName;
		this.loginPass = loginPass;
		this.relayType = relayType;
		this.className = className;
	}
}
