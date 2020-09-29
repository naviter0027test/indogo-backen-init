package com.indogo.relay.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.IndogoTable;
import com.indogo.model.member.BankCodeModel;
import com.lionpig.webui.database.HistoryAction;
import com.lionpig.webui.database.HistoryData;
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

public class BankCodeConfiguration implements IFunction, ITablePage {
	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.bank_code_list;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<TablePageColumn>();
		cols.add(new TablePageColumn(C.bank_code, C.columnTypeString, C.columnDirectionDefault, true, false, "Bank Code"));
		cols.add(new TablePageColumn(C.bank_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Bank Name"));
		cols.add(new TablePageColumn(C.swift_code, C.columnTypeString, C.columnDirectionDefault, true, false, "Swift Code"));
		cols.add(new TablePageColumn(C.display_seq, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Display Seq"));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, C.lm_user));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r, boolean isHtml,
			TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.bank_code).setValue(r.getString(C.bank_code));
		cols.get(C.bank_name).setValue(r.getString(C.bank_name));
		cols.get(C.swift_code).setValue(r.getString(C.swift_code));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.display_seq).setValue(r.getInt(C.display_seq));
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
		
		if (action.equals(C.getDataForUpdate)) {
			StringBuilder sb = new StringBuilder();
			String bankCode = Helper.getString(params, "bank_code", true);
			Connection conn = fi.getConnection().getConnection();
			PreparedStatement pstmt = conn.prepareStatement("select bank_name, lm_time, lm_user, display_seq, swift_code from bank_code_list where bank_code = ?");
			try {
				pstmt.setString(1, bankCode);
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
					if (r.next()) {
						sb.append(r.getString(1)).append(C.char_31)
						.append(r.getTimestamp(2)).append(C.char_31)
						.append(r.getString(3)).append(C.char_31)
						.append(r.getInt(4)).append(C.char_31)
						.append(r.getString(5));
					} else {
						throw new Exception(String.format(C.data_not_exist, "bank_code = " + bankCode));
					}
				}
			} finally {
				pstmt.close();
			}
			return sb.toString();
		} else if (action.equals(C.init)) {
			StringBuilder sb = new StringBuilder();
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			sb.append(roles.size());
			for (RoleNameListModel role : roles) {
				sb.append(C.char_31).append(role.ROLE_ID);
			}
			return sb.toString();
		} else if (action.equals(C.insert)) {
			return insert(fi);
		} else if (action.equals(C.update)) {
			return update(fi);
		} else if (action.equals(C.delete)) {
			return delete(fi);
		} else {
			throw new Exception(String.format(C.unknown_action, action));
		}
	}
	
