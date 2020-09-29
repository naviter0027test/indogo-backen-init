package com.lionpig.webui.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.lionpig.webui.http.util.Stringify;

public class ResultSetWrapperStringify implements AutoCloseable {
	private ResultSetWrapper r;
	
	public ResultSetWrapperStringify(ResultSet r) {
		this.r = new ResultSetWrapper(r);
	}
	
	public ResultSetWrapperStringify(ResultSetWrapper r) {
		this.r = r;
	}
	
	public ResultSetWrapper unwrap() {
		return r;
	}
	
	public String getString(int columnIndex) throws SQLException {
		return Stringify.getString(r.getString(columnIndex));
	}
	
	public String getString(String columnLabel) throws SQLException {
		return Stringify.getString(r.getString(columnLabel));
	}
	
	public String getInt(int columnIndex) throws SQLException {
		return Stringify.getString(r.getInt(columnIndex));
	}
	
	public String getInt(String columnLabel) throws SQLException {
		return Stringify.getString(r.getInt(columnLabel));
	}
	
	public String getLong(int columnIndex) throws SQLException {
		return Stringify.getString(r.getLong(columnIndex));
	}
	
	public String getLong(String columnLabel) throws SQLException {
		return Stringify.getString(r.getLong(columnLabel));
	}
	
	public String getDouble(int columnIndex) throws SQLException {
		return Stringify.getString(r.getDouble(columnIndex));
	}
	
	public String getDouble(String columnLabel) throws SQLException {
		return Stringify.getString(r.getDouble(columnLabel));
	}
	
	public String getIntCurrency(int columnIndex) throws SQLException {
		return Stringify.getCurrency(r.getInt(columnIndex));
	}
	
	public String getIntCurrency(String columnLabel) throws SQLException {
		return Stringify.getCurrency(r.getInt(columnLabel));
	}
	
	public String getLongCurrency(int columnIndex) throws SQLException {
		return Stringify.getCurrency(r.getLong(columnIndex));
	}
	
	public String getLongCurrency(String columnLabel) throws SQLException {
		return Stringify.getCurrency(r.getLong(columnLabel));
	}
	
	public String getDoubleCurrency(int columnIndex) throws SQLException {
		return Stringify.getCurrency(r.getDouble(columnIndex));
	}
	
	public String getDoubleCurrency(String columnLabel) throws SQLException {
		return Stringify.getCurrency(r.getDouble(columnLabel));
	}
	
	public String getTimestamp(int columnIndex) throws SQLException {
		return Stringify.getTimestamp(r.getTimestamp(columnIndex));
	}
	
	public String getTimestamp(String columnLabel) throws SQLException {
		return Stringify.getTimestamp(r.getTimestamp(columnLabel));
	}
	
	public String getDate(int columnIndex) throws SQLException {
		return Stringify.getDate(r.getTimestamp(columnIndex));
	}
	
	public String getDate(String columnLabel) throws SQLException {
		return Stringify.getDate(r.getTimestamp(columnLabel));
	}
	
	/*
	public String getString(int columnIndex) throws SQLException {
		String s = r.getString(columnIndex);
		if (s == null) {
			return C.emptyString;
		} else {
			return s;
		}
	}
	
	public String getString(String columnLabel) throws SQLException {
		String s = r.getString(columnLabel);
		if (s == null) {
			return C.emptyString;
		} else {
			return s;
		}
	}
	
	public String getInt(int columnIndex) throws SQLException {
		int i = r.getInt(columnIndex);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.valueOf(i);
		}
	}
	
	public String getInt(String columnLabel) throws SQLException {
		int i = r.getInt(columnLabel);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.valueOf(i);
		}
	}
	
	public String getIntCurrency(int columnIndex) throws SQLException {
		int i = r.getInt(columnIndex);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.format("%,d", i);
		}
	}
	
	public String getIntCurrency(String columnLabel) throws SQLException {
		int i = r.getInt(columnLabel);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.format("%,d", i);
		}
	}
	
	public String getLong(int columnIndex) throws SQLException {
		long l = r.getLong(columnIndex);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.valueOf(l);
		}
	}
	
	public String getLong(String columnLabel) throws SQLException {
		long l = r.getLong(columnLabel);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.valueOf(l);
		}
	}

	public String getLongCurrency(int columnIndex) throws SQLException {
		long l = r.getLong(columnIndex);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.format("%,d", l);
		}
	}
	
	public String getLongCurrency(String columnLabel) throws SQLException {
		long l = r.getLong(columnLabel);
		if (r.wasNull())
			return C.emptyString;
		else
			return String.format("%,d", l);
	}
	
	public String getTimestamp(int columnIndex) throws SQLException {
		Timestamp ts = r.getTimestamp(columnIndex);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return DateFormat.getInstance().format(ts);
		}
	}
	
	public String getTimestamp(String columnLabel) throws SQLException {
		Timestamp ts = r.getTimestamp(columnLabel);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return DateFormat.getInstance().format(ts);
		}
	}
	
	public String getDate(int columnIndex) throws SQLException {
		Timestamp ts = r.getTimestamp(columnIndex);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return DateFormat.getInstance().formatShort(ts);
		}
	}
	
	public String getDate(String columnLabel) throws SQLException {
		Timestamp ts = r.getTimestamp(columnLabel);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return DateFormat.getInstance().formatShort(ts);
		}
	}
	
	public String getDouble(int columnIndex) throws SQLException {
		double d = r.getDouble(columnIndex);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.valueOf(d);
		}
	}
	
	public String getDouble(String columnLabel) throws SQLException {
		double d = r.getDouble(columnLabel);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.valueOf(d);
		}
	}
	
	public String getDoubleCurrency(int columnIndex) throws SQLException {
		double d = r.getDouble(columnIndex);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.format("%,.4f", d);
		}
	}
	
	public String getDoubleCurrency(String columnLabel) throws SQLException {
		double d = r.getDouble(columnLabel);
		if (r.wasNull()) {
			return C.emptyString;
		} else {
			return String.format("%,.4f", d);
		}
	}
	*/
	
	public boolean next() throws SQLException {
		return r.next();
	}

	@Override
	public void close() throws Exception {
		r.close();
	}
}
