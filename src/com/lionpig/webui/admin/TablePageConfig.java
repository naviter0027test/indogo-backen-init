package com.lionpig.webui.admin;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.database.RoleNameListModel;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class TablePageConfig extends AbstractFunction {

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		if (action.equals(C.update_allow_export)) {
			int role_id = Helper.getInt(params, C.role_id, true);
			int table_page_id = Helper.getInt(params, C.table_page_id, true);
			boolean allow_export = Helper.getInt(params, C.allow_export, true) == 1;
			
			try {
				int updateCount;
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update role_table_page set allow_export = ? where role_id = ? and table_page_id = ?"))) {
					pstmt.setInt(1, allow_export ? 1 : 0);
					pstmt.setInt(2, role_id);
					pstmt.setInt(3, table_page_id);
					updateCount = pstmt.executeUpdate();
				}
				
				if (updateCount == 0) {
					try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into role_table_page (allow_export, role_id, table_page_id) values (?,?,?)"))) {
						pstmt.setInt(1, allow_export ? 1 : 0);
						pstmt.setInt(2, role_id);
						pstmt.setInt(3, table_page_id);
						pstmt.executeUpdate();
					}
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
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select table_page_id, table_name from table_page order by table_name"))) {
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
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select role_id, table_page_id, allow_export from role_table_page"))) {
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				int size = 0;
				StringBuilder ss = new StringBuilder();
				while (r.next()) {
					ss.append(C.char_31).append(r.getInt(1))
					.append(C.char_31).append(r.getInt(2))
					.append(C.char_31).append(r.getInt(3));
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
