package com.indogo.relay.member;

import java.io.File;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.indogo.PaymentType;
import com.indogo.TransferMoneyThroughBank;
import com.indogo.TransferStatus;
import com.indogo.bni.BniHostToHost;
import com.indogo.bni.BniPaymentResult;
import com.indogo.bni.BniTransferType;
import com.indogo.bni.PaymentInfo;
import com.indogo.bni.VostroInfo;
import com.indogo.model.member.BankCodeModel;
import com.indogo.model.member.BankInfo;
import com.indogo.relay.member.BankCodeConfiguration.OrderBy;
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

public class MoneyTransferBNI implements ITablePage, IFunction {
	
	private PreparedStatement pstmtMember = null;
	private PreparedStatement pstmtRecipient = null;
	private PreparedStatement pstmtBank = null;
	private HashMap<String, BankInfo> bankCache = null;
	private int threshold1 = 25000000, threshold2 = 500000000;

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
		List<TablePageColumn> cols = new ArrayList<TablePageColumn>();
		cols.add(new TablePageColumn(C.bni_transfer_type, C.columnTypeString, C.columnDirectionNone, false, true, "BNI Transfer Type", false));
		cols.add(new TablePageColumn(C.bni_reply_message, C.columnTypeString, C.columnDirectionNone, false, true, "BNI Reply", false));
		cols.add(new TablePageColumn(C.transfer_status_name, C.columnTypeString, C.columnDirectionNone, false, true, "Status"));
		cols.add(new TablePageColumn(C.payment_info, C.columnTypeString, C.columnDirectionDefault, true, false, "Payment Info"));
		cols.add(new TablePageColumn(C.bni_retry, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Seq"));
		cols.add(new TablePageColumn(C.txn_id, C.columnTypeNumber, C.columnDirectionDesc, true, false, "Transaction Id"));
		cols.add(new TablePageColumn(C.member_name, C.columnTypeString, C.columnDirectionNone, false, true, "Member Name"));
		cols.add(new TablePageColumn(C.phone_no, C.columnTypeString, C.columnDirectionNone, false, true, "Phone No"));
		cols.add(new TablePageColumn(C.app_phone_no, C.columnTypeString, C.columnDirectionNone, false, true, "Phone No (APP)"));
		cols.add(new TablePageColumn(C.arc_no, C.columnTypeString, C.columnDirectionNone, false, true, "ARC"));
		cols.add(new TablePageColumn(C.arc_expire_date, C.columnTypeDateTime, C.columnDirectionNone, false, true, "ARC Expire", false, true));
		cols.add(new TablePageColumn(C.recipient_name, C.columnTypeString, C.columnDirectionNone, false, true, "Recipient Name"));
		cols.add(new TablePageColumn(C.bank_code, C.columnTypeString, C.columnDirectionNone, false, true, "Bank Code"));
		cols.add(new TablePageColumn(C.bank_name, C.columnTypeString, C.columnDirectionNone, false, true, "Bank Name"));
		cols.add(new TablePageColumn(C.swift_code, C.columnTypeString, C.columnDirectionNone, false, true, "SWIFT"));
		cols.add(new TablePageColumn(C.bank_acc, C.columnTypeString, C.columnDirectionNone, false, true, "Bank Account"));
		cols.add(new TablePageColumn(C.payment_name, C.columnTypeString, C.columnDirectionNone, false, true, "Payment Type"));
		cols.add(new TablePageColumn(C.kurs_value, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Kurs"));
		cols.add(new TablePageColumn(C.transfer_amount_ntd, C.columnTypeNumber, C.columnDirectionDefault, true, false, "NTD"));
		cols.add(new TablePageColumn(C.transfer_amount_idr, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Rupiah"));
		cols.add(new TablePageColumn(C.service_charge, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Ongkir"));
		cols.add(new TablePageColumn(C.total, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total (NTD)"));
		cols.add(new TablePageColumn(C.is_print, C.columnTypeString, C.columnDirectionDefault, true, false, "Printed", false, true));
		cols.add(new TablePageColumn(C.lm_time_paid, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Paid Date"));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, false));
		cols.add(new TablePageColumn(C.recipient_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.recipient_id, true, true));
		cols.add(new TablePageColumn(C.payment_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.payment_id, true, false));
		cols.add(new TablePageColumn(C.transfer_status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.transfer_status_id, true, true));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, C.lm_user));
		cols.add(new TablePageColumn(C.is_verified, C.columnTypeNumber, C.columnDirectionNone, false, true, C.is_verified, true, false));
		cols.add(new TablePageColumn(C.is_app, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_app, true, false));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		long memberId = r.unwrap().unwrap().getLong(C.member_id);
		int recipientId = r.unwrap().getInt(C.recipient_id);
		TransferStatus status = TransferStatus.get(r.unwrap().getInt(C.transfer_status_id));
		PaymentType paymentType = PaymentType.get(r.unwrap().getInt(C.payment_id));
		
		cols.get(C.txn_id).setValue(r.getLong(C.txn_id));
		cols.get(C.kurs_value).setValue(r.getInt(C.kurs_value));
		cols.get(C.transfer_amount_ntd).setValue(r.getIntCurrency(C.transfer_amount_ntd));
		cols.get(C.transfer_amount_idr).setValue(r.getLongCurrency(C.transfer_amount_idr));
		cols.get(C.service_charge).setValue(r.getIntCurrency(C.service_charge));
		cols.get(C.total).setValue(r.getIntCurrency(C.total));
		cols.get(C.is_print).setValue(r.getInt(C.is_print));
		cols.get(C.member_id).setValue(String.valueOf(memberId));
		cols.get(C.recipient_id).setValue(String.valueOf(recipientId));
		cols.get(C.payment_id).setValue(String.valueOf(paymentType.getId()));
		cols.get(C.transfer_status_id).setValue(String.valueOf(status.getId()));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.payment_name).setValue(paymentType.getDisplay());
		cols.get(C.payment_info).setValue(r.getString(C.payment_info));
		cols.get(C.transfer_status_name).setValue(status.name());
		cols.get(C.is_app).setValue(r.getInt(C.is_app));
		cols.get(C.bni_retry).setValue(String.valueOf(r.unwrap().getInt(C.bni_retry)));
		cols.get(C.lm_time_paid).setValue(r.getTimestamp(C.lm_time_paid));
		
		// member
		pstmtMember.setLong(1, memberId);
		try (ResultSetWrapperStringify rMember = new ResultSetWrapperStringify(pstmtMember.executeQuery())) {
			if (rMember.next()) {
				cols.get(C.member_name).setValue(rMember.getString(C.member_name));
				cols.get(C.phone_no).setValue(StringUtils.join(StringUtils.split(rMember.getString(C.phone_no), '.'), '\n'));
				cols.get(C.arc_no).setValue(rMember.getString(C.arc_no));
				cols.get(C.arc_expire_date).setValue(rMember.getDate(C.arc_expire_date));
				cols.get(C.app_phone_no).setValue(rMember.getString(C.app_phone_no));
			} else {
				cols.get(C.member_name).setValue(C.emptyString);
				cols.get(C.phone_no).setValue(C.emptyString);
				cols.get(C.arc_no).setValue(C.emptyString);
				cols.get(C.arc_expire_date).setValue(C.emptyString);
				cols.get(C.app_phone_no).setValue(C.emptyString);
			}
		}
		
		// recipient
		pstmtRecipient.setLong(1, memberId);
		pstmtRecipient.setInt(2, recipientId);
		try (ResultSetWrapperStringify rRecipient = new ResultSetWrapperStringify(pstmtRecipient.executeQuery())) {
			if (rRecipient.next()) {
				cols.get(C.bank_code).setValue(rRecipient.getString(C.bank_code));
				cols.get(C.bank_name).setValue(rRecipient.getString(C.bank_name));
				cols.get(C.bank_acc).setValue(rRecipient.getString(C.bank_acc));
				cols.get(C.recipient_name).setValue(rRecipient.getString(C.recipient_name));
				cols.get(C.is_verified).setValue(rRecipient.getInt(C.is_verified));
				cols.get(C.swift_code).setValue(rRecipient.getString(C.swift_code));
			} else {
				cols.get(C.bank_code).setValue(C.emptyString);
				cols.get(C.bank_name).setValue(C.emptyString);
				cols.get(C.bank_acc).setValue(C.emptyString);
				cols.get(C.recipient_name).setValue(C.emptyString);
				cols.get(C.is_verified).setValue(C.emptyString);
				cols.get(C.swift_code).setValue(C.emptyString);
			}
		}
		
		String bank_code = cols.get(C.bank_code).getValue();
		if (Helper.isNullOrEmpty(bank_code)) {
			rowAttr.HtmlClass.add("bni_error");
			cols.get(C.bni_transfer_type).setValue(C.emptyString);
			cols.get(C.bni_reply_message).setValue("recipient not exist");
		} else {
			BankInfo info;
			if (bankCache.containsKey(bank_code)) {
				info = bankCache.get(bank_code);
			} else {
				pstmtBank.setString(1, bank_code);
				ResultSet rr = pstmtBank.executeQuery();
				try {
					info = new BankInfo();
					if (rr.next()) {
						info.transfer_type = BniTransferType.get(rr.getInt(1));
						info.transfer_threshold_1 = BniTransferType.get(rr.getInt(2));
						info.transfer_threshold_2 = BniTransferType.get(rr.getInt(3));
						info.is_exist = true;
					} else {
						info.transfer_type = null;
						info.transfer_threshold_1 = null;
						info.transfer_threshold_2 = null;
						info.is_exist = false;
					}
					bankCache.put(bank_code, info);
				} finally {
					rr.close();
				}
			}
			
			if (info.is_exist) {
				long transfer_amount_idr = r.unwrap().getLong(C.transfer_amount_idr);
				
				BniTransferType transferType;
				if (transfer_amount_idr >= threshold2)
					transferType = info.transfer_threshold_2;
				else if (transfer_amount_idr >= threshold1)
					transferType = info.transfer_threshold_1;
				else
					transferType = info.transfer_type;
				
				cols.get(C.bni_transfer_type).setValue(transferType == null ? C.emptyString : String.valueOf(transferType.getId()));
				cols.get(C.bni_reply_message).setValue(C.emptyString);
			} else {
				rowAttr.HtmlClass.add("bni_error");
				cols.get(C.bni_transfer_type).setValue(C.emptyString);
				cols.get(C.bni_reply_message).setValue("bank code [" + bank_code + "] not defined");
			}
		}
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		filter.add(new TablePageFilter(C.transfer_status_id, C.columnTypeNumber, C.operationEqual, "2", null));
		
		Connection conn = fi.getConnection().getConnection();
		pstmtMember = conn.prepareStatement("select member_name, phone_no, arc_no, arc_expire_date, app_phone_no from member where member_id = ?");

		/**
		 * SELECT a.recipient_name, a.bank_code, a.bank_acc, b.bank_name, a.is_verified, b.swift_code
FROM member_recipient a
inner join bank_code_list b on b.bank_code = a.bank_code
WHERE a.member_id = ? AND a.recipient_id = ?
		 */
		pstmtRecipient = conn.prepareStatement("SELECT a.recipient_name, a.bank_code, a.bank_acc, b.bank_name, a.is_verified, b.swift_code\r\n" + 
				"FROM member_recipient a\r\n" + 
				"inner join bank_code_list b on b.bank_code = a.bank_code\r\n" + 
				"WHERE a.member_id = ? AND a.recipient_id = ?");
		
		pstmtBank = fi.getConnection().getConnection().prepareStatement("select transfer_type, transfer_threshold_1, transfer_threshold_2 from bank_code_list where bank_code = ?");
		bankCache = new HashMap<String, BankInfo>();
		String threshold1 = fi.getConnection().getGlobalConfig(C.bni_h2h, C.transfer_threshold_1);
		String threshold2 = fi.getConnection().getGlobalConfig(C.bni_h2h, C.transfer_threshold_2);
		if (!Helper.isNullOrEmpty(threshold1)) {
			this.threshold1 = Integer.parseInt(threshold1);
		}
		if (!Helper.isNullOrEmpty(threshold2)) {
			this.threshold2 = Integer.parseInt(threshold2);
		}
		
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		pstmtMember.close();
		pstmtRecipient.close();
	}

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		
		if (action.equals(C.init)) {
			StringBuilder sb = new StringBuilder(100);
			
			List<BankCodeModel> bankCodeList = new BankCodeConfiguration().getBankCodeList(fi, OrderBy.bank_code);
			sb.append(bankCodeList.size());
			for (BankCodeModel bankCodeModel : bankCodeList) {
				sb.append(C.char_31).append(bankCodeModel.bank_code)
				.append(C.char_31).append(bankCodeModel.bank_name);
			}
			
			bankCodeList = new BankCodeConfiguration().getBankCodeList(fi, OrderBy.bank_name);
			sb.append(C.char_31).append(bankCodeList.size());
			for (BankCodeModel bankCodeModel : bankCodeList) {
				sb.append(C.char_31).append(bankCodeModel.bank_code)
				.append(C.char_31).append(bankCodeModel.bank_name);
			}
			
			sb.append(C.char_31).append(fi.getConnection().getGlobalConfig(C.bni_h2h, C.default_key_store_filename));
			
			sb.append(C.char_31).append(BniTransferType.values().length);
			for (BniTransferType t : BniTransferType.values()) {
				sb.append(C.char_31).append(t.getId()).append(C.char_31).append(t.name());
			}
			
			PreparedStatement pstmt = fi.getConnection().getConnection().prepareStatement("select create_date, expire_date, file_name from bni_key_store order by create_date desc");
			try {
				ResultSet r = pstmt.executeQuery();
				try {
					List<Object[]> list = new ArrayList<Object[]>();
					while (r.next()) {
						Object[] objs = new Object[3];
						objs[0] = r.getTimestamp(1);
						objs[1] = r.getTimestamp(2);
						objs[2] = r.getString(3);
						list.add(objs);
					}
					
					sb.append(C.char_31).append(list.size());
					for (Object[] objs : list) {
						sb.append(C.char_31).append(Stringify.getDate((Timestamp)objs[0]))
						.append(C.char_31).append(Stringify.getDate((Timestamp)objs[1]))
						.append(C.char_31).append(objs[2]);
					}
				} finally {
					r.close();
				}
			} finally {
				pstmt.close();
			}
			
			sb.append(C.char_31).append(fi.getConnection().getGlobalConfig(C.kotsms, C.msg_footnote));
			
			return sb.toString();
		} else if (action.equals(C.bni_transfer)) {
			int bni_transfer_type = Helper.getInt(params, C.bni_transfer_type, true);
			long txn_id = Helper.getLong(params, C.txn_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			char[] password = Helper.getString(params, C.password, true).toCharArray();
			
			Connection conn = fi.getConnection().getConnection();
			try {
				Timestamp currentTime = fi.getConnection().getCurrentTime();
				String message;
				BniPaymentResult result = doBniTransfer(fi, BniTransferType.get(bni_transfer_type), txn_id, lm_time, password, currentTime);
				if (result.message.equals("OK") || result.message.equals("O.K.")
						|| result.message.equals("PAID") || result.message.equals("PAYABLE")) {
					conn.commit();
					message = C.emptyString;
				} else {
					conn.rollback();
					message = result.message;
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append(txn_id)
				.append(C.char_31).append(Stringify.getTimestamp(currentTime))
				.append(C.char_31).append(message)
				.append(C.char_31).append(fi.getSessionInfo().getUserName())
				.append(C.char_31).append(TransferStatus.transfered.getId())
				.append(C.char_31).append(TransferStatus.transfered.name());
				return sb.toString();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.create_key_store)) {
			String password = Helper.getString(params, C.private_key_password, true);
			int validity = Helper.getInt(params, C.validity, true);
			Object[] objs = BniHostToHost.createCertificate(fi, password, validity);
			StringBuilder sb = new StringBuilder();
			sb.append(Stringify.getDate((Timestamp)objs[0])).append(C.char_31).append(Stringify.getDate((Timestamp)objs[1])).append(C.char_31).append(objs[2]);
			return sb.toString();
		} else if (action.equals(C.export_certificate)) {
			String password = Helper.getString(params, C.private_key_password, true);
			String filename = Helper.getString(params, C.filename, true);
			String outputFilename = "indogo.cer";
			BniHostToHost.exportCertificate(fi, password, filename, new File(fi.getTempFolder(), outputFilename).getAbsolutePath());
			return outputFilename;
		} else if (action.equals(C.certificate_set_as_default)) {
			String filename = Helper.getString(params, C.filename, true);
			PreparedStatement pstmt = fi.getConnection().getConnection().prepareStatement("select count(*) from bni_key_store where file_name = ?");
			try {
				int count;
				pstmt.setString(1, filename);
				ResultSet r = pstmt.executeQuery();
				try {
					r.next();
					count = r.getInt(1);
				} finally {
					r.close();
				}
				
				if (count > 0) {
					fi.getConnection().setGlobalConfig(C.bni_h2h, C.default_key_store_filename, filename);
					return "1";
				} else
					throw new Exception("certificate not exist");
			} finally {
				pstmt.close();
			}
		} else if (action.equals(C.vostro_inquiry)) {
			char[] private_key_password = Helper.getString(params, C.private_key_password, true).toCharArray();
			try (BniHostToHost h2h = new BniHostToHost(fi.getConnection(), private_key_password)) {
				VostroInfo vostroInfo = h2h.vostroInquery();
				String balance;
				try {
					balance = String.format("%,d", (long)Double.parseDouble(vostroInfo.balance));
				} catch (Exception ignore) {
					balance = vostroInfo.balance;
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append(vostroInfo.accountName)
				.append(C.char_31).append(vostroInfo.accountNumber)
				.append(C.char_31).append(balance)
				.append(C.char_31).append(vostroInfo.openingDate)
				.append(C.char_31).append(vostroInfo.accountStatus);
				return sb.toString();
			}
		} else if (action.equals(C.payment_order_inquiry)) {
			String payment_info = Helper.getString(params, C.payment_info, true);
			String bni_trx_date;
			int bni_retry;
			PreparedStatement pstmt = fi.getConnection().getConnection().prepareStatement("select bni_trx_date, bni_retry from money_transfer where payment_info = ?");
			try {
				pstmt.setString(1, payment_info);
				ResultSet r = pstmt.executeQuery();
				try {
					if (r.next()) {
						bni_trx_date = r.getString(1);
						bni_retry = r.getInt(2);
					} else
						throw new Exception(String.format(C.data_not_exist, "payment_info = " + payment_info));
				} finally {
					r.close();
				}
			} finally {
				pstmt.close();
			}
			
			try (BniHostToHost bni = new BniHostToHost(fi.getConnection())) {
				if (Helper.isNullOrEmpty(bni_trx_date))
					bni_trx_date = bni.getTrxDate();
				
				PaymentInfo paymentInfo;
				if (bni_retry > 0)
					paymentInfo = bni.paymentOrderInquiry(payment_info + bni_retry, bni_trx_date);
				else
					paymentInfo = bni.paymentOrderInquiry(payment_info, bni_trx_date);
				StringBuilder sb = new StringBuilder();
				sb.append(paymentInfo.status)
				.append(C.char_31).append(paymentInfo.statusDescription)
				.append(C.char_31).append(paymentInfo.paymentDetail.bniReference)
				.append(C.char_31).append(paymentInfo.paymentDetail.paidDate)
				.append(C.char_31).append(paymentInfo.paymentDetail.paidCurrency)
				.append(C.char_31).append(paymentInfo.paymentDetail.paidAmount)
				.append(C.char_31).append(paymentInfo.paymentDetail.chargesAmount)
				.append(C.char_31).append(paymentInfo.paymentDetail.beneficiaryAccount)
				.append(C.char_31).append(paymentInfo.paymentDetail.beneficiaryName);
				return sb.toString();
			}
		} else if (action.equals(C.increase_bni_retry)) {
			long txn_id = Helper.getLong(params, C.txn_id, true);
			Connection conn = fi.getConnection().getConnection();
			PreparedStatement pstmt = conn.prepareStatement("select bni_retry from money_transfer where txn_id = ? for update");
			PreparedStatement pstmtUpdate = conn.prepareStatement("update money_transfer set bni_retry = ? where txn_id = ?");
			try {
				int bni_retry;
				pstmt.setLong(1, txn_id);
				try (ResultSet r = pstmt.executeQuery()) {
					if (r.next()) {
						bni_retry = r.getInt(1);
					} else
						throw new Exception(String.format(C.data_not_exist, "txn_id = " + txn_id));
				}
				
				bni_retry++;
				
				pstmtUpdate.setInt(1, bni_retry);
				pstmtUpdate.setLong(2, txn_id);
				pstmtUpdate.executeUpdate();
				
				conn.commit();
				
				return String.valueOf(bni_retry);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				pstmt.close();
				pstmtUpdate.close();
			}
		} else if (action.equals(C.sql_temp_table)) {
			Connection conn = fi.getConnection().getConnection();
			String bank_code = Helper.getString(params, C.bank_code, false);
			String exclude_bank_code = Helper.getString(params, C.exclude_bank_code, false);
			try {
				int count;
				if (!Helper.isNullOrEmpty(bank_code)) {
					count = sqlTempTableWithBankCode(fi, bank_code);
				} else {
					count = sqlTempTableExcludeBankCode(fi, exclude_bank_code);
				}
				conn.commit();
				return String.valueOf(count);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}
	
	private BniPaymentResult doBniTransfer(FunctionItem fi, BniTransferType bni_transfer_type, long txn_id, Timestamp lm_time, char[] password, Timestamp currentTime) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select member_id, recipient_id, transfer_amount_idr, payment_info, bni_retry from money_transfer where txn_id = ?");
		PreparedStatement pstmtMember = conn.prepareStatement("select member_name from member where member_id = ?");
		PreparedStatement pstmtMemberRecipient = conn.prepareStatement("select recipient_name, bank_code, bank_acc from member_recipient where member_id = ? and recipient_id = ?");
		PreparedStatement pstmtBank = conn.prepareStatement("select clr_code, rtgs_code from bank_code_list where bank_code = ?");
		PreparedStatement pstmtUpdate = conn.prepareStatement("update money_transfer set bni_trx_date = ? where txn_id = ?");
		try {
			new MoneyTransfer().changeStatus(fi, txn_id, lm_time, TransferStatus.transfered, "BNI auto transfer", currentTime, TransferMoneyThroughBank.BNI);
			
			long member_id;
			int recipient_id;
			int transfer_amount_idr;
			int bni_retry;
			String payment_info;
			pstmt.setLong(1, txn_id);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					member_id = r.getLong(1);
					recipient_id = r.getInt(2);
					transfer_amount_idr = r.getInt(3);
					payment_info = r.getString(4);
					bni_retry = r.getInt(5);
				} else {
					throw new Exception(String.format(C.data_not_exist, "txn_id = " + txn_id));
				}
			} finally {
				r.close();
			}
			
			String member_name;
			pstmtMember.setLong(1, member_id);
			r = pstmtMember.executeQuery();
			try {
				if (r.next()) {
					member_name = r.getString(1);
				} else {
					throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id));
				}
			} finally {
				r.close();
			}
			
			String recipient_name, bank_code, bank_acc;
			pstmtMemberRecipient.setLong(1, member_id);
			pstmtMemberRecipient.setInt(2, recipient_id);
			r = pstmtMemberRecipient.executeQuery();
			try {
				if (r.next()) {
					recipient_name = r.getString(1);
					bank_code = r.getString(2);
					bank_acc = r.getString(3);
				} else {
					throw new Exception(String.format(C.data_not_exist, "recipient_id = " + recipient_id));
				}
			} finally {
				r.close();
			}
			
			String clr_code, rtgs_code;
			pstmtBank.setString(1, bank_code);
			r = pstmtBank.executeQuery();
			try {
				if (r.next()) {
					clr_code = r.getString(1);
					rtgs_code = r.getString(2);
				} else {
					throw new Exception(String.format(C.data_not_exist, "bank_code = " + bank_code));
				}
			} finally {
				r.close();
			}
			
			BniHostToHost bni = new BniHostToHost(fi.getConnection(), password);
			try {
				String trxDate = bni.getTrxDate();
				
				pstmtUpdate.setString(1, trxDate);
				pstmtUpdate.setLong(2, txn_id);
				pstmtUpdate.executeUpdate();
				
				String refNumber = Helper.isNullOrEmpty(payment_info) ? String.valueOf(txn_id) : payment_info;
				if (bni_retry > 0) {
					refNumber = refNumber + bni_retry;
				}
				int amount = transfer_amount_idr;
				String orderingName = member_name;
				String orderingAddress = C.TAIWAN;
				String beneficiaryAccount = bank_acc;
				String beneficiaryName = recipient_name;
				String beneficiaryAddress = C.INDONESIA;
				String beneficiaryPhoneNo = C.emptyString;
				BniPaymentResult result;
				try {
					switch (bni_transfer_type) {
						case CreditToAccount:
							result = bni.paymentCreditToAccount(trxDate, refNumber, amount, orderingName, orderingAddress, beneficiaryAccount, beneficiaryName, beneficiaryAddress, beneficiaryPhoneNo);
							break;
						case CashPickup:
							result = bni.paymentCashPickup(trxDate, refNumber, amount, orderingName, orderingAddress, beneficiaryName, beneficiaryAddress, beneficiaryPhoneNo, beneficiaryAccount);
							break;
						case Interbank:
							result = bni.paymentInterbank(trxDate, refNumber, amount, orderingName, orderingAddress, beneficiaryAccount, beneficiaryName, beneficiaryAddress, beneficiaryPhoneNo, bank_code);
							break;
						case Clearing:
							result = bni.paymentClearing(trxDate, refNumber, amount, orderingName, orderingAddress, beneficiaryAccount, beneficiaryName, beneficiaryAddress, beneficiaryPhoneNo, clr_code);
							break;
						case RTGS:
							result = bni.paymentRTGS(trxDate, refNumber, amount, orderingName, orderingAddress, beneficiaryAccount, beneficiaryName, beneficiaryAddress, beneficiaryPhoneNo, rtgs_code);
							break;
						default:
							throw new Exception(bni_transfer_type + " not implemented");
					}
				} catch (ClientProtocolException e) {
					result = new BniPaymentResult();
					result.message = e.getMessage();
				}
				return result;
			} finally {
				bni.close();
			}
		} finally {
			pstmt.close();
			pstmtMember.close();
			pstmtMemberRecipient.close();
			pstmtUpdate.close();
			pstmtBank.close();
		}
	}

	private int sqlTempTableWithBankCode(FunctionItem fi, String bank_code) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatement pstmt = conn.prepareStatement("delete from sql_temp_table where session_id = ? and page_id = ?")) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.money_transfer_bni);
			pstmt.executeUpdate();
		}
		
		List<Long> txn_ids = new ArrayList<>();
		try (PreparedStatement pstmt = conn.prepareStatement("select distinct a.txn_id from money_transfer a, member_recipient b where a.member_id = b.member_id and a.recipient_id = b.recipient_id and a.transfer_status_id = 2 and b.bank_code = ?")) {
			pstmt.setString(1, bank_code);
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
					pstmt.setString(2, C.money_transfer_bni);
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
	
	private int sqlTempTableExcludeBankCode(FunctionItem fi, String exclude_bank_code) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatement pstmt = conn.prepareStatement("delete from sql_temp_table where session_id = ? and page_id = ?")) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.money_transfer_bni);
			pstmt.executeUpdate();
		}
		
		List<Long> txn_ids = new ArrayList<>();
		try (PreparedStatement pstmt = conn.prepareStatement("select distinct a.txn_id from money_transfer a, member_recipient b where a.member_id = b.member_id and a.recipient_id = b.recipient_id and a.transfer_status_id = 2 and b.bank_code <> ?")) {
			pstmt.setString(1, exclude_bank_code);
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
					pstmt.setString(2, C.money_transfer_bni);
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
