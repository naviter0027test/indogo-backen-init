package com.lionpig.webui.http.tablepage;

import java.sql.ResultSet;

public class TablePageResult {
	private ResultSet r;
	private int totalRecord;
	
	public TablePageResult(ResultSet r, int totalRecord) {
		this.r = r;
		this.totalRecord = totalRecord;
	}
	
	public ResultSet getResultSet() {
		return r;
	}
	
	public int getTotalRecord() {
		return totalRecord;
	}
}
