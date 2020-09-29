package com.lionpig.webui.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class PreparedStatementWrapper implements AutoCloseable {
	
	private PreparedStatement pstmt;
	
	public PreparedStatementWrapper(PreparedStatement pstmt) {
		this.pstmt = pstmt;
	}
	
	public void setString(int index, String value) throws SQLException {
		if (value == null || value.length() == 0)
			pstmt.setNull(index, Types.VARCHAR);
		else
			pstmt.setString(index, value);
	}
	
	public void setInt(int index, int value) throws SQLException {
		pstmt.setInt(index, value);
	}
	
	public void setInt(int index, Integer value) throws SQLException {
		if (value == null)
			pstmt.setNull(index, Types.NUMERIC);
		else
			pstmt.setInt(index, value.intValue());
	}
	
	public void setLong(int index, long value) throws SQLException {
		pstmt.setLong(index, value);
	}
	
	public void setLong(int index, Long value) throws SQLException {
		if (value == null)
			pstmt.setNull(index, Types.NUMERIC);
		else
			pstmt.setLong(index, value.longValue());
	}
	
	public void setDouble(int index, double value) throws SQLException {
		pstmt.setDouble(index, value);
	}
	
	public void setDouble(int index, Double value) throws SQLException {
		if (value == null)
			pstmt.setNull(index, Types.NUMERIC);
		else
			pstmt.setDouble(index, value.doubleValue());
	}
	
	public void setTimestamp(int index, Timestamp value) throws SQLException {
		if (value == null)
			pstmt.setNull(index, Types.DATE);
		else
			pstmt.setTimestamp(index, value);
	}
	
	public int executeUpdate() throws SQLException {
		return pstmt.executeUpdate();
	}
	
	public ResultSet executeQuery() throws SQLException {
		return pstmt.executeQuery();
	}
	
	public ResultSetWrapper executeQueryWrapper() throws SQLException {
		return new ResultSetWrapper(pstmt.executeQuery());
	}
	
	public ResultSetWrapperStringify executeQueryWrapperStringify() throws SQLException {
		return new ResultSetWrapperStringify(pstmt.executeQuery());
	}

	@Override
	public void close() throws Exception {
		pstmt.close();
	}
	
	public void addBatch() throws SQLException {
		pstmt.addBatch();
	}
	
	public int[] executeBatch() throws SQLException {
		return pstmt.executeBatch();
	}

}
