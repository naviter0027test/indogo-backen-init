package com.lionpig.webui.admin;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.database.RoleNameListModel;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class RoleConfig extends AbstractFunction {

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		if (action.equals(C.create_new_role)) {
			String role_name = Helper.getString(params, C.role_name, true);
			String role_desc = Helper.getString(params, C.role_desc, true);
			
			try {
				Integer max_role_id;
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select max(role_id) from role_name_list"))) {
					try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
						r.next();
						max_role_id = r.getInt(1);
					}
				}
				
				if (max_role_id == null) {
					max_role_id = 1;
				} else {
					max_role_id++;
				}
				
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into role_name_list (role_id, role_name, role_desc) values (?, ?, ?)"))) {
					pstmt.setInt(1, max_role_id);
					pstmt.setString(2, role_name);
					pstmt.setString(3, role_desc);
					pstmt.executeUpdate();
				}
				
				conn.commit();
				
				return String.valueOf(max_role_id);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.delete_role)) {
			int role_id = Helper.getInt(params, C.role_id, true);
			
			try {
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from user_role where role_id = ?"))) {
					pstmt.setInt(1, role_id);
					pstmt.executeUpdate();
				}
				
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from role_menu where role_id = ?"))) {
					pstmt.setInt(1, role_id);
					pstmt.executeUpdate();
				}
				
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from role_name_list where role_id = ?"))) {
					pstmt.setInt(1, role_id);
					pstmt.executeUpdate();
				}
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
		Connection conn = fi.getConnection().getConnection();
		
		StringBuilder sb = new StringBuilder();
		List<RoleNameListModel> roles = fi.getConnection().adminRoleGetAll();
		sb.append(roles.size());
		for (RoleNameListModel m : roles) {
			sb.append(C.char_31).append(m.ROLE_ID)
			.append(C.char_31).append(m.ROLE_NAME)
			.append(C.char_31).append(m.ROLE_DESC);
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select path_row_id, path_name from menu_path"))) {
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				int size = 0;
				StringBuilder ss = new StringBuilder();
				while (r.next()) {
					ss.append(C.char_31).append(r.getInt(1))
					.append(C.char_31).append(r.getString(2));
					size++;
				}
				sb.append(C.char_31).append(size).append(ss);
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select menu_row_id, title, path_row_id, display_seq from menu order by display_seq"))) {
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				int size = 0;
				StringBuilder ss = new StringBuilder();
				while (r.next()) {
					ss.append(C.char_31).append(r.getInt(1))
					.append(C.char_31).append(r.getString(2))
					.append(C.char_31).append(r.getInt(3))
					.append(C.char_31).append(r.getInt(4));
					size++;
				}
				sb.append(C.char_31).append(size).append(ss);
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select role_id, menu_row_id from role_menu"))) {
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				int size = 0;
				StringBuilder ss = new StringBuilder();
				while (r.next()) {
					ss.append(C.char_31).append(r.getInt(1))
					.append(C.char_31).append(r.getInt(2));
					size++;
				}
				sb.append(C.char_31).append(size).append(ss);
			}
		}
		
		return sb.toString();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int role_id = Helper.getInt(params, C.role_id, true);
		int menu_row_id = Helper.getInt(params, C.menu_row_id, true);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into role_menu (role_id, menu_row_id) values (?, ?)"))) {
				pstmt.setInt(1, role_id);
				pstmt.setInt(2, menu_row_id);
				pstmt.executeUpdate();
			}
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
		return null;
	}

	@Override
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int role_id = Helper.getInt(params, C.role_id, true);
		int menu_row_id = Helper.getInt(params, C.menu_row_id, true);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from role_menu where role_id = ? and menu_row_id = ?"))) {
				pstmt.setInt(1, role_id);
				pstmt.setInt(2, menu_row_id);
				pstmt.executeUpdate();
			}
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
		return null;
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
