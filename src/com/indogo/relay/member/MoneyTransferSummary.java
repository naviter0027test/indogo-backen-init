package com.indogo.relay.member;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.indogo.MemberSex;
import com.indogo.MiniMart;
import com.indogo.PaymentType;
import com.indogo.TransferStatus;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;
import com.lionpig.webui.http.util.Stringify;

public class MoneyTransferSummary implements ITablePage, IFunction {
	
	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.money_transfer_v;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.lm_time_paid, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "繳款日期"));
		cols.add(new TablePageColumn(C.lm_time_transfer, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "匯款日期"));
		cols.add(new TablePageColumn(C.member_name, C.columnTypeString, C.columnDirectionDefault, true, false, "委託人（外籍勞工）"));
		cols.add(new TablePageColumn(C.phone_no, C.columnTypeString, C.columnDirectionDefault, true, false, "在臺居所電話"));
		cols.add(new TablePageColumn(C.arc_no, C.columnTypeString, C.columnDirectionDefault, true, false, "外僑居留證號碼"));
		cols.add(new TablePageColumn(C.arc_expire_date, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "外僑居留證有效期限"));
		cols.add(new TablePageColumn(C.birthday, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "生日"));
		cols.add(new TablePageColumn(C.nationality, C.columnTypeString, C.columnDirectionNone, false, true, "國籍"));
		cols.add(new TablePageColumn(C.transfer_amount_ntd, C.columnTypeNumber, C.columnDirectionDefault, true, false, "委託結匯金額"));
		cols.add(new TablePageColumn(C.payment_name, C.columnTypeString, C.columnDirectionNone, false, true, "匯款方式", true));
		cols.add(new TablePageColumn(C.mini_mart_name, C.columnTypeString, C.columnDirectionNone, false, true, "通路", true));
		cols.add(new TablePageColumn(C.transfer_status_name, C.columnTypeString, C.columnDirectionNone, false, true, "交易狀態"));
		cols.add(new TablePageColumn(C.transfer_through_bank_name, C.columnTypeString, C.columnDirectionDefault, true, false, "匯款銀行"));
		cols.add(new TablePageColumn(C.bank_name, C.columnTypeString, C.columnDirectionDefault, true, false, "入款銀行"));
		cols.add(new TablePageColumn(C.swift_code, C.columnTypeString, C.columnDirectionDefault, true, false, "SWIFT"));
		cols.add(new TablePageColumn(C.export_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Export Bank Format DateTime"));
		cols.add(new TablePageColumn(C.status_name, C.columnTypeString, C.columnDirectionNone, false, true, "Member Status"));
		cols.add(new TablePageColumn(C.recipient_birthday, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Recipient Birthday"));
		cols.add(new TablePageColumn(C.id_filename, C.columnTypeString, C.columnDirectionDefault, true, false, "Recipient ID Filename"));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, false));
		cols.add(new TablePageColumn(C.payment_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.payment_id, true, false));
		cols.add(new TablePageColumn(C.mini_mart_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.mini_mart_id, true, false));
		cols.add(new TablePageColumn(C.is_app, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_app, false, false));
		cols.add(new TablePageColumn(C.transfer_status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.transfer_status_id, true, true));
		cols.add(new TablePageColumn(C.recipient_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.recipient_id, true, true));
		cols.add(new TablePageColumn(C.txn_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, "廠商訂單編號", false, false));
		cols.add(new TablePageColumn(C.status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.status_id, true, true));
		cols.add(new TablePageColumn(C.payment_info, C.columnTypeString, C.columnDirectionDefault, true, false, "條碼"));
		cols.add(new TablePageColumn(C.recipient_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Recipient Name"));
		cols.add(new TablePageColumn(C.recipient_name_2, C.columnTypeString, C.columnDirectionDefault, true, false, "Recipient Name 2"));
		cols.add(new TablePageColumn(C.bank_code, C.columnTypeString, C.columnDirectionDefault, true, false, "Bank Code"));
		cols.add(new TablePageColumn(C.bank_acc, C.columnTypeString, C.columnDirectionDefault, true, false, "Bank Account"));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		Integer mini_mart_id = r.unwrap().getInt(C.mini_mart_id);
		String mini_mart_name = C.emptyString;
		if (mini_mart_id != null) {
			mini_mart_name = MiniMart.get(mini_mart_id).getDisplay();
		}
		PaymentType paymentType = PaymentType.get(r.unwrap().getInt(C.payment_id));
		TransferStatus transferStatus = TransferStatus.get(r.unwrap().getInt(C.transfer_status_id));
		int status_id = r.unwrap().unwrap().getInt(C.status_id);
		
		String[] phone_nos = r.getString(C.phone_no).split("\\.");
		for (String phone_no : phone_nos) {
			if (phone_no.length() > 0) {
				cols.get(C.phone_no).setValue(phone_no);
				break;
			}
		}
		
		cols.get(C.lm_time_paid).setValue(r.getTimestamp(C.lm_time_paid));
		cols.get(C.lm_time_transfer).setValue(r.getTimestamp(C.lm_time_transfer));
		cols.get(C.member_name).setValue(r.getString(C.member_name));
		cols.get(C.arc_no).setValue(r.getString(C.arc_no));
		cols.get(C.arc_expire_date).setValue(r.getDate(C.arc_expire_date));
		cols.get(C.birthday).setValue(r.getDate(C.birthday));
		cols.get(C.nationality).setValue("印尼");
		cols.get(C.transfer_amount_ntd).setValue(r.getIntCurrency(C.transfer_amount_ntd));
		cols.get(C.payment_name).setValue(paymentType.getDisplay());
		cols.get(C.mini_mart_name).setValue(mini_mart_name);
		cols.get(C.transfer_status_name).setValue(transferStatus.name());
		cols.get(C.transfer_through_bank_name).setValue(r.getString(C.transfer_through_bank_name));
		cols.get(C.bank_name).setValue(r.getString(C.bank_name));
		cols.get(C.swift_code).setValue(r.getString(C.swift_code));
		cols.get(C.export_time).setValue(r.getTimestamp(C.export_time));
		cols.get(C.status_name).setValue(status_id == 0 ? C.ACTIVE : C.INACTIVE);
		cols.get(C.recipient_birthday).setValue(r.getDate(C.recipient_birthday));
		cols.get(C.id_filename).setValue(r.getString(C.id_filename));
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
		cols.get(C.payment_id).setValue(String.valueOf(paymentType.getId()));
		cols.get(C.mini_mart_id).setValue(mini_mart_id == null ? C.emptyString : String.valueOf(mini_mart_id));
		cols.get(C.is_app).setValue(r.getInt(C.is_app));
		cols.get(C.transfer_status_id).setValue(String.valueOf(transferStatus.getId()));
		cols.get(C.recipient_id).setValue(r.getInt(C.recipient_id));
		cols.get(C.txn_id).setValue(r.getLong(C.txn_id));
		cols.get(C.status_id).setValue(String.valueOf(status_id));
		cols.get(C.payment_info).setValue(r.getString(C.payment_info));
		cols.get(C.recipient_name).setValue(r.getString(C.recipient_name));
		cols.get(C.recipient_name_2).setValue(r.getString(C.recipient_name_2));
		cols.get(C.bank_code).setValue(r.getString(C.bank_code));
		cols.get(C.bank_acc).setValue(r.getString(C.bank_acc));
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
		Connection conn = fi.getConnection().getConnection();
		
		if (action.equals(C.init)) {
			return MoneyTransfer.getTransferStatusList(fi);
		} else if (action.equals(C.sql_temp_table)) {
			TransferStatus status = TransferStatus.get(Helper.getInt(params, C.transfer_status_id, true));
			Timestamp start_time = Helper.getTimestamp(params, C.start_time, true);
			Timestamp end_time = Helper.getTimestamp(params, C.end_time, true);
			String[] bank_codes = Helper.getStringArray(params, C.bank_codes, true);
			String query_type = Helper.getString(params, C.query_type, true);
			
			try {
				int count;
				if (query_type.equals(C.include)) {
					count = sqlTempTableTransferToBankCode(fi, status, start_time, end_time, bank_codes);
				} else if (query_type.equals(C.exclude)) {
					count = sqlTempTableTransferToBankCodeExclude(fi, status, start_time, end_time, bank_codes);
				} else {
					throw new Exception("not supported");
				}
				conn.commit();
				return String.valueOf(count);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.export_bank_format)) {
			long[] txn_ids = Helper.getLongArray(params, C.txn_ids, true);
			try {
				String s = exportBankFormat(fi, txn_ids);
				conn.commit();
				return s;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else {
			throw new Exception(String.format(C.unknown_action, action));
		}
	}
	
	public int sqlTempTableTransferToBankCode(FunctionItem fi, TransferStatus status, Timestamp start_time, Timestamp end_time, String[] bank_codes) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		String lm_time_column_name;
		switch (status) {
			case paid:
				lm_time_column_name = C.lm_time_paid;
				break;
			case transfered:
				lm_time_column_name = C.lm_time_transfer;
				break;
			default:
				throw new Exception("not supported");
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bank_codes.length; i++) {
			sb.append("?,");
		}
		sb.delete(sb.length() - 1, sb.length());
		
		try (PreparedStatement pstmt = conn.prepareStatement("delete from sql_temp_table where session_id = ? and page_id = ?")) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.money_transfer_summary);
			pstmt.executeUpdate();
		}
		
		List<Long> txn_ids = new ArrayList<>();
		try (PreparedStatement pstmt = conn.prepareStatement("select a.txn_id from money_transfer a, member_recipient b where a.member_id = b.member_id and a.recipient_id = b.recipient_id and a." + lm_time_column_name + " between ? and ? and b.bank_code in (" + sb + ")")) {
			pstmt.setTimestamp(1, start_time);
			pstmt.setTimestamp(2, end_time);
			for (int i = 0; i < bank_codes.length; i++) {
				pstmt.setString(i+3, bank_codes[i]);
			}
			try (ResultSet r = pstmt.executeQuery()) {
				while (r.next()) {
					txn_ids.add(r.getLong(1));
				}
			}
		}
		
		if (txn_ids.size() > 0) {
			try (PreparedStatement pstmt = conn.prepareStatement("insert into sql_temp_table (session_id, page_id, long1) values (?,?,?)")) {
				int batchSize = 0;
				for (long txn_id : txn_ids) {
					pstmt.setString(1, fi.getSID());
					pstmt.setString(2, C.money_transfer_summary);
					pstmt.setLong(3, txn_id);
					pstmt.addBatch();
					batchSize++;
					if (batchSize >= 5000) {
						pstmt.executeBatch();
						batchSize = 0;
					}
				}
				if (batchSize > 0) {
					pstmt.executeBatch();
				}
				return txn_ids.size();
			}
		} else {
			return 0;
		}
	}
	
	public int sqlTempTableTransferToBankCodeExclude(FunctionItem fi, TransferStatus status, Timestamp start_time, Timestamp end_time, String[] bank_codes) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		String lm_time_column_name;
		switch (status) {
			case paid:
				lm_time_column_name = C.lm_time_paid;
				break;
			case transfered:
				lm_time_column_name = C.lm_time_transfer;
				break;
			default:
				throw new Exception("not supported");
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bank_codes.length; i++) {
			sb.append("?,");
		}
		sb.delete(sb.length() - 1, sb.length());
		
		try (PreparedStatement pstmt = conn.prepareStatement("delete from sql_temp_table where session_id = ? and page_id = ?")) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.money_transfer_summary);
			pstmt.executeUpdate();
		}
		
		List<Long> txn_ids = new ArrayList<>();
		try (PreparedStatement pstmt = conn.prepareStatement("select a.txn_id from money_transfer a, member_recipient b where a.member_id = b.member_id and a.recipient_id = b.recipient_id and a." + lm_time_column_name + " between ? and ? and b.bank_code not in (" + sb + ")")) {
			pstmt.setTimestamp(1, start_time);
			pstmt.setTimestamp(2, end_time);
			for (int i = 0; i < bank_codes.length; i++) {
				pstmt.setString(i+3, bank_codes[i]);
			}
			try (ResultSet r = pstmt.executeQuery()) {
				while (r.next()) {
					txn_ids.add(r.getLong(1));
				}
			}
		}
		
		if (txn_ids.size() > 0) {
			try (PreparedStatement pstmt = conn.prepareStatement("insert into sql_temp_table (session_id, page_id, long1) values (?,?,?)")) {
				int batchSize = 0;
				for (long txn_id : txn_ids) {
					pstmt.setString(1, fi.getSID());
					pstmt.setString(2, C.money_transfer_summary);
					pstmt.setLong(3, txn_id);
					pstmt.addBatch();
					batchSize++;
					if (batchSize >= 5000) {
						pstmt.executeBatch();
						batchSize = 0;
					}
				}
				if (batchSize > 0) {
					pstmt.executeBatch();
				}
				return txn_ids.size();
			}
		} else {
			return 0;
		}
	}
	
	public String exportBankFormat(FunctionItem fi, long[] txn_ids) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(sql.clear_temp_table())) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.money_transfer_summary);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatement pstmt = conn.prepareStatement(sql.insert_temp_table_long1())) {
			int batchSize = 0;
			for (long txn_id : txn_ids) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.money_transfer_summary);
				pstmt.setLong(3, txn_id);
				pstmt.addBatch();
				batchSize++;
				if (batchSize >= 5000) {
					pstmt.executeBatch();
					batchSize = 0;
				}
			}
			if (batchSize > 0) {
				pstmt.executeBatch();
			}
		}
		
		Timestamp currentTime = fi.getConnection().getCurrentTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd");
		String filename = "COMBINED_foreign_" + dateFormat.format(currentTime) + ".txt";
		File file = new File(fi.getTempFolder(), filename);
		try (PrintWriter pw = new PrintWriter(file)) {
			try (PreparedStatement pstmt = conn.prepareStatement(sql.money_transfer_get_for_export_bank_format())) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.money_transfer_summary);
				try (ResultSet r = pstmt.executeQuery()) {
					while (r.next()) {
						String member_name = r.getString(1);
						String arc_no = r.getString(2);
						Timestamp birthday = r.getTimestamp(3);
						MemberSex sex = MemberSex.get(r.getInt(4));
						int status_id = r.getInt(5);
						
						pw.print(StringUtils.rightPad(member_name, 60, ' '));
						pw.print("|FOREIGN");
						pw.print(StringUtils.rightPad(arc_no, 20, ' '));
						pw.print("|                                                  |                                                  |                                                  |                                                  |                                                  |                                                  |                                                  |                                                  |                                        |INDIVIDUAL                                        |                                        |                                        |                                        |                                        |                                        |                                        |                                        |        |ID        |INDONESIA                                         |TW                            |TAIWAN                                            |");
						pw.print(dateFormat2.format(birthday));
						pw.print('|');
						pw.print(sex.getShortName());
						pw.print("|CIF164              |FOREIGN");
						pw.print(StringUtils.rightPad(arc_no, 17, ' '));
						pw.print("|00|");
						pw.print(status_id == 0 ? C.ACTIVE + "  " : C.INACTIVE);
						pw.print("|");
						pw.print(dateFormat2.format(currentTime));
						pw.println();
						pw.println();
					}
				}
			}
		}
		
		List<Long> txn_ids_sorted = new ArrayList<>();
		for (long txn_id : txn_ids) {
			txn_ids_sorted.add(txn_id);
		}
		Collections.sort(txn_ids_sorted);
		
		try (PreparedStatementWrapper pstmtLock = new PreparedStatementWrapper(conn.prepareStatement("select export_time from money_transfer where txn_id = ? for update"))) {
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update money_transfer set export_time = ? where txn_id = ?"))) {
				for (long txn_id : txn_ids_sorted) {
					pstmtLock.setLong(1, txn_id);
					Timestamp export_time;
					try (ResultSetWrapper r = pstmtLock.executeQueryWrapper()) {
						if (r.next()) {
							export_time = r.getTimestamp(1);
						} else {
							continue;
						}
					}
					
					if (export_time == null) {
						pstmt.setTimestamp(1, currentTime);
						pstmt.setLong(2, txn_id);
						pstmt.executeUpdate();
					}
				}
			}
		}
		
		return filename + C.char_31 + Stringify.getTimestamp(currentTime);
	}

}
