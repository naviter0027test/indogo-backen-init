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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.indogo.PaymentType;
import com.indogo.TransferMoneyThroughBank;
import com.indogo.TransferStatus;
import com.indogo.bri.BriHostToHost;
import com.indogo.bri.BriHostToHostException;
import com.indogo.bri.BriPaymentResult;
import com.indogo.bri.VostroInfo;
import com.indogo.model.member.BankCodeModel;
import com.indogo.model.member.MoneyTransferModel;
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

public class MoneyTransferBRI implements ITablePage, IFunction {
	
	private static Log LOG = LogFactory.getLog(MoneyTransferBRI.class);

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
		return cols;
	}
	
	private PreparedStatement pstmtMember = null;
	private PreparedStatement pstmtRecipient = null;

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
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		Connection conn = fi.getConnection().getConnection();
		
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
			
			sb.append(C.char_31).append(fi.getConnection().getGlobalConfig(C.kotsms, C.msg_footnote));
			
			return sb.toString();
		} else if (action.equals(C.print)) {
			long[] txn_ids = Helper.getLongArray(params, C.txn_ids, true);
			return exportBRI(fi, txn_ids);
		} else if (action.equals(C.import_bri)) {
			return importBRI(fi);
		} else if (action.equals(C.import_bri_rev_2)) {
			return importBRIrev2(fi);
		} else if (action.equals(C.confirm_bri)) {
			String uuid = Helper.getString(params, C.uuid, true);
			confirm(fi, uuid);
			return "1";
		} else if (action.equals(C.sql_temp_table)) {
			TransferStatus transfer_status = TransferStatus.get(Helper.getInt(params, C.transfer_status_id, true));
			String bank_code = Helper.getString(params, C.bank_code, false);
			String exclude_bank_code = Helper.getString(params, C.exclude_bank_code, false);
			try {
				int count;
				if (!Helper.isNullOrEmpty(bank_code)) {
					count = sqlTempTableWithBankCode(fi, transfer_status, bank_code);
				} else {
					count = sqlTempTableExcludeBankCode(fi, transfer_status, exclude_bank_code);
				}
				conn.commit();
				return String.valueOf(count);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.vostro_inquiry)) {
			String password = Helper.getString(params, C.private_key_password, true);
			BriHostToHost h2h = new BriHostToHost(fi.getConnection(), password);
			String token = h2h.requestToken();
			VostroInfo info = h2h.inquiryVostro(token);
			
			StringBuilder sb = new StringBuilder();
			sb.append(info.accountName)
			.append(C.char_31).append(info.balance)
			.append(C.char_31).append(info.currency);
			return sb.toString();
		} else if (action.equals(C.bri_transfer)) {
			long txn_id = Helper.getLong(params, C.txn_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			String password = Helper.getString(params, C.password, true);
			try {
				Timestamp currentTime = fi.getConnection().getCurrentTime();
				BriPaymentResult result = doBriTransfer(fi, txn_id, lm_time, password, currentTime);
				if (result.ecd == 0) {
					conn.commit();
				} else {
					conn.rollback();
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append(txn_id)
				.append(C.char_31).append(Stringify.getTimestamp(currentTime))
				.append(C.char_31).append(result.message)
				.append(C.char_31).append(fi.getSessionInfo().getUserName())
				.append(C.char_31).append(TransferStatus.transfered.getId())
				.append(C.char_31).append(TransferStatus.transfered.name());
				return sb.toString();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.payment_order_inquiry)) {
			String payment_info = Helper.getString(params, C.payment_info, true);
			BriHostToHost bri = new BriHostToHost(fi.getConnection());
			String token = bri.requestToken();
			String status = bri.inquiryTransaction(token, payment_info);
			return status;
		} else if (action.equals(C.change_password)) {
			String password = Helper.getString(params, C.password, true);
			BriHostToHost bri = new BriHostToHost(fi.getConnection(), password);
			bri.requestToken();
			
			String encryptedPassword = BriHostToHost.encrypt(password);
			fi.getConnection().setGlobalConfig(C.bri_h2h, C.password, encryptedPassword);
			return encryptedPassword;
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}
	
	public String exportBRI(FunctionItem fi, long[] txn_ids) throws Exception {
		Timestamp currentTime = fi.getConnection().getCurrentTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String currentDate = dateFormat.format(currentTime);
		long seq = fi.getConnection().getSeq(C.export_bri + currentDate, true);
		StringBuilder sb = new StringBuilder(10);
		sb.append("bri_")
		.append(currentDate);
		if (seq < 10)
			sb.append("0");
		sb.append(seq).append(".csv");
		
		String filename = sb.toString();
		PrintWriter pw = new PrintWriter(new File(fi.getTempFolder(), filename));
		try {
			Connection conn = fi.getConnection().getConnection();
			PreparedStatement pstmt = conn.prepareStatement("select payment_info, member_id, recipient_id, transfer_amount_idr, transfer_status_id, lm_time from money_transfer where txn_id = ? for update");
			PreparedStatement pstmtMember = conn.prepareStatement("select member_name, arc_no, phone_no from member where member_id = ?");
			PreparedStatement pstmtRecipient = conn.prepareStatement("select recipient_name, bank_code, bank_acc from member_recipient where member_id = ? and recipient_id = ?");
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
							updateList.add(m);
						}
					}
				}
				
				for (MoneyTransferModel m : updateList) {
					if (m.transfer_status_id == TransferStatus.paid) {
						moneyTransfer.changeStatus(fi, m.txn_id, m.lm_time, TransferStatus.process, "export BRI", currentTime, TransferMoneyThroughBank.BRI);
					} else if (m.transfer_status_id != TransferStatus.process) {
						throw new Exception("only status paid or process can do export BRI, problem txn_id = " + m.txn_id);
					}
					
					String member_name;
					String arc_no;
					String phone_no = "";
					pstmtMember.setLong(1, m.member_id);
					try (ResultSet r = pstmtMember.executeQuery()) {
						if (r.next()) {
							member_name = r.getString(1);
							arc_no = r.getString(2);
							String[] tokens = StringUtils.split(r.getString(3), '.');
							for (int i = 0; i < tokens.length; i++) {
								if (tokens[i].length() > 0) {
									phone_no = tokens[i];
									break;
								}
							}
						} else
							continue;
					}
					
					String recipient_name;
					String bank_code;
					String bank_acc;
					pstmtRecipient.setLong(1, m.member_id);
					pstmtRecipient.setInt(2, m.recipient_id);
					try (ResultSet r = pstmtRecipient.executeQuery()) {
						if (r.next()) {
							recipient_name = r.getString(1);
							bank_code = r.getString(2);
							bank_acc = r.getString(3);
						} else
							continue;
					}
					
					int fee = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, bank_code.equals("002") ? C.bri_fee_idr : C.bri_fee_to_non_bri));
					long total_transfer = m.transfer_amount_idr + fee;
					
					sb = new StringBuilder(50);
					sb.append(m.payment_info).append("|")
					.append(" (").append(member_name).append(") |0|")
					.append(arc_no).append("|")
					.append(bank_acc).append("|")
					.append(recipient_name).append("|IDR|")
					.append(total_transfer).append("||")
					.append(phone_no).append(" |")
					.append(bank_code).append(C.crlf);
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

	private String importBRI(FunctionItem fi) throws Exception {
		Hashtable<String, FileItem> files = fi.getUploadedFiles();
		if (files.size() > 0) {
			FileItem file = files.get(C.import_bri);
			if (file != null && file.getName().length() > 0) {
				StringBuilder sb = new StringBuilder(100);
				CSVParser records = CSVFormat.EXCEL.withHeader().parse(new InputStreamReader(file.getInputStream()));
				try {
					Map<String, Integer> header = records.getHeaderMap();
					if (!header.containsKey(C.import_bri_reference_number))
						throw new Exception("csv header [" + C.import_bri_reference_number + "] not found");
					if (!header.containsKey(C.import_bri_counterpart))
						throw new Exception("csv header [" + C.import_bri_counterpart + "] not found");
					if (!header.containsKey(C.import_bri_status_desc))
						throw new Exception("csv header [" + C.import_bri_status_desc + "] not found");
					if (!header.containsKey(C.import_bri_date))
						throw new Exception("csv header [" + C.import_bri_date + "] not found");
					if (!header.containsKey(C.import_bri_credit_account))
						throw new Exception("csv header [" + C.import_bri_credit_account + "] not found");
					if (!header.containsKey(C.import_bri_amount))
						throw new Exception("csv header [" + C.import_bri_amount + "] not found");
					
					String uuid = UUID.randomUUID().toString().replaceAll("-", "");
					Connection conn = fi.getConnection().getConnection();
					PreparedStatement pstmt = conn.prepareStatement("insert into sql_temp_table (session_id, page_id, long1, item1, item2, item3, item4, item5, item6, item7, num1) values(?,?,?,?,?,?,?,?,?,?,?)");
					PreparedStatement pstmtCount = conn.prepareStatement("select transfer_status_id from money_transfer where payment_info = ?");
					try {
						sb.append(uuid).append(C.char_30);
						long seq = 1;
						String success_status_desc = fi.getConnection().getGlobalConfig(C.import_bri, C.success_status_desc);
						for (CSVRecord record : records) {
							String c1 = record.get(C.import_bri_reference_number);
							String c2 = record.get(C.import_bri_counterpart);
							String c3 = record.get(C.import_bri_status_desc);
							String c4 = record.get(C.import_bri_date);
							String c5 = record.get(C.import_bri_credit_account);
							String c6 = record.get(C.import_bri_amount);
							
							long amount = Long.parseLong(c6);
							String etx = C.emptyString;
							int ecd = 0;
							
							String payment_info = c1.startsWith(C.import_bri_brc04) ? c1.substring(C.import_bri_brc04.length()) : c1;
							
							if (c3.equalsIgnoreCase(success_status_desc)) {
								TransferStatus status = null;
								pstmtCount.setString(1, payment_info);
								ResultSet r = pstmtCount.executeQuery();
								try {
									if (r.next()) {
										status = TransferStatus.get(r.getInt(1));
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
							} else {
								etx = "BRI reply failed";
								ecd = 3;
							}
							
							pstmt.setString(1, fi.getSID());
							pstmt.setString(2, uuid);
							pstmt.setLong(3, seq);
							pstmt.setString(4, payment_info);
							pstmt.setString(5, c2);
							pstmt.setString(6, c3);
							pstmt.setString(7, c4);
							pstmt.setString(8, c5);
							pstmt.setString(9, c6);
							pstmt.setString(10, etx);
							pstmt.setInt(11, ecd);
							pstmt.executeUpdate();
							seq++;
							
							sb.append(c1).append(C.char_31)
							.append(c2).append(C.char_31)
							.append(c3).append(C.char_31)
							.append(c4).append(C.char_31)
							.append(c5).append(C.char_31)
							.append(String.format("%,d", amount)).append(C.char_31)
							.append(etx).append(C.char_31)
							.append(ecd).append(C.char_30);
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
				} finally {
					try {
						records.close();
					} catch (Exception ignore) {}
				}
			} else
				throw new Exception("file import_bri not found");
		} else
			throw new Exception("no files");
	}
	
	private String importBRIrev2(FunctionItem fi) throws Exception {
		Hashtable<String, FileItem> files = fi.getUploadedFiles();
		if (files.size() > 0) {
			FileItem file = files.get(C.import_bri);
			if (file != null && file.getName().length() > 0) {
				StringBuilder sb = new StringBuilder(100);
				BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
				// skip 2 lines:
				// Date Printed:,16/5/17 10:4:27
				// Period:,2017-05-16 until 2017-05-16
				reader.readLine();
				reader.readLine();
				CSVParser records = CSVFormat.EXCEL.withHeader().parse(reader);
				try {
					Map<String, Integer> header = records.getHeaderMap();
					if (!header.containsKey(C.import_bri_reference_number))
						throw new Exception("csv header [" + C.import_bri_reference_number + "] not found");
					if (!header.containsKey(C.import_bri_counterpart))
						throw new Exception("csv header [" + C.import_bri_counterpart + "] not found");
					if (!header.containsKey(C.import_bri_status_desc_rev_2))
						throw new Exception("csv header [" + C.import_bri_status_desc_rev_2 + "] not found");
					if (!header.containsKey(C.import_bri_date_rev_2))
						throw new Exception("csv header [" + C.import_bri_date_rev_2 + "] not found");
					if (!header.containsKey(C.import_bri_credit_account))
						throw new Exception("csv header [" + C.import_bri_credit_account + "] not found");
					if (!header.containsKey(C.import_bri_amount_rev_2))
						throw new Exception("csv header [" + C.import_bri_amount_rev_2 + "] not found");
					
					String uuid = UUID.randomUUID().toString().replaceAll("-", "");
					Connection conn = fi.getConnection().getConnection();
					PreparedStatement pstmt = conn.prepareStatement("insert into sql_temp_table (session_id, page_id, long1, item1, item2, item3, item4, item5, item6, item7, num1) values(?,?,?,?,?,?,?,?,?,?,?)");
					PreparedStatement pstmtCount = conn.prepareStatement("select transfer_status_id from money_transfer where payment_info = ?");
					try {
						sb.append(uuid).append(C.char_30);
						long seq = 1;
						String success_status_desc = fi.getConnection().getGlobalConfig(C.import_bri, C.success_status_desc);
						for (CSVRecord record : records) {
							String c1 = record.get(C.import_bri_reference_number);
							String c2 = record.get(C.import_bri_counterpart);
							String c3 = record.get(C.import_bri_status_desc_rev_2);
							String c4 = record.get(C.import_bri_date_rev_2);
							String c5 = record.get(C.import_bri_credit_account);
							String c6 = record.get(C.import_bri_amount_rev_2);
							
							long amount = Long.parseLong(c6);
							String etx = C.emptyString;
							int ecd = 0;
							
							String payment_info = c1.startsWith(C.import_bri_brc04) ? c1.substring(C.import_bri_brc04.length()) : c1;
							
							if (c3.equalsIgnoreCase(success_status_desc)) {
								TransferStatus status = null;
								pstmtCount.setString(1, payment_info);
								ResultSet r = pstmtCount.executeQuery();
								try {
									if (r.next()) {
										status = TransferStatus.get(r.getInt(1));
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
							} else {
								etx = "BRI reply failed";
								ecd = 3;
							}
							
							pstmt.setString(1, fi.getSID());
							pstmt.setString(2, uuid);
							pstmt.setLong(3, seq);
							pstmt.setString(4, payment_info);
							pstmt.setString(5, c2);
							pstmt.setString(6, c3);
							pstmt.setString(7, c4);
							pstmt.setString(8, c5);
							pstmt.setString(9, c6);
							pstmt.setString(10, etx);
							pstmt.setInt(11, ecd);
							pstmt.executeUpdate();
							seq++;
							
							sb.append(c1).append(C.char_31)
							.append(c2).append(C.char_31)
							.append(c3).append(C.char_31)
							.append(c4).append(C.char_31)
							.append(c5).append(C.char_31)
							.append(String.format("%,d", amount)).append(C.char_31)
							.append(etx).append(C.char_31)
							.append(ecd).append(C.char_30);
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
				} finally {
					try {
						records.close();
					} catch (Exception ignore) {}
				}
			} else
				throw new Exception("file import_bri not found");
		} else
			throw new Exception("no files");
	}
	
	private void confirm(FunctionItem fi, String uuid) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select item1 from sql_temp_table where session_id = ? and page_id = ? and num1 = 0");
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
							throw new Exception("payment_info [" + payment_info + "] has wrong status to do confirm BRI");
						
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
				moneyTransfer.changeStatus(fi, txnId, lmTime, TransferStatus.transfered, "import BRI", currentTime, TransferMoneyThroughBank.BRI);
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
	
	private int sqlTempTableWithBankCode(FunctionItem fi, TransferStatus transfer_status, String bank_code) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatement pstmt = conn.prepareStatement("delete from sql_temp_table where session_id = ? and page_id = ?")) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.money_transfer_bri);
			pstmt.executeUpdate();
		}
		
		List<Long> txn_ids = new ArrayList<>();
		try (PreparedStatement pstmt = conn.prepareStatement("select distinct a.txn_id from money_transfer a, member_recipient b where a.member_id = b.member_id and a.recipient_id = b.recipient_id and a.transfer_status_id = ? and b.bank_code = ?")) {
			pstmt.setInt(1, transfer_status.getId());
			pstmt.setString(2, bank_code);
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
					pstmt.setString(2, C.money_transfer_bri);
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
	
	private int sqlTempTableExcludeBankCode(FunctionItem fi, TransferStatus transfer_status, String bank_code) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatement pstmt = conn.prepareStatement("delete from sql_temp_table where session_id = ? and page_id = ?")) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.money_transfer_bri);
			pstmt.executeUpdate();
		}
		
		List<Long> txn_ids = new ArrayList<>();
		try (PreparedStatement pstmt = conn.prepareStatement("select distinct a.txn_id from money_transfer a, member_recipient b where a.member_id = b.member_id and a.recipient_id = b.recipient_id and a.transfer_status_id = ? and b.bank_code <> ?")) {
			pstmt.setInt(1, transfer_status.getId());
			pstmt.setString(2, bank_code);
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
					pstmt.setString(2, C.money_transfer_bri);
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
	
	private BriPaymentResult doBriTransfer(FunctionItem fi, long txn_id, Timestamp lm_time, String password, Timestamp currentTime) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select member_id, recipient_id, transfer_amount_idr, payment_info, bni_retry from money_transfer where txn_id = ?");
		PreparedStatement pstmtMember = conn.prepareStatement("select member_name, arc_no from member where member_id = ?");
		PreparedStatement pstmtMemberRecipient = conn.prepareStatement("select recipient_name, bank_code, bank_acc from member_recipient where member_id = ? and recipient_id = ?");
		PreparedStatement pstmtUpdate = conn.prepareStatement("update money_transfer set bni_trx_date = ? where txn_id = ?");
		try {
			new MoneyTransfer().changeStatus(fi, txn_id, lm_time, TransferStatus.transfered, "BRI auto transfer", currentTime, TransferMoneyThroughBank.BRI);
			
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
			
			String member_name, arc_no;
			pstmtMember.setLong(1, member_id);
			r = pstmtMember.executeQuery();
			try {
				if (r.next()) {
					member_name = r.getString(1);
					arc_no = r.getString(2);
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
			
			String refNumber = Helper.isNullOrEmpty(payment_info) ? String.valueOf(txn_id) : payment_info;
			if (bni_retry > 0) {
				refNumber = refNumber + bni_retry;
			}
			
			int fee = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, bank_code.equals("002") ? C.bri_fee_idr : C.bri_fee_to_non_bri));
			
			BriPaymentResult result = new BriPaymentResult();
			BriHostToHost h2h = new BriHostToHost(fi.getConnection(), password);
			String token = h2h.requestToken();
			try {
				String ticketNumber = h2h.paymentAccount(token, refNumber, bank_acc, recipient_name, C.emptyString, "NA", member_name, C.TAIWAN, arc_no, bank_code, transfer_amount_idr + fee);

				pstmtUpdate.setString(1, ticketNumber);
				pstmtUpdate.setLong(2, txn_id);
				pstmtUpdate.executeUpdate();
				
				result.ecd = 0;
				result.message = C.success;
				result.ticketNumber = ticketNumber;
			} catch (BriHostToHostException e) {
				result.ecd = 1;
				result.message = e.getMessage();
				LOG.error(e.getMessage(), e);
			}
			return result;
		} finally {
			pstmt.close();
			pstmtMember.close();
			pstmtMemberRecipient.close();
			pstmtUpdate.close();
		}
	}
}
