package com.indogo.relay.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.func.TablePage;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class MemberPointSchedule implements ITablePage, IFunction {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.member_point_schedule;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.date_of_month, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Date"));
		cols.add(new TablePageColumn(C.month_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Month"));
		cols.add(new TablePageColumn(C.year_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Year"));
		cols.add(new TablePageColumn(C.remit_point, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Point"));
		cols.add(new TablePageColumn(C.schedule_desc, C.columnTypeString, C.columnDirectionDefault, true, false, "Description"));
		cols.add(new TablePageColumn(C.schedule_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.schedule_id, true, false));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.date_of_month).setValue(r.getInt(C.date_of_month));
		cols.get(C.month_id).setValue(r.getInt(C.month_id));
		cols.get(C.year_id).setValue(r.getInt(C.year_id));
		cols.get(C.remit_point).setValue(r.getInt(C.remit_point));
		cols.get(C.schedule_desc).setValue(r.getString(C.schedule_desc));
		cols.get(C.schedule_id).setValue(r.getInt(C.schedule_id));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
	}

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		if (action.equals(C.insert)) {
			Integer date_of_month = Helper.getIntNullable(params, C.date_of_month, false);
			Integer month_id = Helper.getIntNullable(params, C.month_id, false);
			Integer year_id = Helper.getIntNullable(params, C.year_id, false);
			int remit_point = Helper.getInt(params, C.remit_point, true);
			String schedule_desc = Helper.getString(params, C.schedule_desc, true);
			try {
				int schedule_id = insert(fi, date_of_month, month_id, year_id, remit_point, schedule_desc);
				
				List<TablePageFilter> filter = new ArrayList<>();
				filter.add(new TablePageFilter(C.schedule_id, C.columnTypeNumber, C.operationEqual, String.valueOf(schedule_id), null));
				TablePage p = new TablePage();
				String s = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
				
				conn.commit();
				
				return s;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.update)) {
			int schedule_id = Helper.getInt(params, C.schedule_id, true);
			Integer date_of_month = Helper.getIntNullable(params, C.date_of_month, false);
			Integer month_id = Helper.getIntNullable(params, C.month_id, false);
			Integer year_id = Helper.getIntNullable(params, C.year_id, false);
			int remit_point = Helper.getInt(params, C.remit_point, true);
			String schedule_desc = Helper.getString(params, C.schedule_desc, true);
			try {
				update(fi, date_of_month, month_id, year_id, remit_point, schedule_desc, schedule_id);
				
				List<TablePageFilter> filter = new ArrayList<>();
				filter.add(new TablePageFilter(C.schedule_id, C.columnTypeNumber, C.operationEqual, String.valueOf(schedule_id), null));
				TablePage p = new TablePage();
				String s = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
				
				conn.commit();
				
				return s;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.delete)) {
			int schedule_id = Helper.getInt(params, C.schedule_id, true);
			try {
				delete(fi, schedule_id);
				conn.commit();
				return C.emptyString;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.getDataForUpdate)) {
			int schedule_id = Helper.getInt(params, C.schedule_id, true);
			PreparedStatement pstmt = conn.prepareStatement("select date_of_month, month_id, year_id, remit_point, schedule_desc from member_point_schedule where schedule_id = ?");
			try {
				pstmt.setInt(1, schedule_id);
				ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery());
				try {
					if (r.next()) {
						StringBuilder sb = new StringBuilder();
						sb.append(r.getInt(1)).append(C.char_31)
						.append(r.getInt(2)).append(C.char_31)
						.append(r.getInt(3)).append(C.char_31)
						.append(r.getInt(4)).append(C.char_31)
						.append(r.getString(5));
						return sb.toString();
					} else
						throw new Exception(String.format(C.data_not_exist, "schedule_id = " + schedule_id));
				} finally {
					r.close();
				}
			} finally {
				pstmt.close();
			}
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}
	
	private int insert(FunctionItem fi, Integer date_of_month, Integer month_id, Integer year_id, int remit_point, String schedule_desc) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("insert into member_point_schedule (schedule_id, date_of_month, month_id, year_id, remit_point, schedule_desc) values (?,?,?,?,?,?)");
		try {
			int schedule_id = (int) fi.getConnection().getSeq(C.schedule_id, true);
			
			pstmt.setInt(1, schedule_id);
			if (date_of_month == null) pstmt.setNull(2, Types.NUMERIC); else pstmt.setInt(2, date_of_month);
			if (month_id == null) pstmt.setNull(3, Types.NUMERIC); else pstmt.setInt(3, month_id);
			if (year_id == null) pstmt.setNull(4, Types.NUMERIC); else pstmt.setInt(4, year_id);
			pstmt.setInt(5, remit_point);
			pstmt.setString(6, schedule_desc);
			pstmt.executeUpdate();
			
			return schedule_id;
		} finally {
			pstmt.close();
		}
	}
	
	private void update(FunctionItem fi, Integer date_of_month, Integer month_id, Integer year_id, int remit_point, String schedule_desc, int schedule_id) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("update member_point_schedule set date_of_month = ?, month_id = ?, year_id = ?, remit_point = ?, schedule_desc = ? where schedule_id = ?");
		try {
			if (date_of_month == null) pstmt.setNull(1, Types.NUMERIC); else pstmt.setInt(1, date_of_month);
			if (month_id == null) pstmt.setNull(2, Types.NUMERIC); else pstmt.setInt(2, month_id);
			if (year_id == null) pstmt.setNull(3, Types.NUMERIC); else pstmt.setInt(3, year_id);
			pstmt.setInt(4, remit_point);
			pstmt.setString(5, schedule_desc);
			pstmt.setInt(6, schedule_id);
			pstmt.executeUpdate();
		} finally {
			pstmt.close();
		}
	}
	
	private void delete(FunctionItem fi, int schedule_id) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("delete from member_point_schedule where schedule_id = ?");
		try {
			pstmt.setInt(1, schedule_id);
			pstmt.executeUpdate();
		} finally {
			pstmt.close();
		}
	}

}