	private String insert(FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String bankCode = Helper.getString(params, C.bank_code, true, "Bank Code");
		String bankName = Helper.getString(params, C.bank_name, true, "Bank Name");
		int displaySeq = Helper.getInt(params, C.display_seq, false);
		String swiftCode = Helper.getString(params, C.swift_code, false);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			BankCodeModel model = new BankCodeModel();
			model.bank_code = bankCode;
			model.bank_name = bankName;
			model.display_seq = displaySeq;
			model.swift_code = swiftCode;
			insert(fi, model);

			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.bank_code, C.columnTypeString, C.operationEqual, model.bank_code, null));
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
		String bankCode = Helper.getString(params, C.bank_code, true, "Bank Code");
		String bankName = Helper.getString(params, C.bank_name, true, "Bank Name");
		Timestamp lmTime = Helper.getTimestamp(params, C.lm_time, true);
		int displaySeq = Helper.getInt(params, C.display_seq, false);
		String swiftCode = Helper.getString(params, C.swift_code, false);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			BankCodeModel model = new BankCodeModel();
			model.bank_code = bankCode;
			model.bank_name = bankName;
			model.lm_time = lmTime;
			model.display_seq = displaySeq;
			model.swift_code = swiftCode;
			update(fi, model);

			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.bank_code, C.columnTypeString, C.operationEqual, model.bank_code, null));
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
		String bankCode = Helper.getString(params, C.bank_code, true, "Bank Code");
		Timestamp lmTime = Helper.getTimestamp(params, C.lm_time, true);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			BankCodeModel model = new BankCodeModel();
			model.bank_code = bankCode;
			model.lm_time = lmTime;
			delete(fi, model);

			conn.commit();
			
			return "1";
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	public void insert(FunctionItem fi, BankCodeModel model) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("insert into bank_code_list (bank_code, bank_name, lm_time, lm_user, display_seq, swift_code) values (?, ?, ?, ?, ?, ?)");
		try {
			model.lm_time = fi.getConnection().getCurrentTime();
			model.lm_user = fi.getSessionInfo().getUserName();
			pstmt.setString(1, model.bank_code);
			pstmt.setString(2, model.bank_name);
			pstmt.setTimestamp(3, model.lm_time);
			pstmt.setString(4, model.lm_user);
			pstmt.setInt(5, model.display_seq);
			pstmt.setString(6, model.swift_code);
			pstmt.executeUpdate();
			
			fi.getConnection().logHistory(IndogoTable.bank_code_list, null, HistoryAction.add, null, model.lm_time, model.lm_user,
					new HistoryData(C.bank_code, null, model.bank_code),
					new HistoryData(C.bank_name, null, model.bank_name),
					new HistoryData(C.display_seq, null, String.valueOf(model.display_seq)),
					new HistoryData(C.swift_code, null, model.swift_code));
		} finally {
			pstmt.close();
		}
	}
	
	public void update(FunctionItem fi, BankCodeModel model) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmtLock = conn.prepareStatement("select bank_name, lm_time, display_seq, swift_code from bank_code_list where bank_code = ? for update");
		PreparedStatement pstmtUpdate = conn.prepareStatement("update bank_code_list set bank_name = ?, lm_time = ?, lm_user = ?, display_seq = ?, swift_code = ? where bank_code = ?");
		try {
			String bankName, swiftCode;
			Timestamp lmTime;
			int displaySeq;
			pstmtLock.setString(1, model.bank_code);
			ResultSet r = pstmtLock.executeQuery();
			try {
				if (r.next()) {
					bankName = r.getString(1);
					lmTime = r.getTimestamp(2);
					displaySeq = r.getInt(3);
					swiftCode = r.getString(4);
				} else {
					throw new Exception(String.format(C.data_not_exist, "bank_code = " + model.bank_code));
				}
			} finally {
				r.close();
			}
			
			if (lmTime.getTime() != model.lm_time.getTime()) {
				throw new Exception(String.format(C.data_already_updated_by_another_user, "bank_code = " + model.bank_code));
			}
			
			model.lm_time = fi.getConnection().getCurrentTime();
			model.lm_user = fi.getSessionInfo().getUserName();
			pstmtUpdate.setString(1, model.bank_name);
			pstmtUpdate.setTimestamp(2, model.lm_time);
			pstmtUpdate.setString(3, model.lm_user);
			pstmtUpdate.setInt(4, model.display_seq);
			pstmtUpdate.setString(5, model.swift_code);
			pstmtUpdate.setString(6, model.bank_code);
			pstmtUpdate.executeUpdate();
			
			fi.getConnection().logHistory(IndogoTable.bank_code_list, null, HistoryAction.update, null, model.lm_time, model.lm_user,
					new HistoryData(C.bank_code, model.bank_code, model.bank_code),
					new HistoryData(C.bank_name, bankName, model.bank_name),
					new HistoryData(C.display_seq, String.valueOf(displaySeq), String.valueOf(model.display_seq)),
					new HistoryData(C.swift_code, swiftCode, model.swift_code));
		} finally {
			pstmtLock.close();
			pstmtUpdate.close();
		}
	}
	
	public void delete(FunctionItem fi, BankCodeModel model) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmtLock = conn.prepareStatement("select bank_name, lm_time, display_seq from bank_code_list where bank_code = ? for update");
		PreparedStatement pstmtDelete = conn.prepareStatement("delete from bank_code_list where bank_code = ?");
		try {
			String bankName;
			Timestamp lm_time;
			int displaySeq;
			pstmtLock.setString(1, model.bank_code);
			ResultSet r = pstmtLock.executeQuery();
			try {
				if (r.next()) {
					bankName = r.getString(1);
					lm_time = r.getTimestamp(2);
					displaySeq = r.getInt(3);
				} else {
					throw new Exception(String.format(C.data_not_exist, "bank_code = " + model.bank_code));
				}
			} finally {
				r.close();
			}
			
			if (lm_time.getTime() != model.lm_time.getTime()) {
				throw new Exception(String.format(C.data_already_updated_by_another_user, "bank_code = " + model.bank_code));
			}
			
			pstmtDelete.setString(1, model.bank_code);
			pstmtDelete.executeUpdate();
			
			fi.getConnection().logHistory(IndogoTable.bank_code_list, null, HistoryAction.delete, null, fi.getConnection().getCurrentTime(), fi.getSessionInfo().getUserName(),
					new HistoryData(C.bank_code, model.bank_code, null),
					new HistoryData(C.bank_name, bankName, null),
					new HistoryData(C.display_seq, String.valueOf(displaySeq), null)
					);
		} finally {
			pstmtLock.close();
			pstmtDelete.close();
		}
	}
	
	public enum OrderBy {
		bank_code, bank_name;
	}
	
	public List<BankCodeModel> getBankCodeList(FunctionItem fi, OrderBy orderBy) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		Statement stmt = conn.createStatement();
		try {
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(stmt.executeQuery("select bank_code, bank_name, display_seq, swift_code from bank_code_list order by display_seq desc, " + orderBy.name()))) {
				List<BankCodeModel> list = new ArrayList<>();
				while (r.next()) {
					BankCodeModel m = new BankCodeModel();
					m.bank_code = r.getString(1);
					m.bank_name = r.getString(2);
					m.swift_code = r.getString(4);
					list.add(m);
				}
				return list;
			}
		} finally {
			stmt.close();
		}
	}
}
