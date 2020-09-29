package com.indogo.relay.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.model.member.MemberPointHistoryInsertResult;
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

public class MemberPointHistory implements ITablePage, IFunction {
	
	private HashMap<Integer, String> reasons = new HashMap<>();

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.member_point;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<TablePageColumn>();
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDesc, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.remit_point, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Point"));
		cols.add(new TablePageColumn(C.reason_desc, C.columnTypeString, C.columnDirectionDefault, true, false, "Description"));
		cols.add(new TablePageColumn(C.reason_name, C.columnTypeString, C.columnDirectionNone, false, true, C.reason_name));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, false));
		cols.add(new TablePageColumn(C.reason_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.reason_id, true, false));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		int reason_id = r.unwrap().unwrap().getInt(C.reason_id);
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.remit_point).setValue(r.getInt(C.remit_point));
		cols.get(C.reason_name).setValue(reasons.get(reason_id));
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
		cols.get(C.reason_id).setValue(String.valueOf(reason_id));
		cols.get(C.reason_desc).setValue(r.getString(C.reason_desc));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		Statement stmt = fi.getConnection().getConnection().createStatement();
		try {
			ResultSet r = stmt.executeQuery("select reason_id, reason_name from member_point_reason");
			try {
				while (r.next()) {
					int reason_id = r.getInt(1);
					String reason_name = r.getString(2);
					reasons.put(reason_id, reason_name);
				}
			} finally {
				r.close();
			}
		} finally {
			stmt.close();
		}
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
	}

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		if (action.equals(C.insert)) {
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			boolean isAllowed = false;
			for (RoleNameListModel role : roles) {
				if (role.ROLE_ID == 1 || role.ROLE_ID == 2 || role.ROLE_ID == 3) {
					isAllowed = true;
					break;
				}
			}
			
			if (!isAllowed) {
				throw new Exception(String.format(C.function_not_allowed, "Add Point"));
			}
			
			return insert(fi, params);
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}

	private String insert(FunctionItem fi, Hashtable<String, String> params) throws Exception {
		long member_id = Helper.getLong(params, C.member_id, true);
		int remit_point = Helper.getInt(params, C.remit_point, true);
		String reason_desc = Helper.getString(params, C.reason_desc, true);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			MemberPointHistoryInsertResult result = insert(fi, member_id, remit_point, reason_desc);
			
			conn.commit();
			
			return String.valueOf(result.total_point);
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	private MemberPointHistoryInsertResult insert(FunctionItem fi, long member_id, int remit_point, String reason_desc) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select remit_point from member where member_id = ? for update");
		PreparedStatement pstmtUpdate = conn.prepareStatement("update member set remit_point = ? where member_id = ?");
		PreparedStatement pstmtInsert = conn.prepareStatement("insert into member_point (member_id, lm_time, remit_point, reason_id, reason_desc) values (?,?,?,5,?)");
		try {
			int total_point;
			pstmt.setLong(1, member_id);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					total_point = r.getInt(1);
				} else
					throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id));
			} finally {
				r.close();
			}
			
			total_point = total_point + remit_point;
			Timestamp lm_time = fi.getConnection().getCurrentTime();
			
			pstmtUpdate.setInt(1, total_point);
			pstmtUpdate.setLong(2, member_id);
			pstmtUpdate.executeUpdate();
			
			pstmtInsert.setLong(1, member_id);
			pstmtInsert.setTimestamp(2, lm_time);
			pstmtInsert.setInt(3, remit_point);
			pstmtInsert.setString(4, reason_desc);
			pstmtInsert.executeUpdate();
			
			MemberPointHistoryInsertResult result = new MemberPointHistoryInsertResult();
			result.lm_time = lm_time;
			result.total_point = total_point;
			return result;
		} finally {
			pstmt.close();
			pstmtUpdate.close();
			pstmtInsert.close();
		}
	}
}
