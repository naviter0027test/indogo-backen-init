package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
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

public class ItemCategory extends AbstractFunction implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.item_category;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.category_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.category_name()));
		cols.add(new TablePageColumn(C.item_name_prefix, C.columnTypeString, C.columnDirectionDefault, true, false, l.item_name_prefix()));
		cols.add(new TablePageColumn(C.imei_flag, C.columnTypeString, C.columnDirectionDefault, true, false, l.imei_flag()));
		cols.add(new TablePageColumn(C.category_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.category_id(), true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.category_name).setValue(r.getString(C.category_name));
		cols.get(C.item_name_prefix).setValue(r.getString(C.item_name_prefix));
		cols.get(C.imei_flag).setValue(r.getInt(C.imei_flag));
		cols.get(C.category_id).setValue(r.getInt(C.category_id));
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
		return null;
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		String category_name = Helper.getString(params, C.category_name, true);
		boolean imei_flag = Helper.getInt(params, C.imei_flag, false) == 1;
		String item_name_prefix = Helper.getString(params, C.item_name_prefix, false);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_category_insert());
		try {
			int category_id = (int) fi.getConnection().getSeq(C.category_id, true);
			pstmt.setInt(1, category_id);
			pstmt.setString(2, category_name);
			pstmt.setInt(3, imei_flag ? 1 : 0);
			pstmt.setString(4, item_name_prefix);
			pstmt.executeUpdate();
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.category_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(category_id), null));
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
		int category_id = Helper.getInt(params, C.category_id, true);
		String category_name = Helper.getString(params, C.category_name, true);
		boolean imei_flag = Helper.getInt(params, C.imei_flag, false) == 1;
		String item_name_prefix = Helper.getString(params, C.item_name_prefix, false);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_category_update());
		try {
			pstmt.setString(1, category_name);
			pstmt.setInt(2, imei_flag ? 1 : 0);
			pstmt.setString(3, item_name_prefix);
			pstmt.setInt(4, category_id);
			pstmt.executeUpdate();
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.category_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(category_id), null));
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
		int category_id = Helper.getInt(params, C.category_id, true);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.item_category_delete());
		try {
			pstmt.setInt(1, category_id);
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
		int category_id = Helper.getInt(params, C.category_id, true);
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.item_category_get()))) {
			pstmt.setInt(1, category_id);
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				if (r.next()) {
					return new StringBuilder().append(r.getString(1)).append(C.char_31).append(r.getInt(2)).append(C.char_31).append(r.getString(3)).toString();
				} else
					throw new Exception(l.data_not_exist(C.category_id + C.operationEqual + category_id));
			}
		}
	}
	
	public static String getAll(FunctionItem fi) throws Exception {
		StringBuilder sb = new StringBuilder();
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(fi.getSql().item_category_get_all()))) {
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				StringBuilder cc = new StringBuilder();
				int size = 0;
				while (r.next()) {
					cc.append(C.char_31).append(r.getInt(1))
					.append(C.char_31).append(r.getString(2))
					.append(C.char_31).append(r.getString(3));
					size++;
				}
				sb.append(size).append(cc);
			}
		}
		return sb.toString();
	}

}
