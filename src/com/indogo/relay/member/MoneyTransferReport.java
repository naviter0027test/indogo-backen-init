package com.indogo.relay.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.MiniMart;
import com.indogo.PaymentType;
import com.indogo.TransferMoneyThroughBank;
import com.indogo.TransferStatus;
import com.lionpig.webui.database.PreparedStatementWrapper;
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

public class MoneyTransferReport implements IFunction, ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.money_transfer;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.lm_time_transfer, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "匯款日"));
		cols.add(new TablePageColumn(C.payment_name, C.columnTypeString, C.columnDirectionNone, false, true, "匯款方式"));
		cols.add(new TablePageColumn(C.mini_mart_name, C.columnTypeString, C.columnDirectionNone, false, true, "通路"));
		cols.add(new TablePageColumn(C.lm_date_paid, C.columnTypeDateTime, C.columnDirectionNone, false, true, "付款日期"));
		cols.add(new TablePageColumn(C.store_name, C.columnTypeString, C.columnDirectionNone, false, true, "店鋪名稱"));
		cols.add(new TablePageColumn(C.create_date, C.columnTypeDateTime, C.columnDirectionNone, false, true, "訂單日期"));
		cols.add(new TablePageColumn(C.lm_time_paid, C.columnTypeDateTime, C.columnDirectionDesc, true, false, "繳款日期"));
		cols.add(new TablePageColumn(C.txn_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, "廠商訂單編號"));
		cols.add(new TablePageColumn(C.payment_info, C.columnTypeString, C.columnDirectionDefault, true, false, "條碼"));
		cols.add(new TablePageColumn(C.transfer_status_name, C.columnTypeString, C.columnDirectionNone, false, true, "交易狀態"));
		cols.add(new TablePageColumn(C.total, C.columnTypeNumber, C.columnDirectionDefault, true, false, "代收金額"));
		cols.add(new TablePageColumn(C.service_charge, C.columnTypeNumber, C.columnDirectionDefault, true, false, "手續費"));
		cols.add(new TablePageColumn(C.transfer_amount_ntd, C.columnTypeNumber, C.columnDirectionDefault, true, false, "匯款金額"));
		cols.add(new TablePageColumn(C.kurs_value, C.columnTypeNumber, C.columnDirectionDefault, true, false, "匯率"));
		cols.add(new TablePageColumn(C.estimate_kurs, C.columnTypeNumber, C.columnDirectionNone, false, true, "參考匯率"));
		cols.add(new TablePageColumn(C.transfer_amount_idr, C.columnTypeNumber, C.columnDirectionDefault, true, false, "IDR 金額"));
		cols.add(new TablePageColumn(C.transfer_through_bank_name, C.columnTypeString, C.columnDirectionDefault, true, false, "匯款銀行"));
		cols.add(new TablePageColumn(C.bank_name, C.columnTypeString, C.columnDirectionNone, false, true, "入款銀行"));
		cols.add(new TablePageColumn(C.swift_code, C.columnTypeString, C.columnDirectionNone, false, true, "SWIFT"));
		cols.add(new TablePageColumn(C.service_charge_idr, C.columnTypeNumber, C.columnDirectionNone, false, true, "手續費 IDR"));
		cols.add(new TablePageColumn(C.transfer_amount_idr_plus_fee, C.columnTypeNumber, C.columnDirectionNone, false, true, "實際匯款金額"));
		cols.add(new TablePageColumn(C.member_name, C.columnTypeString, C.columnDirectionNone, false, true, "Member Name"));
		cols.add(new TablePageColumn(C.phone_no, C.columnTypeString, C.columnDirectionNone, false, true, "Phone No"));
		cols.add(new TablePageColumn(C.arc_no, C.columnTypeString, C.columnDirectionNone, false, true, "ARC"));
		cols.add(new TablePageColumn(C.recipient_name, C.columnTypeString, C.columnDirectionNone, false, true, "Recipient Name"));
		cols.add(new TablePageColumn(C.recipient_name_2, C.columnTypeString, C.columnDirectionNone, false, true, "Recipient Name 2"));
		cols.add(new TablePageColumn(C.bank_code, C.columnTypeString, C.columnDirectionNone, false, true, "Bank Code"));
		cols.add(new TablePageColumn(C.bank_acc, C.columnTypeString, C.columnDirectionNone, false, true, "Bank Account"));
		cols.add(new TablePageColumn(C.recipient_birthday, C.columnTypeDateTime, C.columnDirectionNone, false, true, "Recipient Birthday"));
		cols.add(new TablePageColumn(C.id_filename, C.columnTypeString, C.columnDirectionNone, false, true, "Recipient ID Filename"));
		cols.add(new TablePageColumn(C.mini_mart_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.mini_mart_id, true, false));
		cols.add(new TablePageColumn(C.transfer_status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.transfer_status_id, true, false));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, false));
		cols.add(new TablePageColumn(C.recipient_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.recipient_id, true, false));
		cols.add(new TablePageColumn(C.payment_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.payment_id, true, false));
		cols.add(new TablePageColumn(C.store_id, C.columnTypeString, C.columnDirectionDefault, true, false, C.store_id, true, false));
		cols.add(new TablePageColumn(C.is_app, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_app, false, false));
		cols.add(new TablePageColumn(C.exchange_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_app, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		Integer mini_mart_id = r.unwrap().getInt(C.mini_mart_id);
		String lm_time_paid = r.getTimestamp(C.lm_time_paid);
		long txn_id = r.unwrap().unwrap().getLong(C.txn_id);
		TransferStatus transferStatus = TransferStatus.get(r.unwrap().getInt(C.transfer_status_id));
		long member_id = r.unwrap().getLong(C.member_id);
		int recipient_id = r.unwrap().getInt(C.recipient_id);
		String fromBank = r.getString(C.transfer_through_bank_name);
		long transfer_amount_idr = r.unwrap().getLong(C.transfer_amount_idr);
		PaymentType paymentType = PaymentType.get(r.unwrap().getInt(C.payment_id));
		String store_id = r.getString(C.store_id);
		Long exchange_id = r.unwrap().getLong(C.exchange_id);
		
		cols.get(C.lm_time_transfer).setValue(r.getTimestamp(C.lm_time_transfer));
		
		if (mini_mart_id != null) {
			MiniMart miniMart = MiniMart.get(mini_mart_id);
			cols.get(C.mini_mart_name).setValue(miniMart.getDisplay());
			cols.get(C.mini_mart_id).setValue(String.valueOf(mini_mart_id));
			
			switch (miniMart) {
				case SevenEleven:
				case OkMart:
				case HiLife:
					if (cacheStore.containsKey(store_id)) {
						cols.get(C.store_name).setValue(cacheStore.get(store_id));
					} else {
						String storeName;
						pstmtStoreName.setInt(1, mini_mart_id);
						pstmtStoreName.setString(2, store_id);
						ResultSet rr = pstmtStoreName.executeQuery();
						try {
							if (rr.next()) {
								storeName = rr.getString(1);
							} else {
								storeName = store_id;
							}
						} finally {
							rr.close();
						}
						cacheStore.put(store_id, storeName);
						cols.get(C.store_name).setValue(storeName);
					}
					break;
				default:
					cols.get(C.store_name).setValue(store_id);
					break;
			}
		} else {
			cols.get(C.mini_mart_name).setValue(C.emptyString);
			cols.get(C.mini_mart_id).setValue(C.emptyString);
			cols.get(C.store_name).setValue(store_id);
		}
		
		if (lm_time_paid.length() > 0)
			cols.get(C.lm_date_paid).setValue(lm_time_paid.substring(0, 10));
		else
			cols.get(C.lm_date_paid).setValue(C.emptyString);
		
		cols.get(C.store_id).setValue(store_id);
		
		StringBuilder sb = new StringBuilder();
		sb.append(txn_id);
		sb.insert(4, "/");
		sb.insert(7, "/");
		cols.get(C.create_date).setValue(sb.substring(0, 10));
		
		cols.get(C.lm_time_paid).setValue(r.getTimestamp(C.lm_time_paid));
		cols.get(C.txn_id).setValue(String.valueOf(txn_id));
		cols.get(C.payment_info).setValue(r.getString(C.payment_info));
		cols.get(C.transfer_status_name).setValue(transferStatus.name());
		cols.get(C.total).setValue(r.getIntCurrency(C.total));
		cols.get(C.service_charge).setValue(r.getIntCurrency(C.service_charge));
		cols.get(C.transfer_amount_ntd).setValue(r.getIntCurrency(C.transfer_amount_ntd));
		cols.get(C.transfer_amount_idr).setValue(Stringify.getCurrency(transfer_amount_idr));
		cols.get(C.transfer_through_bank_name).setValue(fromBank);
		
		pstmtRecipient.setLong(1, member_id);
		pstmtRecipient.setInt(2, recipient_id);
		try (ResultSetWrapperStringify rRecipient = new ResultSetWrapperStringify(pstmtRecipient.executeQuery())) {
			if (rRecipient.next()) {
				String bankCode = rRecipient.getString(C.bank_code);
				
				if (fromBank.equals(TransferMoneyThroughBank.BRI.name())) {
					if (bankCode.equals("002")) {
						cols.get(C.service_charge_idr).setValue(Stringify.getCurrency(briFee));
						cols.get(C.transfer_amount_idr_plus_fee).setValue(Stringify.getCurrency(transfer_amount_idr + briFee));
					} else {
						cols.get(C.service_charge_idr).setValue(Stringify.getCurrency(briFeeToNonBri));
						cols.get(C.transfer_amount_idr_plus_fee).setValue(Stringify.getCurrency(transfer_amount_idr + briFeeToNonBri));
					}
				} else if (fromBank.equals(TransferMoneyThroughBank.BNI.name())) {
					if (bankCode.equals("009")) {
						cols.get(C.service_charge_idr).setValue(Stringify.getCurrency(bniFee));
						cols.get(C.transfer_amount_idr_plus_fee).setValue(Stringify.getCurrency(transfer_amount_idr + bniFee));
					} else {
						cols.get(C.service_charge_idr).setValue(Stringify.getCurrency(bniFeeToNonBni));
						cols.get(C.transfer_amount_idr_plus_fee).setValue(Stringify.getCurrency(transfer_amount_idr + bniFeeToNonBni));
					}
				} else {
					cols.get(C.service_charge_idr).setValue(C.emptyString);
					cols.get(C.transfer_amount_idr_plus_fee).setValue(C.emptyString);
				}
				
				cols.get(C.bank_name).setValue(rRecipient.getString(C.bank_name));
				cols.get(C.bank_code).setValue(bankCode);
				cols.get(C.bank_acc).setValue(rRecipient.getString(C.bank_acc));
				cols.get(C.recipient_name).setValue(rRecipient.getString(C.recipient_name));
				cols.get(C.recipient_name_2).setValue(rRecipient.getString(C.recipient_name_2));
				cols.get(C.swift_code).setValue(rRecipient.getString(C.swift_code));
				cols.get(C.recipient_birthday).setValue(rRecipient.getDate(C.birthday));
				cols.get(C.id_filename).setValue(rRecipient.getString(C.id_filename));
			} else {
				cols.get(C.bank_name).setValue(C.emptyString);
				cols.get(C.service_charge_idr).setValue(C.emptyString);
				cols.get(C.transfer_amount_idr_plus_fee).setValue(C.emptyString);
				cols.get(C.bank_code).setValue(C.emptyString);
				cols.get(C.bank_acc).setValue(C.emptyString);
				cols.get(C.recipient_name).setValue(C.emptyString);
				cols.get(C.recipient_name_2).setValue(C.emptyString);
				cols.get(C.swift_code).setValue(C.emptyString);
				cols.get(C.recipient_birthday).setValue(C.emptyString);
				cols.get(C.id_filename).setValue(C.emptyString);
			}
		}
		
		cols.get(C.transfer_status_id).setValue(String.valueOf(transferStatus.getId()));
		cols.get(C.member_id).setValue(String.valueOf(member_id));
		cols.get(C.recipient_id).setValue(String.valueOf(recipient_id));
		cols.get(C.payment_id).setValue(String.valueOf(paymentType.getId()));
		cols.get(C.payment_name).setValue(paymentType.getDisplay());
		cols.get(C.is_app).setValue(r.getInt(C.is_app));
		
		pstmtMember.setLong(1, member_id);
		try (ResultSetWrapperStringify rMember = new ResultSetWrapperStringify(pstmtMember.executeQuery())) {
			if (rMember.next()) {
				cols.get(C.member_name).setValue(rMember.getString(C.member_name));
				cols.get(C.phone_no).setValue(rMember.getString(C.phone_no));
				cols.get(C.arc_no).setValue(rMember.getString(C.arc_no));
			} else {
				cols.get(C.member_name).setValue(C.emptyString);
				cols.get(C.phone_no).setValue(C.emptyString);
				cols.get(C.arc_no).setValue(C.emptyString);
			}
		}
		
		if (exchange_id != null) {
			cols.get(C.kurs_value).setValue(r.getDoubleCurrency(C.kurs_value));
			
			pstmtExchange.setLong(1, exchange_id);
			pstmtExchange.setLong(2, txn_id);
			try (ResultSetWrapperStringify rr = new ResultSetWrapperStringify(pstmtExchange.executeQuery())) {
				if (rr.next()) {
					cols.get(C.estimate_kurs).setValue(rr.getDoubleCurrency(1));
				} else {
					cols.get(C.estimate_kurs).setValue(C.emptyString);
				}
			}
		} else {
			cols.get(C.kurs_value).setValue(C.emptyString);
			cols.get(C.estimate_kurs).setValue(r.getDoubleCurrency(C.kurs_value));
		}
	}
	
	private PreparedStatement pstmtMember = null;
	private PreparedStatement pstmtRecipient = null;
	private PreparedStatement pstmtStoreName = null;
	private PreparedStatementWrapper pstmtExchange = null;
	private int briFee, briFeeToNonBri;
	private int bniFee, bniFeeToNonBni;
	private HashMap<String, String> cacheStore = new HashMap<>();
	
	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		pstmtMember = conn.prepareStatement("select member_name, phone_no, arc_no, arc_expire_date from member where member_id = ?");
		/**
		 * SELECT a.bank_code, b.bank_name, bank_acc, a.recipient_name, b.swift_code
FROM member_recipient a
inner join bank_code_list b on b.bank_code = a.bank_code
WHERE a.member_id = ? AND a.recipient_id = ?
		 */
		pstmtRecipient = conn.prepareStatement("SELECT a.bank_code, b.bank_name, bank_acc, a.recipient_name, b.swift_code, a.birthday, a.id_filename, a.recipient_name_2\r\n" + 
				"FROM member_recipient a\r\n" + 
				"inner join bank_code_list b on b.bank_code = a.bank_code\r\n" + 
				"WHERE a.member_id = ? AND a.recipient_id = ?");
		pstmtStoreName = conn.prepareStatement("select concat(store_area, store_name) as store_name from mini_mart_store where mini_mart_id = ? and store_id = ?");
		pstmtExchange = new PreparedStatementWrapper(conn.prepareStatement("select old_kurs_value from exchange_item where exchange_id = ? and txn_id = ?"));
		briFee = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, C.bri_fee_idr));
		briFeeToNonBri = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, C.bri_fee_to_non_bri));
		bniFee = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, C.bni_fee_idr));
		bniFeeToNonBni = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, C.bni_fee_to_non_bni));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		pstmtRecipient.close();
		pstmtStoreName.close();
		pstmtMember.close();
		pstmtExchange.close();
	}

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		
		if (action.equals(C.init)) {
			return MoneyTransfer.getTransferStatusList(fi);
		} else if (action.equals(C.sql_temp_table)) {
			TransferStatus status = TransferStatus.get(Helper.getInt(params, C.transfer_status_id, true));
			Timestamp start_time = Helper.getTimestamp(params, C.start_time, true);
			Timestamp end_time = Helper.getTimestamp(params, C.end_time, true);
			String[] bank_codes = Helper.getStringArray(params, C.bank_codes, true);
			String query_type = Helper.getString(params, C.query_type, true);
			
			Connection conn = fi.getConnection().getConnection();
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
			pstmt.setString(2, C.money_transfer_report);
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
					pstmt.setString(2, C.money_transfer_report);
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
			pstmt.setString(2, C.money_transfer_report);
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
					pstmt.setString(2, C.money_transfer_report);
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

}
