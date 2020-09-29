package com.lionpig.webui.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.lionpig.webui.http.util.C;

public class ResultSetWrapper implements AutoCloseable {

	private ResultSet r;
	
	public ResultSetWrapper(ResultSet r) {
		this.r = r;
	}
	
	public ResultSet unwrap() {
		return r;
	}
	
	public String getString(int columnIndex) throws SQLException {
		String s = r.getString(columnIndex);
		if (s == null || s.length() == 0) {
			return null;
		} else {
			return s;
		}
	}
	
	public String getString(String columnLabel) throws SQLException {
		String s = r.getString(columnLabel);
		if (s == null || s.length() == 0) {
			return C.emptyString;
		} else {
			return s;
		}
	}
	
	public Integer getInt(int columnIndex) throws SQLException {
		int i = r.getInt(columnIndex);
		if (r.wasNull()) {
			return null;
		} else {
			return i;
		}
	}
	
	public Integer getInt(String columnLabel) throws SQLException {
		int i = r.getInt(columnLabel);
		if (r.wasNull()) {
			return null;
		} else {
			return i;
		}
	}
	
	public Long getLong(int columnIndex) throws SQLException {
		long l = r.getLong(columnIndex);
		if (r.wasNull()) {
			return null;
		} else {
			return l;
		}
	}
	
	public Long getLong(String columnLabel) throws SQLException {
		long l = r.getLong(columnLabel);
		if (r.wasNull()) {
			return null;
		} else {
			return l;
		}
	}
	
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return r.getTimestamp(columnIndex);
	}
	
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return r.getTimestamp(columnLabel);
	}
	
	public Double getDouble(int columnIndex) throws SQLException {
		double d = r.getDouble(columnIndex);
		if (r.wasNull()) {
			return null;
		} else {
			return d;
		}
	}
	
	public Double getDouble(String columnLabel) throws SQLException {
		double d = r.getDouble(columnLabel);
		if (r.wasNull()) {
			return null;
		} else {
			return d;
		}
	}
	
	public Boolean getBoolean(int columnIndex) throws SQLException {
		int i = r.getInt(columnIndex);
		if (r.wasNull()) {
			return null;
		} else {
			return i == 1;
		}
	}
	
	public Boolean getBoolean(String columnLabel) throws SQLException {
		int i = r.getInt(columnLabel);
		if (r.wasNull()) {
			return null;
		} else {
			return i == 1;
		}
	}
	
	public boolean next() throws SQLException {
		return r.next();
	}

	@Override
	public void close() throws Exception {
		r.close();
	}
}
