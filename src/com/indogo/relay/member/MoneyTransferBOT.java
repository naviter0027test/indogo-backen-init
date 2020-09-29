package com.indogo.relay.member;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;

import com.indogo.PaymentType;
import com.indogo.TransferMoneyThroughBank;
import com.indogo.TransferStatus;
import com.indogo.model.member.BankCodeModel;
import com.indogo.model.member.MoneyTransferModel;
import com.indogo.relay.member.BankCodeConfiguration.OrderBy;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;
import com.lionpig.webui.http.util.Stringify;

public class MoneyTransferBOT extends AbstractFunction implements ITablePage {
	
	private PreparedStatement pstmtMember = null;
	private PreparedStatement pstmtRecipient = null;

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
		cols.add(new TablePageColumn(C.transfer_status_name, C.columnTypeString, C.columnDirectionNone, false, true, "Status"));
		cols.add(new TablePageColumn(C.payment_info, C.columnTypeString, C.columnDirectionDefault, true, false, "Payment Info"));
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
		cols.add(new TablePageColumn(C.payment_name, C.columnTypeString, C.columnDirectionNone, false, true, "Payment Type", false, true));
		cols.add(new TablePageColumn(C.kurs_value, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Kurs"));
		cols.add(new TablePageColumn(C.transfer_amount_ntd, C.columnTypeNumber, C.columnDirectionDefault, true, false, "NTD"));
		cols.add(new TablePageColumn(C.transfer_amount_idr, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Rupiah"));
		cols.add(new TablePageColumn(C.service_charge, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Ongkir"));
		cols.add(new TablePageColumn(C.total, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total (NTD)"));
		cols.add(new TablePageColumn(C.is_print, C.columnTypeString, C.columnDirectionDefault, true, false, "Printed"));
		cols.add(new TablePageColumn(C.lm_time_paid, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Paid Date"));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, false));
		cols.add(new TablePageColumn(C.recipient_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.recipient_id, true, true));
		cols.add(new TablePageColumn(C.payment_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.payment_id, true, false));
		cols.add(new TablePageColumn(C.transfer_status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.transfer_status_id, true, true));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, C.lm_user));
		cols.add(new TablePageColumn(C.is_verified, C.columnTypeNumber, C.columnDirectionNone, false, true, C.is_verified, true, false));
		cols.add(new TablePageColumn(C.is_app, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_app, true, false));
		cols.add(new TablePageColumn(C.transfer_through_bank_name, C.columnTypeString, C.columnDirectionDefault, true, false, C.transfer_through_bank_name, true, true));
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
		cols.get(C.lm_time_paid).setValue(r.getTimestamp(C.lm_time_paid));
		cols.get(C.transfer_through_bank_name).setValue(r.getString(C.transfer_through_bank_name));
		
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
				cols.get(C.recipient_name).setValue(C.emptyString);
				cols.get(C.bank_code).setValue(C.emptyString);
				cols.get(C.bank_name).setValue(C.emptyString);
				cols.get(C.bank_acc).setValue(C.emptyString);
				cols.get(C.is_verified).setValue(C.emptyString);
				cols.get(C.swift_code).setValue(C.emptyString);
			}
		}
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
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
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		pstmtMember.close();
		pstmtRecipient.close();
	}

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		if (action.equals(C.print)) {
			long[] txn_ids = Helper.getLongArray(params, C.txn_ids, true);
			return exportBOT(fi, txn_ids);
		} else if (action.equals(C.import_bot)) {
			return importBOT(fi);
		} else if (action.equals(C.confirm_bot)) {
			String uuid = Helper.getString(params, C.uuid, true);
			confirm(fi, uuid);
			return "1";
		}
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
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
		
		sb.append(C.char_31).append(fi.getConnection().getGlobalConfig(C.kotsms, C.msg_footnote));
		
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
	
	public String exportBOT(FunctionItem fi, long[] txn_ids) throws Exception {
		Timestamp currentTime = fi.getConnection().getCurrentTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String currentDate = dateFormat.format(currentTime);
		long seq = fi.getConnection().getSeq(C.export_bot + currentDate, true);
		StringBuilder sb = new StringBuilder(10);
		sb.append("bot_")
		.append(currentDate);
		if (seq < 10)
			sb.append("0");
		sb.append(seq).append(".csv");
		
		String filename = sb.toString();
		PrintWriter pw = new PrintWriter(new File(fi.getTempFolder(), filename));
		pw.print("Reference No,Sender Name 1,Sender Name 2,Beneficiary Name,Account No.,CCY,Remit Amount,Beneficiary Bank,TWD Amount,Other Information" + C.crlf);
		try {
			Connection conn = fi.getConnection().getConnection();
			PreparedStatement pstmt = conn.prepareStatement("select payment_info, member_id, recipient_id, transfer_amount_idr, transfer_status_id, lm_time, transfer_amount_ntd from money_transfer where txn_id = ? for update");
			PreparedStatement pstmtMember = conn.prepareStatement("select member_name from member where member_id = ?");
			PreparedStatement pstmtRecipient = conn.prepareStatement("select recipient_name, bank_acc, (select bank_name from bank_code_list where bank_code = a.bank_code) as bank_name from member_recipient a where member_id = ? and recipient_id = ?");
			try {
				MoneyTransfer moneyTransfer = new MoneyTransfer();
				Arrays.sort(txn_ids);
				List<MoneyTransferModel> updateList = new ArrayList<>();
				for (long txn_id : txn_ids) {
					MoneyTransferModel m = new MoneyTransferModel();
					pstmt.setLong(1, txn_id);
					try (ResultSet r = pstmt.executeQuery()) {
						if (r.next()) {
							m.txn_id = txn_id;
							m.payment_info = r.getString(1);
							m.member_id = r.getLong(2);
							m.recipient_id = r.getInt(3);
							m.transfer_amount_idr = r.getLong(4);
							m.transfer_status_id = TransferStatus.get(r.getInt(5));
							m.lm_time = r.getTimestamp(6);
							m.transfer_amount_ntd = r.getInt(7);
							updateList.add(m);
						}
					}
				}
				
				for (MoneyTransferModel m : updateList) {
					if (m.transfer_status_id == TransferStatus.paid) {
						moneyTransfer.changeStatus(fi, m.txn_id, m.lm_time, TransferStatus.process, "export BOT", currentTime, TransferMoneyThroughBank.BOT);
					} else if (m.transfer_status_id != TransferStatus.process) {
						throw new Exception("only status paid or process can do export BOT, problem txn_id = " + m.txn_id);
					}
					
					String member_name;
					pstmtMember.setLong(1, m.member_id);
					try (ResultSet r = pstmtMember.executeQuery()) {
						if (r.next()) {
							member_name = r.getString(1);
						} else
							continue;
					}
					
					String recipient_name;
					String bank_acc;
					String bank_name;
					pstmtRecipient.setLong(1, m.member_id);
					pstmtRecipient.setInt(2, m.recipient_id);
					try (ResultSet r = pstmtRecipient.executeQuery()) {
						if (r.next()) {
							recipient_name = r.getString(1);
							bank_acc = r.getString(2);
							bank_name = r.getString(3);
						} else
							continue;
					}
					
					sb = new StringBuilder(50);
					sb.append(m.txn_id).append(",")
					.append(m.payment_info).append(",")
					.append(member_name).append(",")
					.append(recipient_name).append(",")
					.append(bank_acc).append(",")
					.append("IDR,")
					.append(m.transfer_amount_idr).append(",")
					.append(bank_name).append(",")
					.append(m.transfer_amount_ntd).append(",")
					.append(C.crlf);
					pw.print(sb.toString());
				}
				conn.commit();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				pstmt.close();
				pstmtMember.close();
				pstmtRecipient.close();
			}
		} finally {
			pw.close();
		}
		return filename;
	}
	
	private String importBOT(FunctionItem fi) throws Exception {
		Hashtable<String, FileItem> files = fi.getUploadedFiles();
		if (files.size() > 0) {
			FileItem file = files.get(C.import_bot);
			if (file != null && file.getName().length() > 0) {
				StringBuilder sb = new StringBuilder(100);
				
				BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
				String line = br.readLine();
				if (line != null) {
					String uuid = UUID.randomUUID().toString().replaceAll("-", "");
					Connection conn = fi.getConnection().getConnection();
					PreparedStatement pstmt = conn.prepareStatement("insert into sql_temp_table (session_id, page_id, long1, item1, item2, item3, item4, item5, item6, item7, item8, item9, item10, num1) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
					PreparedStatement pstmtCount = conn.prepareStatement("select transfer_status_id from money_transfer where payment_info = ?");
					try {
						sb.append(uuid).append(C.char_30);
						long seq = 1;
						
						line = br.readLine();
						while (line != null) {
							String[] cells = StringUtils.splitPreserveAllTokens(line, ',');
							String txn_id = cells[0];
							String payment_info = cells[1];
							String member_name = cells[2];
							String recipient_name = cells[3];
							String bank_acc = cells[4];
							long transfer_amount_idr = Long.parseLong(cells[6]);
							String bank_name = cells[7];
							int transfer_amount_ntd = Integer.parseInt(cells[8]);
							String comment = cells[9];
							String etx = C.emptyString;
							int ecd = 0;
							
							pstmtCount.setString(1, payment_info);
							ResultSet r = pstmtCount.executeQuery();
							try {
								if (r.next()) {
									TransferStatus status = TransferStatus.get(r.getInt(1));
									if (status != TransferStatus.process) {
										etx = "transfer status is not process";
										ecd = 2;
									}
								} else {
									etx = "invoice not found";
									ecd = 1;
								}
							} finally {
								r.close();
							}
							
							pstmt.setString(1, fi.getSID());
							pstmt.setString(2, uuid);
							pstmt.setLong(3, seq);
							pstmt.setString(4, txn_id);
							pstmt.setString(5, payment_info);
							pstmt.setString(6, member_name);
							pstmt.setString(7, recipient_name);
							pstmt.setString(8, bank_acc);
							pstmt.setString(9, Stringify.getCurrency(transfer_amount_idr));
							pstmt.setString(10, bank_name);
							pstmt.setString(11, Stringify.getCurrency(transfer_amount_ntd));
							pstmt.setString(12, comment);
							pstmt.setString(13, etx);
							pstmt.setInt(14, ecd);
							pstmt.executeUpdate();
							seq++;
							
							sb.append(txn_id).append(C.char_31)
							.append(payment_info).append(C.char_31)
							.append(member_name).append(C.char_31)
							.append(recipient_name).append(C.char_31)
							.append(bank_acc).append(C.char_31)
							.append(Stringify.getCurrency(transfer_amount_idr)).append(C.char_31)
							.append(bank_name).append(C.char_31)
							.append(Stringify.getCurrency(transfer_amount_ntd)).append(C.char_31)
							.append(comment).append(C.char_31)
							.append(etx).append(C.char_31)
							.append(ecd).append(C.char_30);
							
							line = br.readLine();
						}
						
						sb.delete(sb.length() - 1, sb.length());
						
						conn.commit();
						
						return sb.toString();
					} catch (Exception e) {
						conn.rollback();
						throw e;
					} finally {
						pstmt.close();
						pstmtCount.close();
					}
				} else
					throw new Exception("empty file");
			} else
				throw new Exception("file import_bot not found");
		} else
			throw new Exception("no files");
	}
	
	private void confirm(FunctionItem fi, String uuid) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select item2 from sql_temp_table where session_id = ? and page_id = ? and num1 = 0");
		PreparedStatement pstmtGetTxn = conn.prepareStatement("select txn_id from money_transfer where payment_info = ?");
		PreparedStatement pstmtDeleteTemp = conn.prepareStatement("delete from sql_temp_table where session_id = ? and page_id = ?");
		PreparedStatement pstmtLock = conn.prepareStatement("select transfer_status_id, payment_info, lm_time, member_id, is_app, point_used from money_transfer where txn_id = ? for update");
		PreparedStatement pstmtLockMember = conn.prepareStatement("select app_remit_count from member where member_id = ? for update");
		try {
			List<String> list = new ArrayList<>();
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, uuid);
			try (ResultSet r = pstmt.executeQuery()) {
				while (r.next()) {
					list.add(r.getString(1));
				}
			}

			List<Long> txnIds = new ArrayList<>();
			for (String paymentInfo : list) {
				pstmtGetTxn.setString(1, paymentInfo);
				try (ResultSet r = pstmtGetTxn.executeQuery()) {
					if (r.next()) {
						txnIds.add(r.getLong(1));
					} else {
						throw new Exception("payment info [" + paymentInfo + "] not exist");
					}
				}
			}
			
			Collections.sort(txnIds);
			
			List<Timestamp> lmTimes = new ArrayList<>();
			Set<Long> unique_member_ids = new HashSet<>();
			for (long txnId : txnIds) {
				pstmtLock.setLong(1, txnId);
				try (ResultSet r = pstmtLock.executeQuery()) {
					if (r.next()) {
						TransferStatus status = TransferStatus.get(r.getInt(1));
						String payment_info = r.getString(2);
						Timestamp lm_time = r.getTimestamp(3);
						long member_id = r.getLong(4);
						boolean is_app = r.getInt(5) == 1;
						int point_used = r.getInt(6);
						
						if (status != TransferStatus.process)
							throw new Exception("payment_info [" + payment_info + "] has wrong status to do confirm BOT");
						
						lmTimes.add(lm_time);
						
						if (is_app && point_used == 0) {
							unique_member_ids.add(member_id);
						}
					} else {
						throw new Exception(String.format(C.data_not_exist, "txn_id = " + txnId));
					}
				}
			}
			
			List<Long> member_ids = new ArrayList<>();
			for (long l : unique_member_ids) {
				member_ids.add(l);
			}
			
			Collections.sort(member_ids);
			
			for (long member_id : member_ids) {
				pstmtLockMember.setLong(1, member_id);
				try (ResultSet r = pstmtLockMember.executeQuery()) {
					if (r.next()) {
						
					} else {
						throw new Exception("member_id [" + member_id + "] not exist");
					}
				}
			}
			
			pstmtDeleteTemp.setString(1, fi.getSID());
			pstmtDeleteTemp.setString(2, uuid);
			pstmtDeleteTemp.executeUpdate();
			
			Timestamp currentTime = fi.getConnection().getCurrentTime();
			MoneyTransfer moneyTransfer = new MoneyTransfer();
			for (int i = 0; i < txnIds.size(); i++) {
				long txnId = txnIds.get(i);
				Timestamp lmTime = lmTimes.get(i);
				moneyTransfer.changeStatus(fi, txnId, lmTime, TransferStatus.transfered, "import BOT", currentTime, TransferMoneyThroughBank.BOT);
			}
			
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		} finally {
			pstmt.close();
			pstmtLock.close();
			pstmtGetTxn.close();
			pstmtDeleteTemp.close();
			pstmtLockMember.close();
		}
	}

}
