package com.indogo.relay.member;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.MemberHistory;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.database.RoleNameListModel;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class MemberPoint implements ITablePage, IFunction {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.member;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<TablePageColumn>();
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, " "));
		cols.add(new TablePageColumn(C.member_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Member Name"));
		cols.add(new TablePageColumn(C.remit_point, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Point"));
		cols.add(new TablePageColumn(C.arc_no, C.columnTypeString, C.columnDirectionDefault, true, false, "ARC"));
		cols.add(new TablePageColumn(C.arc_expire_date, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "ARC Expire"));
		cols.add(new TablePageColumn(C.address, C.columnTypeString, C.columnDirectionDefault, true, false, "Address"));
		cols.add(new TablePageColumn(C.birthday, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Birthday"));
		cols.add(new TablePageColumn(C.app_phone_no, C.columnTypeString, C.columnDirectionDefault, true, false, "App Phone No"));
		cols.add(new TablePageColumn(C.member_login_id, C.columnTypeString, C.columnDirectionDefault, true, false, "App Login Id"));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, false));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, C.lm_user));
		cols.add(new TablePageColumn(C.status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.status_id, true, false));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		int status_id = r.unwrap().unwrap().getInt(C.status_id);
		
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
		cols.get(C.remit_point).setValue(r.getInt(C.remit_point));
		cols.get(C.member_name).setValue(r.getString(C.member_name));
		cols.get(C.app_phone_no).setValue(r.getString(C.app_phone_no));
		cols.get(C.arc_no).setValue(r.getString(C.arc_no));
		cols.get(C.arc_expire_date).setValue(r.getDate(C.arc_expire_date));
		cols.get(C.address).setValue(r.getString(C.address));
		cols.get(C.birthday).setValue(r.getDate(C.birthday));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.status_id).setValue(String.valueOf(status_id));
		cols.get(C.member_login_id).setValue(r.getString(C.member_login_id));

		if (status_id == 1) {
			rowAttr.HtmlClass.add("banned");
		}
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		filter.add(new TablePageFilter(C.is_wait_confirm, C.columnTypeNumber, C.operationIn, "3,4", null));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
	}

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		Connection conn = fi.getConnection().getConnection();
		
		if (action.equals(C.init)) {
			StringBuilder sb = new StringBuilder();
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			sb.append(roles.size());
			for (RoleNameListModel role : roles) {
				sb.append(C.char_31).append(role.ROLE_ID);
			}
			sb.append(C.char_31).append(fi.getConnection().getGlobalConfig(C.kotsms, C.msg_footnote));
			return sb.toString();
		} else if (action.equals(C.change_password)) {
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			boolean isAllowed = false;
			for (RoleNameListModel role : roles) {
				if (role.ROLE_ID == 1 || role.ROLE_ID == 2 || role.ROLE_ID == 3) {
					isAllowed = true;
					break;
				}
			}
			
			if (!isAllowed) {
				throw new Exception(String.format(C.function_not_allowed, "Change Password"));
			}
			
			long member_id = Helper.getLong(params, C.member_id, true);
			String password = Helper.getString(params, C.password, true);
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.reset();
			byte[] hash = digest.digest(password.getBytes());
			String passwordHashed = String.format("%0" + (hash.length * 2) + 'x', new BigInteger(1, hash));
			
			Timestamp lm_time = fi.getConnection().getCurrentTime();
			
			try {
				try (PreparedStatement pstmt = conn.prepareStatement("update member set member_password = ? where member_id = ?")) {
					pstmt.setString(1, passwordHashed);
					pstmt.setLong(2, member_id);
					pstmt.executeUpdate();
				}
				
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into member_history (member_id, lm_time, lm_user, hst_id) values (?,?,?,?)"))) {
					pstmt.setLong(1, member_id);
					pstmt.setTimestamp(2, lm_time);
					pstmt.setString(3, fi.getSessionInfo().getUserName());
					pstmt.setInt(4, MemberHistory.reset_password.getId());
					pstmt.executeUpdate();
				}
				
				conn.commit();
				return C.emptyString;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.modify_app_phone_no)) {
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			boolean isAllowed = false;
			for (RoleNameListModel role : roles) {
				if (role.ROLE_ID == 1 || role.ROLE_ID == 2 || role.ROLE_ID == 3) {
					isAllowed = true;
					break;
				}
			}
			
			if (!isAllowed) {
				throw new Exception(String.format(C.function_not_allowed, "Edit App Phone No"));
			}
			
			long member_id = Helper.getLong(params, C.member_id, true);
			String app_phone_no = Helper.getString(params, C.app_phone_no, true);
			
			Timestamp lm_time = fi.getConnection().getCurrentTime();
			try {
				String old_app_phone_no;
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select app_phone_no from member where member_id = ?"))) {
					pstmt.setLong(1, member_id);
					try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
						if (r.next()) {
							old_app_phone_no = r.getString(1);
						} else {
							throw new Exception("member_id = " + member_id + " not exist");
						}
					}
				}
				
				try (PreparedStatement pstmt = conn.prepareStatement("update member set app_phone_no = ? where member_id = ?")) {
					pstmt.setString(1, app_phone_no);
					pstmt.setLong(2, member_id);
					pstmt.executeUpdate();
				}
				
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into member_history (member_id, lm_time, lm_user, hst_id, hst_desc) values (?,?,?,?,?)"))) {
					pstmt.setLong(1, member_id);
					pstmt.setTimestamp(2, lm_time);
					pstmt.setString(3, fi.getSessionInfo().getUserName());
					pstmt.setInt(4, MemberHistory.change_app_phone_no.getId());
					pstmt.setString(5, "old: " + old_app_phone_no + ", new: " + app_phone_no);
					pstmt.executeUpdate();
				}
				
				conn.commit();
				return C.emptyString;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.modify_member_login_id)) {
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			boolean isAllowed = false;
			for (RoleNameListModel role : roles) {
				if (role.ROLE_ID == 1 || role.ROLE_ID == 2) {
					isAllowed = true;
					break;
				}
			}
			
			if (!isAllowed) {
				throw new Exception(String.format(C.function_not_allowed, "Edit App Login Id"));
			}
			
			long member_id = Helper.getLong(params, C.member_id, true);
			String member_login_id = Helper.getString(params, C.member_login_id, true);
			
			Timestamp lm_time = fi.getConnection().getCurrentTime();
			try {
				String old_member_login_id;
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select member_login_id from member where member_id = ?"))) {
					pstmt.setLong(1, member_id);
					try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
						if (r.next()) {
							old_member_login_id = r.getString(1);
						} else {
							throw new Exception("member_id = " + member_id + " not exist");
						}
					}
				}
				
				try (PreparedStatement pstmt = conn.prepareStatement("update member set member_login_id = ? where member_id = ?")) {
					pstmt.setString(1, member_login_id);
					pstmt.setLong(2, member_id);
					pstmt.executeUpdate();
				}
				
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into member_history (member_id, lm_time, lm_user, hst_id, hst_desc) values (?,?,?,?,?)"))) {
					pstmt.setLong(1, member_id);
					pstmt.setTimestamp(2, lm_time);
					pstmt.setString(3, fi.getSessionInfo().getUserName());
					pstmt.setInt(4, MemberHistory.change_app_login_id.getId());
					pstmt.setString(5, "old: " + old_member_login_id + ", new: " + member_login_id);
					pstmt.executeUpdate();
				}
				
				conn.commit();
				return C.emptyString;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.get_history)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			int hst_id = Helper.getInt(params, C.hst_id, true);
			
			StringBuilder result = new StringBuilder();
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select lm_time, lm_user, hst_desc from member_history where member_id = ? and hst_id = ? order by lm_time desc limit 50"))) {
				pstmt.setLong(1, member_id);
				pstmt.setInt(2, hst_id);
				try (ResultSetWrapperStringify r = pstmt.executeQueryWrapperStringify()) {
					int count = 0;
					StringBuilder sb = new StringBuilder();
					while (r.next()) {
						sb.append(C.char_31).append(r.getTimestamp(1))
						.append(C.char_31).append(r.getString(2))
						.append(C.char_31).append(r.getString(3));
						count++;
					}
					
					result.append(count).append(sb);
				}
			}
			return result.toString();
		} else if (action.equals(C.get_histories)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			
			StringBuilder result = new StringBuilder();
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select lm_time, lm_user, hst_id, hst_desc from member_history where member_id = ? order by lm_time desc limit 50"))) {
				pstmt.setLong(1, member_id);
				try (ResultSetWrapperStringify r = pstmt.executeQueryWrapperStringify()) {
					int count = 0;
					StringBuilder sb = new StringBuilder();
					while (r.next()) {
						sb.append(C.char_31).append(r.getTimestamp(1))
						.append(C.char_31).append(r.getString(2))
						.append(C.char_31).append(MemberHistory.get(r.unwrap().getInt(3)).getDisplay())
						.append(C.char_31).append(r.getString(4));
						count++;
					}
					
					result.append(count).append(sb);
				}
			}
			return result.toString();
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}
}
