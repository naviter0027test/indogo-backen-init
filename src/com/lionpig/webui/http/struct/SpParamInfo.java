package com.lionpig.webui.http.struct;

public class SpParamInfo {
	private String pageId;
	private String paramName;
	private String paramType;
	private String paramDirection;
	private int paramSeq;
	
	public SpParamInfo(String pageId, String paramName, String paramType, String paramDirection, int paramSeq) {
		this.pageId = pageId;
		this.paramName = paramName;
		this.paramType = paramType;
		this.paramDirection = paramDirection;
		this.paramSeq = paramSeq;
	}
	
	public String getPageId() {
		return pageId;
	}
	public String getParamName() {
		return paramName;
	}
	public String getParamType() {
		return paramType;
	}
	public String getParamDirection() {
		return paramDirection;
	}
	public int getParamSeq() {
		return paramSeq;
	}
}
