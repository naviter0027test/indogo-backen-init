package com.lionpig.webui.http.tablepage;

import java.util.Iterator;
import java.util.List;

public class TablePageColumn {
	private String columnName;
	private String columnType;
	private String displayName;
	private String direction;
	private boolean canFilter;
	private boolean virtualColumn;
	private String value;
	private boolean doNotExport;
	private boolean isHidden;
	
	// internal value for export excel
	public boolean CalculateSummary = false;
	public boolean NumberAsString = false;
	
	public TablePageColumn(String columnName, String columnType, boolean virtualColumn) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.virtualColumn = virtualColumn;
		this.doNotExport = false;
	}
	
	public TablePageColumn(String columnName, String columnType, String direction, boolean canFilter, boolean virtualColumn, String displayName) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.direction = direction;
		this.canFilter = canFilter;
		this.virtualColumn = virtualColumn;
		this.displayName = displayName;
		this.doNotExport = false;
	}
	
	public TablePageColumn(String columnName, String columnType, String direction, boolean canFilter, boolean virtualColumn, String displayName, boolean doNotExport) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.direction = direction;
		this.canFilter = canFilter;
		this.virtualColumn = virtualColumn;
		this.displayName = displayName;
		this.doNotExport = doNotExport;
	}
	
	public TablePageColumn(String columnName, String columnType, String direction, boolean canFilter, boolean virtualColumn, String displayName, boolean doNotExport, boolean isHidden) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.direction = direction;
		this.canFilter = canFilter;
		this.virtualColumn = virtualColumn;
		this.displayName = displayName;
		this.doNotExport = doNotExport;
		this.isHidden = isHidden;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof TablePageColumn) {
			return this.columnName.equals(((TablePageColumn)obj).columnName);
		}
		else
			return super.equals(obj);
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public String getColumnType() {
		return columnType;
	}
	
	public String getDirection() {
		return direction;
	}
	
	public boolean getCanFilter() {
		return canFilter;
	}
	
	public boolean isVirtualColumn() {
		return virtualColumn;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public boolean getDoNotExport() {
		return doNotExport;
	}
	
	public boolean isHidden() {
		return isHidden;
	}
	
	public void setValue(String s) {
		value = s;
	}
	
	public String getValue() {
		if (value == null)
			return "";
		else
			return value;
	}
	
	public static String convertToSql(List<TablePageColumn> column) {
		StringBuilder sbCol = new StringBuilder();
		Iterator<TablePageColumn> e = column.iterator();
		TablePageColumn c;
		while (e.hasNext()) {
			c = e.next();
			if (!c.isVirtualColumn()) {
				sbCol.append("a.").append(c.getColumnName());
				
				while (e.hasNext()) {
					c = e.next();
					if (!c.isVirtualColumn()) {
						sbCol.append(", a.").append(c.getColumnName());
					}
				}
			}
		}
		return sbCol.toString();
	}
}
