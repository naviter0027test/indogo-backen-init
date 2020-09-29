package com.indogo.relay.config;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.language.config.AccountConfigurationNaming;
import com.indogo.model.config.UserModel;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.database.RoleNameListModel;
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

import java.sql.Connection;

public class AccountConfiguration implements IFunction, ITablePage {

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp,
			FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		
		if (action.equals(C.insert))
			return this.insert(fi);
		else if (action.equals(C.update))
			return this.update(fi);
		else if (action.equals(C.delete))
			return this.delete(fi);
		else if (action.equals(C.getDataForUpdate)) {
			UserModel m = this.getDataForUpdate(fi, this, Helper.getInt(params, "user_row_id", true));

			StringBuilder sb = new StringBuilder();
			
			sb.append(Helper.replaceNull(m.user_name)).append(C.char_31)
			  .append(Helper.replaceNull(m.email_address)).append(C.char_31)
			  .append(m.disabled ? "Y" : "N").append(C.char_31)
			  .append(Helper.replaceNull(m.password)).append(C.char_31)
			  .append(Helper.replaceNull(m.alias_id)).append(C.char_31)
			  .append(Helper.replaceNull(m.color_id));
			
			sb.append(C.char_30);
			List<RoleNameListModel> userRoles = fi.getConnection().adminUserGetRoles(m.user_name);
			for (RoleNameListModel role : userRoles) {
				sb.append(role.ROLE_ID).append(C.char_31);
			}
			if (userRoles.size() > 0) {
				sb.delete(sb.length() - 1, sb.length());
			}
			
			sb.append(C.char_30);
			sb.append(this.getRoleList(fi));
			
			return sb.toString();
		} else if (action.equals(C.init)) {
			return this.getRoleList(fi);
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return "user_list";
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<TablePageColumn>();
		cols.add(new TablePageColumn("user_name", "STRING", "default", true, false, AccountConfigurationNaming.user_name));
		cols.add(new TablePageColumn("alias_id", "STRING", "default", true, false, AccountConfigurationNaming.alias_id));
		cols.add(new TablePageColumn("email_address", "STRING", "default", true, false, AccountConfigurationNaming.email_address));
		cols.add(new TablePageColumn("role_list", "STRING", "default", false, true, AccountConfigurationNaming.role_list));
		cols.add(new TablePageColumn(C.color_id, "STRING", "default", true, false, "color"));
		cols.add(new TablePageColumn("disabled", "STRING", "default", true, false, AccountConfigurationNaming.disabled));
		cols.add(new TablePageColumn("user_row_id", "NUMBER", "default", true, false, AccountConfigurationNaming.user_row_id));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi,
			Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		String userName = r.getString("user_name");
		
		List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(userName);
		StringBuilder sb = new StringBuilder();
		for (RoleNameListModel role : roles) {
			sb.append(role.ROLE_NAME).append(", ");
		}
		if (roles.size() > 0) {
			sb.delete(sb.length() - 2, sb.length());
		}
		
		cols.get("user_name").setValue(r.getString("user_name"));
		cols.get("alias_id").setValue(r.getString("alias_id"));
		cols.get("email_address").setValue(r.getString("email_address"));
		cols.get("disabled").setValue(r.getString("disabled").equals("Y") ? "Y" : "");
		cols.get("user_row_id").setValue(r.getString("user_row_id"));
		cols.get("role_list").setValue(sb.toString());
		cols.get(C.color_id).setValue(r.getString(C.color_id));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi,
			List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
	}
	
	private String insert(FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String disabled = Helper.getString(params, "disabled", false);
		int[] roleIds = Helper.getIntArray(params, "role_ids", true);
		
		UserModel model = new UserModel();
		model.user_name = Helper.getString(params, "user_name", true, AccountConfigurationNaming.user_name);
		model.email_address = Helper.getString(params, "email_address", true, AccountConfigurationNaming.email_address);
		model.disabled = disabled != null && disabled.equals("Y");
		model.password = Helper.getString(params, "password", true, AccountConfigurationNaming.password);
		model.alias_id = Helper.getString(params, "alias_id", true, AccountConfigurationNaming.alias_id);
		model.color_id = Helper.getString(params, C.color_id, false);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			insert(fi, model, roleIds);

			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter("user_row_id", "NUMBER", "=", String.valueOf(model.user_row_id), null));
			TablePage p = new TablePage();
			String s = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			conn.commit();
			
			return s;
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	private String update(FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String disabled = Helper.getString(params, "disabled", false);
		int[] roleIds = Helper.getIntArray(params, "role_ids", true);
		
		UserModel model = new UserModel();
		model.user_row_id = Helper.getInt(params, "user_row_id", true, AccountConfigurationNaming.user_row_id);
		model.email_address = Helper.getString(params, "email_address", true, AccountConfigurationNaming.email_address);
		model.disabled = disabled != null && disabled.equals("Y");
		model.password = Helper.getString(params, "password", false, AccountConfigurationNaming.password);
		model.alias_id = Helper.getString(params, "alias_id", true, AccountConfigurationNaming.alias_id);
		model.color_id = Helper.getString(params, C.color_id, false);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			update(fi, model, roleIds);

			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter("user_row_id", "NUMBER", "=", String.valueOf(model.user_row_id), null));
			TablePage p = new TablePage();
			String s = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			conn.commit();
			
