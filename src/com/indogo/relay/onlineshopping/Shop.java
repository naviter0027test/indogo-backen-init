package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
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

public class Shop extends AbstractFunction implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.shop;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.shop_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.shop_name()));
		cols.add(new TablePageColumn(C.shop_telp, C.columnTypeString, C.columnDirectionDefault, true, false, l.shop_telp()));
		cols.add(new TablePageColumn(C.shop_address, C.columnTypeString, C.columnDirectionDefault, true, false, l.shop_address()));
		cols.add(new TablePageColumn(C.shop_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.shop_id(), true, true));
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.shop_name).setValue(r.getString(C.shop_name));
		cols.get(C.shop_telp).setValue(r.getString(C.shop_telp));
		cols.get(C.shop_address).setValue(r.getString(C.shop_address));
		cols.get(C.shop_id).setValue(r.getInt(C.shop_id));
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
		Connection conn = fi.getConnection().getConnection();
		if (action.equals(C.get_shop_user)) {
			int shop_id = Helper.getInt(params, C.shop_id, true);
			
			int userListSize = 0;
			StringBuilder sbUserList = new StringBuilder(50);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.user_list_get()))) {
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQueryWrapper())) {
					while (r.next()) {
						sbUserList.append(r.getInt(1))
						.append(C.char_30).append(r.getString(2))
						.append(C.char_30).append(r.getString(3))
						.append(C.char_31);
						userListSize++;
					}
				}
			}
			
			if (userListSize > 0) {
				sbUserList.delete(sbUserList.length() - 1, sbUserList.length());
			}
			
			int shopUserSize = 0;
			StringBuilder sbShopUser = new StringBuilder(50);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.shop_user_get()))) {
				pstmt.setInt(1, shop_id);
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQueryWrapper())) {
					while (r.next()) {
						sbShopUser.append(r.getInt(1))
						.append(C.char_30).append(r.getString(2))
						.append(C.char_30).append(r.getString(3))
						.append(C.char_31);
						shopUserSize++;
					}
				}
			}
			
			if (shopUserSize > 0) {
				sbShopUser.delete(sbShopUser.length() - 1, sbShopUser.length());
			}
			
			StringBuilder sb = new StringBuilder(50);
			sb.append(userListSize)
			.append(C.char_31).append(sbUserList)
			.append(C.char_31).append(shopUserSize)
			.append(C.char_31).append(sbShopUser);
			return sb.toString();
		} else if (action.equals(C.set_shop_user)) {
			int shop_id = Helper.getInt(params, C.shop_id, true);
			int[] user_row_ids = Helper.getIntArray(params, C.user_row_ids, false);
			try {
				setShopUser(fi, shop_id, user_row_ids);
				conn.commit();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		}
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(l.shop_name())
		.append(C.char_31).append(l.shop_telp())
		.append(C.char_31).append(l.shop_address())
		.append(C.char_31).append(l.button_add_shop_user());
		return sb.toString();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		String shop_name = Helper.getString(params, C.shop_name, true, l.shop_name());
		String shop_telp = Helper.getString(params, C.shop_telp, false, l.shop_telp());
		String shop_address = Helper.getString(params, C.shop_address, false, l.shop_address());
		
		Connection conn = fi.getConnection().getConnection();
		try {
			int shop_id = (int) fi.getConnection().getSeq(C.shop, true);
			
			PreparedStatement pstmt = conn.prepareStatement(s.shop_insert());
			try {
				pstmt.setInt(1, shop_id);
				pstmt.setString(2, shop_name);
				pstmt.setString(3, shop_telp);
				pstmt.setString(4, shop_address);
				pstmt.executeUpdate();
			} finally {
				pstmt.close();
			}
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.shop_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(shop_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			conn.commit();
			
			return row;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}

	@Override
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int shop_id = Helper.getInt(params, C.shop_id, true, l.shop_id());
		String shop_name = Helper.getString(params, C.shop_name, true, l.shop_name());
		String shop_telp = Helper.getString(params, C.shop_telp, false, l.shop_telp());
		String shop_address = Helper.getString(params, C.shop_address, false, l.shop_address());
		
		Connection conn = fi.getConnection().getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(s.shop_update());
			try {
				pstmt.setString(1, shop_name);
				pstmt.setString(2, shop_telp);
				pstmt.setString(3, shop_address);
				pstmt.setInt(4, shop_id);
				pstmt.executeUpdate();
			} finally {
				pstmt.close();
			}
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.shop_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(shop_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			conn.commit();
			
			return row;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int shop_id = Helper.getInt(params, C.shop_id, true, l.shop_id());
		Connection conn = fi.getConnection().getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(s.shop_delete());
			try {
				pstmt.setInt(1, shop_id);
				pstmt.executeUpdate();
			} finally {
				pstmt.close();
			}
			conn.commit();
			return C.emptyString;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int shop_id = Helper.getInt(params, C.shop_id, true, l.shop_id());
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(s.shop_get());
		try {
			pstmt.setInt(1, shop_id);
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				if (r.next()) {
					String shop_name = r.getString(1);
					String shop_telp = r.getString(2);
					String shop_address = r.getString(3);
					
					StringBuilder sb = new StringBuilder();
					sb.append(shop_name)
					.append(C.char_31).append(shop_telp)
					.append(C.char_31).append(shop_address);
					return sb.toString();
				} else {
					throw new Exception(l.data_not_exist(C.shop_id + " = " + shop_id));
				}
			}
		} finally {
			pstmt.close();
		}
	}
	
	public void setShopUser(FunctionItem fi, int shop_id, int[] user_row_ids) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(sql.shop_user_delete())) {
			pstmt.setInt(1, shop_id);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatement pstmt = conn.prepareStatement(sql.shop_user_add())) {
			for (int user_row_id : user_row_ids) {
				pstmt.setInt(1, shop_id);
				pstmt.setInt(2, user_row_id);
				pstmt.executeUpdate();
			}
		}
	}
	
	public static String getShopList(FunctionItem fi) throws Exception {
		StringBuilder sb = new StringBuilder();
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(fi.getSql().shop_list_all()))) {
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				StringBuilder cc = new StringBuilder();
				int size = 0;
				while (r.next()) {
					cc.append(C.char_31).append(r.getInt(1))
					.append(C.char_31).append(r.getString(2));
					size++;
				}
				sb.append(size).append(cc);
			}
		}
		return sb.toString();
	}
	
	public static Set<Integer> getShopIds(FunctionItem fi) throws Exception {
		Set<Integer> list = new HashSet<>();
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(fi.getSql().shop_list_all()))) {
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					list.add(r.getInt(1));
				}
			}
		}
		return list;
	}
	
	public static String getShopListForUser(FunctionItem fi) throws Exception {
		StringBuilder sb = new StringBuilder();
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(fi.getSql().shop_list_for_user()))) {
			pstmt.setInt(1, fi.getSessionInfo().getUserRowId());
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				StringBuilder cc = new StringBuilder();
				int size = 0;
				while (r.next()) {
					cc.append(C.char_31).append(r.getInt(1))
					.append(C.char_31).append(r.getString(2));
					size++;
				}
				sb.append(size).append(cc);
			}
		}
		return sb.toString();
	}
	
	public static Set<Integer> getShopIdsForUser(FunctionItem fi) throws Exception {
		Set<Integer> list = new HashSet<>();
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(fi.getSql().shop_list_for_user()))) {
			pstmt.setInt(1, fi.getSessionInfo().getUserRowId());
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					list.add(r.getInt(1));
				}
			}
		}
		return list;
	}

}
