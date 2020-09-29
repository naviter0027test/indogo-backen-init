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

public class ItemSize extends AbstractFunction implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.item_size;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.size_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.size_name()));
		cols.add(new TablePageColumn(C.size_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.size_id(), true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.size_name).setValue(r.getString(C.size_name));
		cols.get(C.size_id).setValue(r.getInt(C.size_id));
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
		return l.size_name();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		String size_name = Helper.getString(params, C.size_name, true);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_size_insert());
		try {
			int size_id = (int) fi.getConnection().getSeq(C.size_id, true);
			pstmt.setInt(1, size_id);
			pstmt.setString(2, size_name);
			pstmt.executeUpdate();
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.size_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(size_id), null));
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
		int size_id = Helper.getInt(params, C.size_id, true);
		String size_name = Helper.getString(params, C.size_name, true);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_size_update());
		try {
			pstmt.setString(1, size_name);
			pstmt.setInt(2, size_id);
			pstmt.executeUpdate();
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.size_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(size_id), null));
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
		int size_id = Helper.getInt(params, C.size_id, true);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_size_delete());
		try {
			pstmt.setInt(1, size_id);
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
		int size_id = Helper.getInt(params, C.size_id, true);
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_size_get());
		try {
			pstmt.setInt(1, size_id);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					return r.getString(1);
				} else
					throw new Exception(l.data_not_exist(C.size_id + C.operationEqual + size_id));
			} finally {
				r.close();
			}
		} finally {
			pstmt.close();
		}
	}

}
