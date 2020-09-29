package com.indogo.relay.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.lionpig.webui.http.util.DateFormat;
import com.lionpig.webui.http.util.Helper;

public class KursConfiguration implements IFunction, ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.kurs_history;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDesc, true, false, "Last Modified Time"));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, "User"));
		cols.add(new TablePageColumn(C.kurs_value, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Kurs Value"));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.kurs_value).setValue(r.getDouble(C.kurs_value));
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
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		
		if (action.equals(C.update)) {
			double newKursValue = Helper.getDouble(params, "kurs", true);
			
			Connection conn = fi.getConnection().getConnection();
			PreparedStatement pstmt = conn.prepareStatement("insert into kurs_history (lm_time, lm_user, kurs_value) values (?,?,?)");
			try {
				String s = fi.getConnection().getGlobalConfig(C.member, C.kurs_value);
				if (s != null) {
					double oldKursValue = Double.parseDouble(s);
					if (newKursValue == oldKursValue) {
						throw new Exception(C.nothing_to_update);
					}
				}
				
				Timestamp lm_time = fi.getConnection().getCurrentTime();
				
				String stringKursValue = String.valueOf(newKursValue);
				char[] charsKursValue = stringKursValue.toCharArray();
				if (charsKursValue[charsKursValue.length - 2] == '.' && charsKursValue[charsKursValue.length - 1] == '0') {
					stringKursValue = new String(charsKursValue, 0, charsKursValue.length - 2);
				}
				fi.getConnection().setGlobalConfig(C.member, C.kurs_value, stringKursValue, true);
				fi.getConnection().setGlobalConfig(C.member, C.kurs_lm_time, DateFormat.getInstance().format(lm_time), true);
				pstmt.setTimestamp(1, lm_time);
				pstmt.setString(2, fi.getSessionInfo().getUserName());
				pstmt.setDouble(3, newKursValue);
				pstmt.executeUpdate();
				
				conn.commit();
			} catch (Exception e) {
				conn.rollback();
			} finally {
				pstmt.close();
				conn.close();
			}
			
			return String.valueOf(newKursValue);
		} else if (action.equals(C.init)) {
			StringBuilder sb = new StringBuilder();
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			sb.append(roles.size());
			for (RoleNameListModel role : roles) {
				sb.append(C.char_31).append(role.ROLE_ID);
			}
			return sb.toString();
		} else {
			throw new Exception(String.format(C.unknown_action, action));
		}
	}

}
