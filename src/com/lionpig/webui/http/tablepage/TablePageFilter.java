package com.lionpig.webui.http.tablepage;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.DateFormat;

public class TablePageFilter {
	private String columnName;
	private String columnType;
	private String operation;
	private String value1;
	private String value2;
	private boolean sqlScript;
	private Object[] sqlScriptValues;
	
	public TablePageFilter(String columnName, String columnType, String operation, String value1, String value2) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.operation = operation;
		this.value1 = value1;
		this.value2 = value2;
		this.sqlScript = false;
		
		if (columnType != null && columnType.equals(C.columnTypeDate)) {
			this.value1 = value1 + " 00:00:00";
			this.value2 = value2 + " 23:59:59";
		}
	}
	
	public TablePageFilter(String sql, String[] values) {
		this.sqlScript = true;
		this.columnName = sql;
		this.columnType = C.columnTypeString;
		if (values == null)
			this.sqlScriptValues = new Object[0];
		else
			this.sqlScriptValues = values;
	}
	
	public String getColumnName() {
		return this.columnName;
	}
	
	public String getColumnType() {
		return columnType;
	}
	
	public String getOperation() {
		return operation;
	}
	
	public String getValue1() {
		return value1;
	}
	
	public String getValue2() {
		return value2;
	}
	
	public boolean isSqlScript() {
		return sqlScript;
	}
	
	public Object[] getSqlScriptValues() {
		return sqlScriptValues;
	}

	public static String convertToSql(List<TablePageFilter> filter, List<Object> listFilterValue) throws ParseException {
		if (listFilterValue == null)
			throw new NullPointerException("You need to provide listFilterValue when using TablePageFilter.convertToSql");
		
		listFilterValue.clear();
		DateFormat df = DateFormat.getInstance();
		StringBuilder sbFilter = new StringBuilder();

		String n, t, o, v1, v2;
		boolean b;
		TablePageFilter f;
		Iterator<TablePageFilter> e = filter.iterator();
		while (e.hasNext()) {
			f = e.next();
			
			n = f.getColumnName();
			t = f.getColumnType();
			o = f.getOperation();
			v1 = f.getValue1();
			b = f.isSqlScript();
			
			if (b) {
				sbFilter.append(n);
				Object[] objs = f.getSqlScriptValues();
				for (int i = 0; i < objs.length; i++)
					listFilterValue.add(objs[i]);
			}
			else {
				if (o.equals(C.operationBetween)) {
					v2 = f.getValue2();

					sbFilter.append("a.").append(n).append(" BETWEEN ? AND ?");
					if (t.equals(C.columnTypeDateTime) || t.equals(C.columnTypeDate)) {
						listFilterValue.add(df.parse(v1));
						listFilterValue.add(df.parse(v2));
					}
					else if (t.equals(C.columnTypeNumber)) {
						listFilterValue.add(Double.parseDouble(v1));
						listFilterValue.add(Double.parseDouble(v2));
					}
					else {
						listFilterValue.add(v1);
						listFilterValue.add(v2);
					}
				}
				else if (o.equals(C.operationIn) || o.equals(C.operationNotIn) || o.equals(C.operationIn31) || o.equals(C.operationNotIn31)) {
					String[] tokens;
					if (o.equals(C.operationIn) || o.equals(C.operationNotIn)) {
						tokens = v1.split(C.comma);
					} else {
						tokens = v1.split(String.valueOf(C.char_31));
						if (o.equals(C.operationIn31))
							o = C.operationIn;
						else
							o = C.operationNotIn;
					}
					sbFilter.append("a.").append(n).append(" ").append(o).append(" (");
					if (t.equals(C.columnTypeDateTime) || t.equals(C.columnTypeDate)) {
						for (int i = 0; i < tokens.length; i++) {
							sbFilter.append("?,");
							listFilterValue.add(df.parse(tokens[i]));
						}
					}
					else if (t.equals(C.columnTypeNumber)) {
						for (int i = 0; i < tokens.length; i++) {
							sbFilter.append("?,");
							listFilterValue.add(Double.parseDouble(tokens[i]));
						}
					}
					else {
						for (int i = 0; i < tokens.length; i++) {
							sbFilter.append("?,");
							listFilterValue.add(tokens[i]);
						}
					}
					sbFilter.delete(sbFilter.length() - 1, sbFilter.length()).append(")");
				}
				else {
					sbFilter.append("a.").append(n).append(" ").append(o).append(" ?");
					if (t.equals(C.columnTypeDateTime) || t.equals(C.columnTypeDate)) {
						listFilterValue.add(df.parse(v1));
					}
					else if (t.equals(C.columnTypeNumber)) {
						listFilterValue.add(Double.parseDouble(v1));
					}
					else {
						listFilterValue.add(v1);
					}
				}
			}
			
			sbFilter.append(" AND ");
		}
		sbFilter.delete(sbFilter.length() - 5, sbFilter.length());
		return sbFilter.toString();
	}
}