			return s;
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	private String delete(FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		UserModel model = new UserModel();
		model.user_row_id = Helper.getInt(params, "user_row_id", true, AccountConfigurationNaming.user_row_id);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			delete(fi, model);
			conn.commit();
			return "1";
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}

	public void insert(FunctionItem fi, UserModel model, int[] roleIds) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("INSERT INTO user_list (user_row_id, user_name, email_address, password, disabled, alias_id, color_id) VALUES (?,?,?,?,?,?,?)");
		PreparedStatement pstmtRole = conn.prepareStatement("INSERT INTO user_role (user_row_id, role_id) VALUES (?,?)");
		try {
			model.user_row_id = (int)fi.getConnection().getSeq("user_row_id", true);
			pstmt.setLong(1, model.user_row_id);
			pstmt.setString(2, model.user_name);
			pstmt.setString(3, model.email_address);
			pstmt.setString(4, model.password);
			pstmt.setString(5, model.disabled ? "Y" : "N");
			pstmt.setString(6, model.alias_id);
			pstmt.setString(7, model.color_id);
			pstmt.executeUpdate();
			
			for (int i = 0; i < roleIds.length; i++) {
				pstmtRole.setLong(1, model.user_row_id);
				pstmtRole.setInt(2, roleIds[i]);
				pstmtRole.executeUpdate();
			}
		}
		finally {
			pstmt.close();
		}
	}

	public void update(FunctionItem fi, UserModel model, int[] roleIds) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt1 = conn.prepareStatement("UPDATE user_list SET email_address = ?, password = ?, disabled = ?, alias_id = ?, color_id = ? WHERE user_row_id = ?");
		PreparedStatement pstmt2 = conn.prepareStatement("UPDATE user_list SET email_address = ?, disabled = ?, alias_id = ?, color_id = ? WHERE user_row_id = ?");
		PreparedStatement pstmtDeleteRoles = conn.prepareStatement("DELETE FROM user_role WHERE user_row_id = ?");
		PreparedStatement pstmtRole = conn.prepareStatement("INSERT INTO user_role (user_row_id, role_id) VALUES (?,?)");
		try {
			if (Helper.isNullOrEmpty(model.password)) {
				pstmt2.setString(1, model.email_address);
				pstmt2.setString(2, model.disabled ? "Y" : "N");
				pstmt2.setString(3, model.alias_id);
				pstmt2.setString(4, model.color_id);
				pstmt2.setLong(5, model.user_row_id);
				pstmt2.executeUpdate();
			}
			else {
				pstmt1.setString(1, model.email_address);
				pstmt1.setString(2, model.password);
				pstmt1.setString(3, model.disabled ? "Y" : "N");
				pstmt1.setString(4, model.alias_id);
				pstmt1.setString(5, model.color_id);
				pstmt1.setLong(6, model.user_row_id);
				pstmt1.executeUpdate();
			}
			
			pstmtDeleteRoles.setLong(1, model.user_row_id);
			pstmtDeleteRoles.executeUpdate();
			
			for (int i = 0; i < roleIds.length; i++) {
				pstmtRole.setLong(1, model.user_row_id);
				pstmtRole.setInt(2, roleIds[i]);
				pstmtRole.executeUpdate();
			}
		}
		finally {
			pstmt1.close();
			pstmt2.close();
			pstmtDeleteRoles.close();
			pstmtRole.close();
		}
	}
	
	public void delete(FunctionItem fi, UserModel model) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("DELETE FROM user_list where user_row_id = ?");
		PreparedStatement pstmtRole = conn.prepareStatement("DELETE FROM user_role where user_row_id = ?");
		try {
			pstmtRole.setLong(1, model.user_row_id);
			pstmtRole.executeUpdate();
			
			pstmt.setLong(1, model.user_row_id);
			pstmt.executeUpdate();
		}
		finally {
			pstmt.close();
		}
	}
	
	public UserModel getDataForUpdate(FunctionItem fi, ITablePage page, int user_row_id) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("SELECT user_name, email_address, password, disabled, alias_id, color_id FROM " + page.getTableName() + " WHERE user_row_id = ?");
		ResultSet r;
		try {
			pstmt.setLong(1, user_row_id);
			r = pstmt.executeQuery();
			try {
				if (r.next()) {
					UserModel m = new UserModel();
					m.user_row_id = user_row_id;
					m.user_name = r.getString("user_name");
					m.email_address = r.getString("email_address");
					m.password = r.getString("password");
					m.disabled = Helper.replaceNull(r.getString("disabled")).equals("Y");
					m.alias_id = r.getString("alias_id");
					m.color_id = r.getString("color_id");
					return m;
				}
				else
					throw new Exception("user_row_id [" + user_row_id + "] not exist");
			}
			finally {
				r.close();
			}
		}
		finally {
			pstmt.close();
		}
	}
	
	private String getRoleList(FunctionItem fi) throws Exception {
		StringBuilder sb = new StringBuilder();
		List<RoleNameListModel> roles = fi.getConnection().adminRoleGetAll();
		for (RoleNameListModel role : roles) {
			sb.append(role.ROLE_ID).append(C.char_31)
			.append(role.ROLE_NAME).append(C.char_31)
			.append(role.ROLE_DESC).append(C.char_30);
		}
		if (roles.size() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}
}
