package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.func.TablePage;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;
import com.lionpig.webui.http.util.Stringify;

public class ItemColor extends AbstractFunction implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.item_color;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.color_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.color_name()));
		cols.add(new TablePageColumn(C.color_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.color_id(), true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.color_name).setValue(r.getString(C.color_name));
		cols.get(C.color_id).setValue(r.getString(C.color_id));
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
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return l.color_name();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		String color_name = Helper.getString(params, C.color_name, true);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_color_insert());
		try {
			int color_id = (int) fi.getConnection().getSeq(C.color_id, true);
			pstmt.setInt(1, color_id);
			pstmt.setString(2, color_name);
			pstmt.executeUpdate();
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.color_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(color_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			conn.commit();
			
			return row;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		} finally {
			pstmt.close();
		}
	}

	@Override
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int color_id = Helper.getInt(params, C.color_id, true);
		String color_name = Helper.getString(params, C.color_name, true);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_color_update());
		try {
			pstmt.setString(1, color_name);
			pstmt.setInt(2, color_id);
			pstmt.executeUpdate();
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.color_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(color_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			conn.commit();
			
			return row;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		} finally {
			pstmt.close();
		}
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int color_id = Helper.getInt(params, C.color_id, true);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_color_delete());
		try {
			pstmt.setInt(1, color_id);
			pstmt.executeUpdate();
			
			conn.commit();
			
			return C.emptyString;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		} finally {
			pstmt.close();
		}
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int color_id = Helper.getInt(params, C.color_id, true);
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_color_get());
		try {
			pstmt.setInt(1, color_id);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					return r.getString(1);
				} else
					throw new Exception(l.data_not_exist(C.color_id + C.operationEqual + color_id));
			} finally {
				r.close();
			}
		} finally {
			pstmt.close();
		}
	}

}
