package com.lionpig.webui.database;

public class HistoryData {
	private String attrName;
	private String oldAttrValue;
	private String newAttrValue;
	
	public HistoryData(String attrName, String oldAttrValue, String newAttrValue) {
		this.attrName = attrName;
		this.oldAttrValue = oldAttrValue;
		this.newAttrValue = newAttrValue;
	}
	
	public String getAttrName() {
		return this.attrName;
	}
	
	public String getOldAttrValue() {
		return this.oldAttrValue;
	}
	
	public String getNewAttrValue() {
		return this.newAttrValue;
	}
}
