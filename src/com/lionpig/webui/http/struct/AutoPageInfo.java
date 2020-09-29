package com.lionpig.webui.http.struct;

public class AutoPageInfo {
	private String pageId;
	private String pageType;
	private String cmdText;
	private String dbName;
	
	public AutoPageInfo(String pageId, String pageType, String cmdText, String dbName) {
		this.pageId = pageId;
		this.pageType = pageType;
		this.cmdText = cmdText;
		this.dbName = dbName;
	}
	
	public String getPageId() {
		return pageId;
	}
	public String getPageType() {
		return pageType;
	}
	public String getCmdText() {
		return cmdText;
	}
	public String getDbName() {
		return dbName;
	}
}
