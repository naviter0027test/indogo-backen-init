package com.lionpig.webui.http.tablepage;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class TablePageSort {
	private String columnName;
	private String columnDirection;
	
	public TablePageSort(String columnName, String columnDirection) {
		this.columnDirection = columnDirection;
		this.columnName = columnName;
	}
	
	public String getColumnName() {
		return this.columnName;
	}
	
	public String getColumnDirection() {
		return this.columnDirection;
	}
	
	public static String convertToSql(List<TablePageSort> sort) {
		// mantis 26: TablePage duplicate column in order by
		Hashtable<String, String> ht = new Hashtable<String, String>();
		StringBuilder sbSort = new StringBuilder();
		Iterator<TablePageSort> i = sort.iterator();
		if (i.hasNext()) {
			TablePageSort s = i.next();
			sbSort.append(s.getColumnName()).append(" ").append(s.getColumnDirection());
			ht.put(s.getColumnName(), s.getColumnName());
			while (i.hasNext()) {
				s = i.next();
				if (!ht.containsKey(s.getColumnName())) {
					sbSort.append(", ").append(s.getColumnName()).append(" ").append(s.getColumnDirection());
					ht.put(s.getColumnName(), s.getColumnName());
				}
			}
		}
		return sbSort.toString();
	}
}
