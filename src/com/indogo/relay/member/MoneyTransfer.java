package com.indogo.relay.member;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.indogo.IndogoTable;
import com.indogo.MemberStatus;
import com.indogo.PaymentType;
import com.indogo.TransferMoneyThroughBank;
import com.indogo.TransferStatus;
import com.indogo.model.UpdateResult;
import com.indogo.model.member.BankCodeModel;
import com.indogo.model.member.MemberModel;
import com.indogo.model.member.MoneyTransferModel;
import com.indogo.model.member.MoneyTransferStatusModel;
import com.indogo.model.member.RecipientModel;
import com.indogo.relay.bookkeeping.IdrBookkeeping;
import com.indogo.relay.member.BankCodeConfiguration.OrderBy;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lionpig.webui.database.HistoryAction;
import com.lionpig.webui.database.HistoryData;
import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.database.RoleNameListModel;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.struct.LogInfo;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.DateFormat;
import com.lionpig.webui.http.util.Helper;
import com.lionpig.webui.http.util.Stringify;

public class MoneyTransfer implements IFunction, ITablePage {
	
	private String recipient_photo_url;

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
		List<TablePageColumn> cols = new ArrayList<TablePageColumn>();
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, " ", true));
		cols.add(new TablePageColumn(C.transfer_status_name, C.columnTypeString, C.columnDirectionNone, false, true, "Status"));
		cols.add(new TablePageColumn(C.payment_info, C.columnTypeString, C.columnDirectionDefault, true, false, "Payment Info"));
		cols.add(new TablePageColumn(C.comment, C.columnTypeString, C.columnDirectionDefault, true, false, "Comment"));
		cols.add(new TablePageColumn(C.is_print, C.columnTypeString, C.columnDirectionDefault, true, false, "Printed", false, true));
		cols.add(new TablePageColumn(C.txn_id, C.columnTypeNumber, C.columnDirectionDesc, true, false, "Invoice"));
		cols.add(new TablePageColumn(C.member_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Member Name"));
		cols.add(new TablePageColumn(C.phone_no, C.columnTypeString, C.columnDirectionDefault, true, false, "Phone No"));
		cols.add(new TablePageColumn(C.arc_no, C.columnTypeString, C.columnDirectionDefault, true, false, "ARC"));
		cols.add(new TablePageColumn(C.arc_expire_date, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "ARC Expire", false, true));
		cols.add(new TablePageColumn(C.recipient_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Recipient Name"));
		cols.add(new TablePageColumn(C.recipient_name_2, C.columnTypeString, C.columnDirectionDefault, true, false, "Recipient Name 2"));
		cols.add(new TablePageColumn(C.bank_code, C.columnTypeString, C.columnDirectionDefault, true, false, "Bank Code"));
		cols.add(new TablePageColumn(C.bank_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Bank Name"));
		cols.add(new TablePageColumn(C.swift_code, C.columnTypeString, C.columnDirectionDefault, true, false, "SWIFT"));
		cols.add(new TablePageColumn(C.bank_acc, C.columnTypeString, C.columnDirectionDefault, true, false, "Bank Account"));
		cols.add(new TablePageColumn(C.payment_name, C.columnTypeString, C.columnDirectionNone, false, true, "Payment Type", false, true));
		cols.add(new TablePageColumn(C.kurs_value, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Kurs"));
		cols.add(new TablePageColumn(C.transfer_amount_ntd, C.columnTypeNumber, C.columnDirectionDefault, true, false, "NTD"));
		cols.add(new TablePageColumn(C.transfer_amount_idr, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Rupiah"));
		cols.add(new TablePageColumn(C.service_charge, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Ongkir"));
		cols.add(new TablePageColumn(C.total, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total (NTD)"));
		cols.add(new TablePageColumn(C.recipient_birthday, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Recipient Birthday"));
		cols.add(new TablePageColumn(C.id_filename, C.columnTypeString, C.columnDirectionDefault, true, false, "Recipient ID Filename"));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, false));
		cols.add(new TablePageColumn(C.recipient_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.recipient_id, true, true));
		cols.add(new TablePageColumn(C.payment_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.payment_id, true, true));
		cols.add(new TablePageColumn(C.transfer_status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.transfer_status_id, true, true));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, C.lm_user));
		cols.add(new TablePageColumn(C.is_verified, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_verified, true, false));
		cols.add(new TablePageColumn(C.transfer_through_bank_name, C.columnTypeString, C.columnDirectionDefault, true, false, C.transfer_through_bank_name, true, true));
		cols.add(new TablePageColumn(C.is_app, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_app, true, false));
		cols.add(new TablePageColumn(C.bni_trx_date, C.columnTypeString, C.columnDirectionDefault, true, false, C.bni_trx_date, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		TransferStatus status = TransferStatus.get(r.unwrap().getInt(C.transfer_status_id));
		PaymentType paymentType = PaymentType.get(r.unwrap().getInt(C.payment_id));
		long member_id = r.unwrap().getLong(C.member_id);
		int recipient_id = r.unwrap().getInt(C.recipient_id);
		String id_filename = r.getString(C.id_filename);
		String id_image = RecipientView.getRecipientPhotoUrl(recipient_photo_url, member_id, recipient_id, id_filename);
		
		cols.get(C.action).setValue("<button class=\"print\">print declaration</button><button class=\"view_history\">view history</button>");
		cols.get(C.transfer_status_name).setValue(status.name());
		cols.get(C.payment_info).setValue(r.getString(C.payment_info));
		cols.get(C.comment).setValue(r.getString(C.comment));
		cols.get(C.is_print).setValue(r.getInt(C.is_print));
		cols.get(C.txn_id).setValue(r.getLong(C.txn_id));
		cols.get(C.member_name).setValue(r.getString(C.member_name));
		cols.get(C.phone_no).setValue(r.getString(C.phone_no));
		cols.get(C.arc_no).setValue(r.getString(C.arc_no));
		cols.get(C.arc_expire_date).setValue(r.getDate(C.arc_expire_date));
		cols.get(C.recipient_name).setValue(r.getString(C.recipient_name));
		cols.get(C.recipient_name_2).setValue(r.getString(C.recipient_name_2));
		cols.get(C.bank_code).setValue(r.getString(C.bank_code));
		cols.get(C.bank_name).setValue(r.getString(C.bank_name));
		cols.get(C.swift_code).setValue(r.getString(C.swift_code));
		cols.get(C.bank_acc).setValue(r.getString(C.bank_acc));
		cols.get(C.payment_name).setValue(paymentType.getDisplay());
		cols.get(C.kurs_value).setValue(r.getDouble(C.kurs_value));
		cols.get(C.transfer_amount_ntd).setValue(r.getIntCurrency(C.transfer_amount_ntd));
		cols.get(C.transfer_amount_idr).setValue(r.getLongCurrency(C.transfer_amount_idr));
		cols.get(C.service_charge).setValue(r.getInt(C.service_charge));
		cols.get(C.total).setValue(r.getIntCurrency(C.total));
		cols.get(C.recipient_birthday).setValue(r.getDate(C.recipient_birthday));
		cols.get(C.id_filename).setValue("<a href=\"" + id_image + "\" target=\"_blank\">" + id_filename + "</a>");
		cols.get(C.member_id).setValue(String.valueOf(member_id));
		cols.get(C.recipient_id).setValue(String.valueOf(recipient_id));
		cols.get(C.payment_id).setValue(String.valueOf(paymentType.getId()));
		cols.get(C.transfer_status_id).setValue(String.valueOf(status.getId()));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.is_verified).setValue(r.getInt(C.is_verified));
		cols.get(C.transfer_through_bank_name).setValue(r.getString(C.transfer_through_bank_name));
		cols.get(C.is_app).setValue(r.getInt(C.is_app));
		cols.get(C.bni_trx_date).setValue(r.getString(C.bni_trx_date));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		recipient_photo_url = fi.getConnection().getGlobalConfig(C.member, C.recipient_photo_url);
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
		
		if (action.equals(C.insert)) {
			return insert(fi);
		} else if (action.equals(C.getCurrent)) {
			StringBuilder sb = new StringBuilder(20);
			String kursValue = fi.getConnection().getGlobalConfig(C.member, C.kurs_value);
			String kursLmTime = fi.getConnection().getGlobalConfig(C.member, C.kurs_lm_time);
			String serviceCharge = fi.getConnection().getGlobalConfig(C.money_transfer, C.service_charge);
			sb.append(kursValue).append(C.char_31)
			.append(kursLmTime).append(C.char_31)
			.append(serviceCharge);
			
			List<BankCodeModel> bankCodeList = new BankCodeConfiguration().getBankCodeList(fi, OrderBy.bank_code);
			sb.append(C.char_31).append(bankCodeList.size());
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
			
			return sb.toString();
		} else if (action.equals(C.change_status)) {
			String[] tokens = fi.getConnection().getGlobalConfig(C.money_transfer, C.change_status_allowed_role_ids).split(",");
			HashSet<Integer> allowedRoleIds = new HashSet<>();
			for (String s : tokens) {
				allowedRoleIds.add(Integer.parseInt(s));
			}
			boolean isChangeStatusAllowed = false;
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			for (RoleNameListModel role : roles) {
				if (allowedRoleIds.contains(role.ROLE_ID)) {
					isChangeStatusAllowed = true;
					break;
				}
			}
			if (!isChangeStatusAllowed) {
				throw new Exception("you are not allowed to change status");
			}
			
			long txn_id = Helper.getLong(params, C.txn_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			TransferStatus transfer_status_id = TransferStatus.get(Helper.getInt(params, C.transfer_status_id, true));
			String comment = Helper.getString(params, C.comment, false);
			String transfer_through_bank_name = Helper.getString(params, C.transfer_through_bank_name, false);
			
			try {
				Timestamp newLmTime = fi.getConnection().getCurrentTime();
				
				changeStatus(fi, txn_id, lm_time, transfer_status_id, comment, newLmTime, transfer_through_bank_name == null ? null : TransferMoneyThroughBank.valueOf(transfer_through_bank_name));
				conn.commit();
				
				return DateFormat.getInstance().format(newLmTime);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.init)) {
			StringBuilder sb = new StringBuilder(20);
			sb.append(getTransferStatusList(fi));
			
			List<BankCodeModel> bankCodeList = new BankCodeConfiguration().getBankCodeList(fi, OrderBy.bank_code);
			sb.append(C.char_31).append(bankCodeList.size());
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
			
			sb.append(C.char_31).append(fi.getConnection().getGlobalConfig(C.money_transfer, C.print_label_default));
			
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			sb.append(C.char_31).append(roles.size());
			for (RoleNameListModel role : roles) {
				sb.append(C.char_31).append(role.ROLE_ID);
			}
			
			return sb.toString();
		} else if (action.equals(C.money_transfer_status)) {
			long txnId = Helper.getLong(params, C.txn_id, true);
			List<MoneyTransferStatusModel> rows = this.getStatusChangeHistory(fi, txnId);
			StringBuilder sb = new StringBuilder(50);
			for (MoneyTransferStatusModel row : rows) {
				sb.append(Stringify.getTimestamp(row.lm_time)).append(C.char_31)
				.append(Stringify.getString(row.lm_user)).append(C.char_31)
				.append(row.old_status == null ? C.emptyString : row.old_status.name()).append(C.char_31)
				.append(row.new_status.name()).append(C.char_31)
				.append(Stringify.getString(row.comment)).append(C.char_30);
			}
			if (sb.length() > 0)
				sb.delete(sb.length() - 1, sb.length());
			return sb.toString();
		} else if (action.equals(C.update_payment_info)) {
			long txn_id = Helper.getLong(params, C.txn_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			String payment_info = Helper.getString(params, C.payment_info, true);
			
			PreparedStatement pstmtLock = conn.prepareStatement("select lm_time, payment_id, payment_info from money_transfer where txn_id = ? for update");
			PreparedStatement pstmtUpdate = conn.prepareStatement("update money_transfer set payment_info = ?, lm_time = ? where txn_id = ?");
			try {
				Timestamp old_lm_time;
				PaymentType paymentType;
				String old_payment_info;
				pstmtLock.setLong(1, txn_id);
				ResultSet r = pstmtLock.executeQuery();
				try {
					if (r.next()) {
						old_lm_time = r.getTimestamp(1);
						paymentType = PaymentType.get(r.getInt(2));
						old_payment_info = r.getString(3);
					} else {
						throw new Exception(String.format(C.data_not_exist, "txn_id = " + txn_id));
					}
				} finally {
					r.close();
				}
				
				if (old_lm_time.getTime() != lm_time.getTime()) {
					throw new Exception(String.format(C.data_already_updated_by_another_user, "txn_id = " + txn_id));
				}
				if (paymentType != PaymentType.heimao) {
					throw new Exception("payment type must be heimao");
				}
				
				lm_time = fi.getConnection().getCurrentTime();
				
				pstmtUpdate.setString(1, payment_info);
				pstmtUpdate.setTimestamp(2, lm_time);
				pstmtUpdate.setLong(3, txn_id);
				pstmtUpdate.executeUpdate();
				
				fi.getConnection().logHistory(IndogoTable.money_transfer, txn_id, HistoryAction.update, "", null, fi.getSessionInfo().getUserName(),
						new HistoryData(C.payment_info, old_payment_info, payment_info));
				
				conn.commit();
				
				StringBuilder sb = new StringBuilder();
				sb.append(DateFormat.getInstance().format(lm_time)).append(C.char_31).append(payment_info);
				return sb.toString();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				pstmtLock.close();
			}
		} else if (action.equals(C.print)) {
			long txn_id = Helper.getLong(params, C.txn_id, true);
			String print_mode = Helper.getString(params, C.print_mode, false);
			if (print_mode == null)
				print_mode = C.print_checked_as_pdf;
			String print_label = Helper.getString(params, C.print_label, true);
			boolean use_stamp = Helper.getInt(params, C.use_stamp, false) == 1;
			boolean use_stamp_for_bank = Helper.getInt(params, C.use_stamp_for_bank, false) == 1;
			int checkbox_selection = Helper.getInt(params, C.checkbox_selection, false);
			try {
				String filename = this.print(fi, txn_id, print_label, use_stamp, use_stamp_for_bank, checkbox_selection);
				conn.commit();
				
				if (print_mode.equals(C.print_checked_as_image)) {
					String imageFilename = txn_id + ".jpg";
					PDDocument document = PDDocument.load(new File(fi.getTempFolder(), filename));
					try {
						PDFRenderer pdfRenderer = new PDFRenderer(document);
						BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
						ImageIO.write(bim, "jpg", new File(fi.getTempFolder(), imageFilename));
					} finally {
						document.close();
					}
					FileUtils.deleteQuietly(new File(fi.getTempFolder(), filename));
					return imageFilename;
				} else {
					return filename;
				}
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.print_as_image)) {
			long txn_id = Helper.getLong(params, C.txn_id, true);
			String print_label = Helper.getString(params, C.print_label, true);
			boolean use_stamp = Helper.getInt(params, C.use_stamp, false) == 1;
			boolean use_stamp_for_bank = Helper.getInt(params, C.use_stamp_for_bank, false) == 1;
			int checkbox_selection = Helper.getInt(params, C.checkbox_selection, false);
			try {
				String filename = this.print(fi, txn_id, print_label, use_stamp, use_stamp_for_bank, checkbox_selection);
				conn.commit();
				
				String imageFilename = txn_id + ".jpg";
				PDDocument document = PDDocument.load(new File(fi.getTempFolder(), filename));
				try {
					PDFRenderer pdfRenderer = new PDFRenderer(document);
					BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
					ImageIO.write(bim, "jpg", new File(fi.getTempFolder(), imageFilename));
				} finally {
					document.close();
				}
				return imageFilename;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.print_checked_as_zip) || action.equals(C.print_checked_as_pdf) || action.equals(C.print_checked_as_image)) {
			Long[] txnIds = Helper.getLongNullableArray(params, C.txn_ids, true);
			String print_label = Helper.getString(params, C.print_label, true);
			boolean use_stamp = Helper.getInt(params, C.use_stamp, false) == 1;
			boolean use_stamp_for_bank = Helper.getInt(params, C.use_stamp_for_bank, false) == 1;
			int checkbox_selection = Helper.getInt(params, C.checkbox_selection, false);
			if (action.equals(C.print_checked_as_zip))
				return printAllAsZip(fi, txnIds, print_label, use_stamp, use_stamp_for_bank, checkbox_selection);
			else if (action.equals(C.print_checked_as_image))
				return printAllAsImage(fi, txnIds, print_label, use_stamp, use_stamp_for_bank, checkbox_selection);
			else
				return printAllAsPdf(fi, txnIds, print_label, use_stamp, use_stamp_for_bank, checkbox_selection);
		} else if (action.equals(C.updateRecipient)) {
			long txn_id = Helper.getLong(params, C.txn_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			
			RecipientModel recipientModel = new RecipientModel();
			recipientModel.member_id = Helper.getLong(params, C.member_id, true);
			recipientModel.recipient_id = Helper.getInt(params, C.recipient_id, true);
			recipientModel.recipient_name = Helper.getString(params, C.recipient_name, true).toUpperCase();
			recipientModel.bank_acc = Helper.getString(params, C.bank_acc, true);
			recipientModel.bank_code = Helper.getString(params, C.bank_code, true);
			recipientModel.lm_time = Helper.getTimestamp(params, C.recipient_lm_time, true);
			recipientModel.is_verified = Helper.getInt(params, C.is_verified, true) == 1;

			try {
				UpdateResult r = updateRecipient(fi, txn_id, lm_time, recipientModel);
				conn.commit();
				
				StringBuilder sb = new StringBuilder();
				sb.append(Stringify.getTimestamp(r.lm_time)).append(C.char_31)
				.append(r.lm_user).append(C.char_31)
				.append(recipientModel.toString());
				return sb.toString();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.update_comment)) {
			long txn_id = Helper.getLong(params, C.txn_id, true);
			String comment = Helper.getString(params, C.comment, true);
			PreparedStatement pstmt = conn.prepareStatement("update money_transfer set comment = ? where txn_id = ?");
			PreparedStatement pstmtGetComment = conn.prepareStatement("select comment from money_transfer where txn_id = ?");
			try {
				pstmt.setString(1, comment);
				pstmt.setLong(2, txn_id);
				pstmt.executeUpdate();
				conn.commit();
				
				pstmtGetComment.setLong(1, txn_id);
				ResultSet r = pstmtGetComment.executeQuery();
				try {
					if (r.next()) {
						return r.getString(1);
					} else {
						throw new Exception(String.format(C.data_not_exist, "txn_id = " + txn_id));
					}
				} finally {
					r.close();
				}
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				pstmt.close();
			}
		} else if (action.equals(C.recipient_history)) {
			long txn_id = Helper.getLong(params, C.txn_id, true);
			StringBuilder sb = new StringBuilder();
			PreparedStatement pstmt = conn.prepareStatement("select lm_time, lm_user, old_recipient_name, (select bank_name from bank_code_list where bank_code = old_bank_code) as old_bank_name, old_bank_acc, new_recipient_name, (select bank_name from bank_code_list where bank_code = new_bank_code) as new_bank_name, new_bank_acc from money_transfer_recipient where txn_id = ? order by lm_time desc");
			try {
				pstmt.setLong(1, txn_id);
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
					while (r.next()) {
						sb.append(r.getTimestamp(1)).append(C.char_30)
						.append(r.getString(2)).append(C.char_30)
						.append(r.getString(3)).append(C.char_30)
						.append(r.getString(4)).append(C.char_30)
						.append(r.getString(5)).append(C.char_30)
						.append(r.getString(6)).append(C.char_30)
						.append(r.getString(7)).append(C.char_30)
						.append(r.getString(8)).append(C.char_31);
					}
					if (sb.length() > 0) {
						sb.delete(sb.length() - 1, sb.length());
					}
				}
			} finally {
				pstmt.close();
			}
			return sb.toString();
		} else if (action.equals(C.member_get_data_for_update)) {
			long memberId = Helper.getLong(params, C.member_id, true);
			
			StringBuilder sb = new StringBuilder();
			
			String kursValue = fi.getConnection().getGlobalConfig(C.member, C.kurs_value);
			String kursLmTime = fi.getConnection().getGlobalConfig(C.member, C.kurs_lm_time);
			String serviceCharge = fi.getConnection().getGlobalConfig(C.money_transfer, C.service_charge);
			sb.append(kursValue).append(C.char_31).append(kursLmTime).append(C.char_31).append(serviceCharge);
			
			sb.append(C.char_31).append(new MemberConfiguration().getDataForUpdate(fi, memberId));
			
			return sb.toString();
		} else {
			throw new Exception(String.format(C.unknown_action, action));
		}
	}
	
	public static String getTransferStatusList(FunctionItem fi) throws Exception {
		Statement stmt = fi.getConnection().getConnection().createStatement();
		try {
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(stmt.executeQuery("select transfer_status_id, transfer_status_name from transfer_status order by transfer_status_id"))) {
				List<String> list = new ArrayList<>();
				while (r.next()) {
					list.add(r.getInt(1));
					list.add(r.getString(2));
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append(list.size() / 2);
				for (String s : list)
					sb.append(C.char_31).append(s);
				return sb.toString();
			}
		} finally {
			stmt.close();
		}
	}
	
	public UpdateResult updateRecipient(FunctionItem fi, long txn_id, Timestamp lm_time, RecipientModel recipientModel) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select lm_time, recipient_id, lm_user from money_transfer where txn_id = ? for update");
		PreparedStatement pstmtRecipient = conn.prepareStatement("select recipient_name, bank_code, bank_acc from member_recipient where member_id = ? and recipient_id = ?");
		try {
			Timestamp old_lm_time;
			int old_recipient_id;
			String lm_user;
			pstmt.setLong(1, txn_id);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					old_lm_time = r.getTimestamp(1);
					old_recipient_id = r.getInt(2);
					lm_user = r.getString(3);
				} else
					throw new Exception(String.format(C.data_not_exist, "txn_id = " + txn_id));
			} finally {
				r.close();
			}
			
			if (old_recipient_id != recipientModel.recipient_id) {
				if (old_lm_time.getTime() != lm_time.getTime())
					throw new Exception(C.data_already_updated_by_another_user);
				
				lm_time = fi.getConnection().getCurrentTime();
				lm_user = fi.getSessionInfo().getUserName();
				
				PreparedStatement pstmtUpdate = conn.prepareStatement("update money_transfer set recipient_id = ?, lm_time = ?, lm_user = ? where txn_id = ?");
				try {
					pstmtUpdate.setInt(1, recipientModel.recipient_id);
					pstmtUpdate.setTimestamp(2, lm_time);
					pstmtUpdate.setString(3, lm_user);
					pstmtUpdate.setLong(4, txn_id);
					pstmtUpdate.executeUpdate();
				} finally {
					pstmtUpdate.close();
				}
			}
			
			String old_recipient_name, old_bank_code, old_bank_acc;
			pstmtRecipient.setLong(1, recipientModel.member_id);
			pstmtRecipient.setInt(2, old_recipient_id);
			r = pstmtRecipient.executeQuery();
			try {
				if (r.next()) {
					old_recipient_name = r.getString(1);
					old_bank_code = r.getString(2);
					old_bank_acc = r.getString(3);
				} else
					throw new Exception(String.format(C.data_not_exist, "member_id = " + recipientModel.member_id + ", recipient_id = " + old_recipient_id));
			} finally {
				r.close();
			}
			
			new MemberConfiguration().updateRecipient(fi, recipientModel);
			
			String new_recipient_name, new_bank_code, new_bank_acc;
			pstmtRecipient.setLong(1, recipientModel.member_id);
			pstmtRecipient.setInt(2, recipientModel.recipient_id);
			r = pstmtRecipient.executeQuery();
			try {
				if (r.next()) {
					new_recipient_name = r.getString(1);
					new_bank_code = r.getString(2);
					new_bank_acc = r.getString(3);
				} else
					throw new Exception(String.format(C.data_not_exist, "member_id = " + recipientModel.member_id + ", recipient_id = " + recipientModel.recipient_id));
			} finally {
				r.close();
			}
			
			try (PreparedStatement pstmtLog = conn.prepareStatement("insert into money_transfer_recipient (txn_id, lm_time, lm_user, old_recipient_name, old_bank_code, old_bank_acc, new_recipient_name, new_bank_code, new_bank_acc) values (?,?,?,?,?,?,?,?,?)")) {
				pstmtLog.setLong(1, txn_id);
				pstmtLog.setTimestamp(2, fi.getConnection().getCurrentTime());
				pstmtLog.setString(3, fi.getSessionInfo().getUserName());
				pstmtLog.setString(4, old_recipient_name);
				pstmtLog.setString(5, old_bank_code);
				pstmtLog.setString(6, old_bank_acc);
				pstmtLog.setString(7, new_recipient_name);
				pstmtLog.setString(8, new_bank_code);
				pstmtLog.setString(9, new_bank_acc);
				pstmtLog.executeUpdate();
			}
			
			UpdateResult result = new UpdateResult();
			result.lm_time = lm_time;
			result.lm_user = lm_user;
			return result;
		} finally {
			pstmt.close();
			pstmtRecipient.close();
		}
	}
	
	private String insert(FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		MoneyTransferModel moneyTransferModel = new MoneyTransferModel();
		moneyTransferModel.member_id = Helper.getLong(params, C.member_id, true);
		moneyTransferModel.recipient_id = Helper.getInt(params, C.recipient_id, false);
		moneyTransferModel.payment_id = PaymentType.get(Helper.getInt(params, C.payment_id, true));
		moneyTransferModel.payment_info = Helper.getString(params, C.payment_info, false);
		moneyTransferModel.transfer_amount_ntd = Helper.getInt(params, C.transfer_amount_ntd, true);
		moneyTransferModel.service_charge = Helper.getInt(params, C.service_charge, true);

		// check kurs_lm_time to make sure customer get the right price
		String kurs_lm_time = Helper.getString(params, "kurs_lm_time", true);
		String current_kurs_lm_time = fi.getConnection().getGlobalConfig(C.member, C.kurs_lm_time);
		if (!kurs_lm_time.equals(current_kurs_lm_time)) {
			throw new Exception("kurs value has already changed, please refresh again before making this transaction");
		}
		
		MemberConfiguration member = new MemberConfiguration();
		MemberModel memberModel = member.getData(fi, moneyTransferModel.member_id);
		
		if (memberModel.status == MemberStatus.banned)
			throw new Exception("member is banned");
		
		Calendar currentTime = Calendar.getInstance();
		Calendar arcExpire = Calendar.getInstance();
		arcExpire.setTime(memberModel.arc_expire_date);
		
		int compareResult = DateUtils.truncatedCompareTo(arcExpire, currentTime, Calendar.DATE);
		if (compareResult < 0) {
			throw new Exception("ARC already expired");
		}
		
		Connection conn = fi.getConnection().getConnection();
		try {
			if (moneyTransferModel.recipient_id == 0) {
				// new recipient
				RecipientModel recipientModel = new RecipientModel();
				recipientModel.member_id = moneyTransferModel.member_id;
				recipientModel.recipient_name = Helper.getString(params, C.recipient_name, true);
				recipientModel.bank_code = Helper.getString(params, C.bank_code, true);
				recipientModel.bank_acc = Helper.getString(params, C.bank_acc, true);

				new MemberConfiguration().insertRecipient(fi, recipientModel);
				moneyTransferModel.recipient_id = recipientModel.recipient_id;
			}
			
			insert(fi, moneyTransferModel);
			
			conn.commit();
			
			StringBuilder sb = new StringBuilder();
			sb.append(moneyTransferModel.total)
			.append(C.char_31).append(Stringify.getString(moneyTransferModel.payment_info))
			.append(C.char_31).append(Stringify.getString(moneyTransferModel.family_mart_error_message))
			.append(C.char_31).append(String.valueOf(moneyTransferModel.txn_id));
			return sb.toString();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	public void changeStatus(FunctionItem fi, long txnId, Timestamp lmTime, TransferStatus newTransferStatus, String comment, Timestamp currentTime, TransferMoneyThroughBank bank) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select lm_time, transfer_status_id, transfer_amount_idr, kurs_value, transfer_amount_ntd, transfer_through_bank_name, member_id, is_app, point_used, lm_time_paid, lm_time_pending, lm_time_process, lm_time_transfer, lm_time_failed, lm_time_cancel, recipient_id, service_charge from money_transfer where txn_id = ? for update");
		PreparedStatement pstmtUpdate = null;
		PreparedStatement pstmtLog = conn.prepareStatement("insert into money_transfer_status (txn_id, lm_time, lm_user, old_status_id, new_status_id, comment, bank_name) values (?,?,?,?,?,?,?)");
		try {
			Timestamp oldLmTime, lm_time_paid, lm_time_pending, lm_time_process, lm_time_transfer, lm_time_failed, lm_time_cancel;
			TransferStatus oldTransferStatus;
			long transferAmountIDR;
			double kursValue;
			int transferAmountNTD;
			TransferMoneyThroughBank old_transfer_through_bank_name;
			long member_id;
			boolean is_app;
			int point_used, recipient_id, service_charge;
			
			pstmt.setLong(1, txnId);
			try (ResultSet r = pstmt.executeQuery()) {
				if (r.next()) {
					oldLmTime = r.getTimestamp(1);
					oldTransferStatus = TransferStatus.get(r.getInt(2));
					transferAmountIDR = r.getLong(3);
					kursValue = r.getDouble(4);
					transferAmountNTD = r.getInt(5);
					String s = r.getString(6);
					if (s == null)
						old_transfer_through_bank_name = null;
					else
						old_transfer_through_bank_name = TransferMoneyThroughBank.valueOf(s);
					member_id = r.getLong(7);
					is_app = r.getInt(8) == 1;
					point_used = r.getInt(9);
					lm_time_paid = r.getTimestamp(10);
					lm_time_pending = r.getTimestamp(11);
					lm_time_process = r.getTimestamp(12);
					lm_time_transfer = r.getTimestamp(13);
					lm_time_failed = r.getTimestamp(14);
					lm_time_cancel = r.getTimestamp(15);
					recipient_id = r.getInt(16);
					service_charge = r.getInt(17);
				} else {
					throw new Exception(String.format(C.data_not_exist, "txn_id = " + txnId));
				}
			}
			
			if (oldLmTime.getTime() != lmTime.getTime())
				throw new Exception(String.format(C.data_already_updated_by_another_user, "txn_id = " + txnId));
			if (oldTransferStatus == newTransferStatus)
				throw new Exception(C.nothing_to_update);
			
			int app_create_invoice_threshold = Integer.parseInt(fi.getConnection().getGlobalConfig(C.remit_point, C.app_create_invoice_threshold));
			
			if (oldTransferStatus == TransferStatus.transfered) {
				int fee;
				try (PreparedStatement pstmtBankCode = conn.prepareStatement("select bank_code from member_recipient where member_id = ? and recipient_id = ?")) {
					pstmtBankCode.setLong(1, member_id);
					pstmtBankCode.setInt(2, recipient_id);
					String bank_code;
					try (ResultSet r = pstmtBankCode.executeQuery()) {
						if (r.next()) {
							bank_code = r.getString(1);
						} else
							throw new Exception("recipient data not found for member_id [" + member_id + "] and recipient_id = [" + recipient_id + "]");
					}
					
					switch (old_transfer_through_bank_name) {
						case BRI:
							fee = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, bank_code.equals("002") ? C.bri_fee_idr : C.bri_fee_to_non_bri));
							break;
						case BNI:
							fee = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, bank_code.equals("009") ? C.bni_fee_idr : C.bni_fee_to_non_bni));
							break;
						default:
							fee = 0;
							break;
					}
				}
				
				// add back the money into idr_bookkeeping
				IdrBookkeeping idrBook = new IdrBookkeeping();
				idrBook.add(fi, transferAmountIDR, "status [" + oldTransferStatus.name() + "] changed to [" + newTransferStatus.name() + (comment == null ? "]" : "] with reason:\n" + comment), txnId, kursValue, transferAmountNTD);
				if (fee > 0) {
					idrBook.add(fi, fee, old_transfer_through_bank_name.name() + " fee returned", txnId, null, null);
				}
				
				// only when service_charge is >= app_create_invoice_threshold, then customer will got point from create_invoice when status is transfered
				// thus we should reclaim the point back when the status is changed from transfered
				if (is_app && service_charge >= app_create_invoice_threshold) {
					PreparedStatement pstmtSearchPoint = conn.prepareStatement("select remit_point, lm_time from member_point where member_id = ? and txn_id = ? and reason_id = 3");
					try {
						Integer remit_point = null;
						Timestamp remit_lm_time = null;
						pstmtSearchPoint.setLong(1, member_id);
						pstmtSearchPoint.setLong(2, txnId);
						try (ResultSet r = pstmtSearchPoint.executeQuery()) {
							if (r.next()) {
								int i = r.getInt(1);
								if (!r.wasNull())
									remit_point = i;
								remit_lm_time = r.getTimestamp(2);
							}
						}
						
						if (remit_point != null) {
							PreparedStatement pstmtLockMember = conn.prepareStatement("select remit_point from member where member_id = ? for update");
							PreparedStatement pstmtSubstractPoint = conn.prepareStatement("update member set remit_point = remit_point - ? where member_id = ?");
							PreparedStatement pstmtDeletePointHistory = conn.prepareStatement("delete from member_point where member_id = ? and lm_time = ?");
							try {
								pstmtLockMember.setLong(1, member_id);
								try (ResultSet r = pstmtLockMember.executeQuery()) {
									if (r.next()) {
										r.getInt(1);
									} else
										throw new Exception("member_id [" + member_id + "] not exist");
								}
								
								pstmtSubstractPoint.setInt(1, remit_point.intValue());
								pstmtSubstractPoint.setLong(2, member_id);
								pstmtSubstractPoint.executeUpdate();
								
								pstmtDeletePointHistory.setLong(1, member_id);
								pstmtDeletePointHistory.setTimestamp(2, remit_lm_time);
								pstmtDeletePointHistory.executeUpdate();
							} finally {
								pstmtSubstractPoint.close();
								pstmtDeletePointHistory.close();
								pstmtLockMember.close();
							}
						}
					} finally {
						pstmtSearchPoint.close();
					}
				}
			} else if ((oldTransferStatus == TransferStatus.cancel || oldTransferStatus == TransferStatus.failed) && newTransferStatus != TransferStatus.cancel && newTransferStatus != TransferStatus.failed) {
				if (point_used > 0) {
					PreparedStatement pstmtLockMember = conn.prepareStatement("select remit_point from member where member_id = ? for update");
					PreparedStatement pstmtSubstractPoint = conn.prepareStatement("update member set remit_point = remit_point - ? where member_id = ?");
					PreparedStatement pstmtAddPointHist = conn.prepareStatement("insert into member_point (member_id, lm_time, remit_point, txn_id, reason_id) values (?,?,?,?,4)");
					try {
						int remit_point;
						pstmtLockMember.setLong(1, member_id);
						try (ResultSet r = pstmtLockMember.executeQuery()) {
							if (r.next()) {
								remit_point = r.getInt(1);
							} else
								throw new Exception("member_id [" + member_id + "] not exist");
						}
						
						if (remit_point >= point_used) {
							pstmtSubstractPoint.setInt(1, point_used);
							pstmtSubstractPoint.setLong(2, member_id);
							pstmtSubstractPoint.executeUpdate();
							
							pstmtAddPointHist.setLong(1, member_id);
							pstmtAddPointHist.setTimestamp(2, currentTime);
							pstmtAddPointHist.setInt(3, point_used);
							pstmtAddPointHist.setLong(4, txnId);
							pstmtAddPointHist.executeUpdate();
						} else
							throw new Exception("member has not enough point");
					} finally {
						pstmtLockMember.close();
						pstmtSubstractPoint.close();
						pstmtAddPointHist.close();
					}
				}
			}
			
			if (newTransferStatus == TransferStatus.transfered) {
				if (bank == null)
					throw new Exception("cannot change status to transfered without providing transfer through bank name");
				
				int fee;
				String bank_code, bank_acc;
				try (PreparedStatement pstmtBankCode = conn.prepareStatement("select bank_code, bank_acc from member_recipient where member_id = ? and recipient_id = ?")) {
					pstmtBankCode.setLong(1, member_id);
					pstmtBankCode.setInt(2, recipient_id);
					try (ResultSet r = pstmtBankCode.executeQuery()) {
						if (r.next()) {
							bank_code = r.getString(1);
							bank_acc = r.getString(2);
						} else
							throw new Exception("recipient data not found for member_id [" + member_id + "] and recipient_id = [" + recipient_id + "]");
					}
				}
				
				// restriction:
				// can only transfer in allowed time
				String allow_console_from_time = fi.getConnection().getGlobalConfig(C.money_transfer, C.allow_console_from_time);
				String allow_console_to_time = fi.getConnection().getGlobalConfig(C.money_transfer, C.allow_console_to_time);
				if (allow_console_from_time.length() > 0 && allow_console_to_time.length() > 0) {
					SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
					Date allowFrom = timeFormat.parse(allow_console_from_time);
					Date allowTo = timeFormat.parse(allow_console_to_time);
					Date currentTimeOnly = timeFormat.parse(timeFormat.format(currentTime));
					if (!(currentTimeOnly.before(allowTo) && currentTimeOnly.after(allowFrom))) {
						throw new Exception("already pass allowed time " + allow_console_from_time + " - " + allow_console_to_time);
					}
				}
				
				// can only transfer to same account max 10 times each day.
				// 0 as disable
				int max_transfer_count = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, C.recipient_transfer_count));
				if (max_transfer_count > 0) {
					try (PreparedStatement pstmtRecipientTransfer = conn.prepareStatement("select transfer_count from recipient_transfer_count where lm_time = ? and bank_code = ? and bank_acc = ? for update")) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(currentTime);
						cal.set(Calendar.HOUR_OF_DAY, 0);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);
						Timestamp currentDate = new Timestamp(cal.getTime().getTime());
						pstmtRecipientTransfer.setTimestamp(1, currentDate);
						pstmtRecipientTransfer.setString(2, bank_code);
						pstmtRecipientTransfer.setString(3, bank_acc);
						Integer transfer_count = null;
						try (ResultSet r = pstmtRecipientTransfer.executeQuery()) {
							if (r.next()) {
								transfer_count = r.getInt(1);
							}
						}
						
						if (transfer_count == null) {
							try (PreparedStatement pstmtRecipientTransferInsert = conn.prepareStatement("insert into recipient_transfer_count (lm_time, bank_code, bank_acc, transfer_count) values (?,?,?,1)")) {
								pstmtRecipientTransferInsert.setTimestamp(1, currentDate);
								pstmtRecipientTransferInsert.setString(2, bank_code);
								pstmtRecipientTransferInsert.setString(3, bank_acc);
								pstmtRecipientTransferInsert.executeUpdate();
							}
						} else {
							if (transfer_count >= max_transfer_count) {
								throw new Exception("over daily maximum transfer count for bank code [" + bank_code + "] and bank account [" + bank_acc + "]");
							}
							
							transfer_count++;
							try (PreparedStatement pstmtRecipientTransferUpdate = conn.prepareStatement("update recipient_transfer_count set transfer_count = ? where lm_time = ? and bank_code = ? and bank_acc = ?")) {
								pstmtRecipientTransferUpdate.setInt(1, transfer_count.intValue());
								pstmtRecipientTransferUpdate.setTimestamp(2, currentDate);
								pstmtRecipientTransferUpdate.setString(3, bank_code);
								pstmtRecipientTransferUpdate.setString(4, bank_acc);
								pstmtRecipientTransferUpdate.executeUpdate();
							}
						}
					}
				}
				
				switch (bank) {
					case BRI:
						fee = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, bank_code.equals("002") ? C.bri_fee_idr : C.bri_fee_to_non_bri));
						break;
					case BNI:
						fee = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, bank_code.equals("009") ? C.bni_fee_idr : C.bni_fee_to_non_bni));
						break;
					default:
						fee = 0;
						break;
				}
				
				IdrBookkeeping idrBook = new IdrBookkeeping();
				idrBook.add(fi, -transferAmountIDR, newTransferStatus.name() + (comment == null ? "" : ":\n" + comment), txnId, kursValue, transferAmountNTD);
				if (fee > 0) {
					idrBook.add(fi, -fee, bank.name() + " transfer fee", txnId, null, null);
				}
				
				// this money transfer is created through app
				// add remit point if not free service charge
				if (is_app) {
					if (point_used == 0) {
						PreparedStatement pstmtLockMember = conn.prepareStatement("select app_remit_count from member where member_id = ? for update");
						PreparedStatement pstmtAddPoint = conn.prepareStatement("update member set remit_point = remit_point + ?, app_remit_count = ? where member_id = ?");
						PreparedStatement pstmtAddPointHist = conn.prepareStatement("insert into member_point (member_id, lm_time, remit_point, txn_id, reason_id, reason_desc) values (?,?,?,?,?,?)");
						PreparedStatement pstmtSchedule = conn.prepareStatement("select date_of_month, month_id, year_id, remit_point, schedule_desc from member_point_schedule where date_of_month = ? or month_id = ? or year_id = ?");
						PreparedStatement pstmtMaxLmTime = conn.prepareStatement("select max(lm_time) from member_point where member_id = ?");
						try {
							int app_create_invoice_bonus = Integer.parseInt(fi.getConnection().getGlobalConfig(C.remit_point, C.app_create_invoice_bonus));
							int app_remit_100th_bonus = Integer.parseInt(fi.getConnection().getGlobalConfig(C.remit_point, C.app_remit_100th_bonus));
							int app_remit_100th_threshold = Integer.parseInt(fi.getConnection().getGlobalConfig(C.remit_point, C.app_remit_100th_threshold));
							
							int app_remit_count;
							pstmtLockMember.setLong(1, member_id);
							try (ResultSet r = pstmtLockMember.executeQuery()) {
								if (r.next()) {
									app_remit_count = r.getInt(1);
								} else
									throw new Exception("member_id [" + member_id + "] not exist");
							}
							
							Timestamp member_point_lm_time = currentTime;
							pstmtMaxLmTime.setLong(1, member_id);
							try (ResultSet r = pstmtMaxLmTime.executeQuery()) {
								r.next();
								Timestamp max_lm_time = r.getTimestamp(1);
								if (max_lm_time != null && member_point_lm_time.getTime() <= max_lm_time.getTime()) {
									member_point_lm_time = new Timestamp(max_lm_time.getTime() + 1000);
								}
							}
							
							app_remit_count++;

							int timestamp_offset = 0;
							
							if (service_charge >= app_create_invoice_threshold) {
								pstmtAddPoint.setInt(1, app_create_invoice_bonus);
								pstmtAddPoint.setInt(2, app_remit_count);
								pstmtAddPoint.setLong(3, member_id);
								pstmtAddPoint.executeUpdate();
								
								pstmtAddPointHist.setLong(1, member_id);
								pstmtAddPointHist.setTimestamp(2, new Timestamp(member_point_lm_time.getTime() + timestamp_offset));
								pstmtAddPointHist.setInt(3, app_create_invoice_bonus);
								pstmtAddPointHist.setLong(4, txnId);
								pstmtAddPointHist.setInt(5, 3);
								pstmtAddPointHist.setNull(6, Types.VARCHAR);
								pstmtAddPointHist.executeUpdate();
								
								timestamp_offset += 1000;
							}
							
							if (app_remit_count >= app_remit_100th_threshold) {
								app_remit_count = app_remit_count - app_remit_100th_threshold;
								
								pstmtAddPoint.setInt(1, app_remit_100th_bonus);
								pstmtAddPoint.setInt(2, app_remit_count);
								pstmtAddPoint.setLong(3, member_id);
								pstmtAddPoint.executeUpdate();
								
								pstmtAddPointHist.setLong(1, member_id);
								pstmtAddPointHist.setTimestamp(2, new Timestamp(member_point_lm_time.getTime() + timestamp_offset));
								pstmtAddPointHist.setInt(3, app_remit_100th_bonus);
								pstmtAddPointHist.setNull(4, Types.NUMERIC);
								pstmtAddPointHist.setInt(5, 6);
								pstmtAddPointHist.setNull(6, Types.VARCHAR);
								pstmtAddPointHist.executeUpdate();
								
								timestamp_offset += 1000;
							}
							
							if (lm_time_paid != null) {
								Calendar calendar = Calendar.getInstance();
								calendar.setTime(lm_time_paid);
								int current_date = calendar.get(Calendar.DAY_OF_MONTH);
								int current_month = calendar.get(Calendar.MONTH) + 1;
								int current_year = calendar.get(Calendar.YEAR);
								pstmtSchedule.setInt(1, current_date);
								pstmtSchedule.setInt(2, current_month);
								pstmtSchedule.setInt(3, current_year);
								try (ResultSet r = pstmtSchedule.executeQuery()) {
									while (r.next()) {
										int date_of_month = r.getInt(1);
										int month_id = r.getInt(2);
										int year_id = r.getInt(3);
										int remit_point = r.getInt(4);
										String schedule_desc = r.getString(5);
										
										if (year_id > 0 && year_id != current_year)
											continue;
										if (month_id > 0 && month_id != current_month)
											continue;
										if (date_of_month > 0 && date_of_month != current_date)
											continue;
										if (remit_point == 0)
											continue;
										
										StringBuilder sb = new StringBuilder();
										sb.append(schedule_desc)
										.append(C.comma).append(C.date_of_month).append(C.operationEqual).append(date_of_month == 0 ? C.emptyString : date_of_month)
										.append(C.comma).append(C.month_id).append(C.operationEqual).append(month_id == 0 ? C.emptyString : month_id)
										.append(C.comma).append(C.year_id).append(C.operationEqual).append(year_id == 0 ? C.emptyString : year_id);
										
										pstmtAddPoint.setInt(1, remit_point);
										pstmtAddPoint.setInt(2, app_remit_count);
										pstmtAddPoint.setLong(3, member_id);
										pstmtAddPoint.executeUpdate();
										
										pstmtAddPointHist.setLong(1, member_id);
										pstmtAddPointHist.setTimestamp(2, new Timestamp(member_point_lm_time.getTime() + timestamp_offset));
										pstmtAddPointHist.setInt(3, remit_point);
										pstmtAddPointHist.setNull(4, Types.NUMERIC);
										pstmtAddPointHist.setInt(5, 9);
										pstmtAddPointHist.setString(6, sb.toString());
										pstmtAddPointHist.executeUpdate();
										
										timestamp_offset += 1000;
									}
								}
							}
						} finally {
							pstmtLockMember.close();
							pstmtAddPoint.close();
							pstmtAddPointHist.close();
							pstmtSchedule.close();
							pstmtMaxLmTime.close();
						}
					}
				}
			} else if ((newTransferStatus == TransferStatus.cancel || newTransferStatus == TransferStatus.failed) && oldTransferStatus != TransferStatus.cancel && oldTransferStatus != TransferStatus.failed) {
				int total_amount_ntd;
				try (PreparedStatement pstmtM = conn.prepareStatement("select transfer_amount_ntd from member where member_id = ? for update")) {
					pstmtM.setLong(1, member_id);
					try (ResultSet rM = pstmtM.executeQuery()) {
						rM.next();
						total_amount_ntd = rM.getInt(1);
					}
				}
				total_amount_ntd = total_amount_ntd - transferAmountNTD;
				if (total_amount_ntd < 0) {
					total_amount_ntd = 0;
				}
				try (PreparedStatement pstmtM = conn.prepareStatement("update member set transfer_amount_ntd = ? where member_id = ?")) {
					pstmtM.setLong(1, total_amount_ntd);
					pstmtM.setLong(2, member_id);
					pstmtM.executeUpdate();
				}
				
				if (point_used > 0) {
					Timestamp remit_lm_time = null;
					try (PreparedStatement pstmtSearch = conn.prepareStatement("select lm_time from member_point where member_id = ? and txn_id = ? and reason_id = 4 order by lm_time desc")) {
						pstmtSearch.setLong(1, member_id);
						pstmtSearch.setLong(2, txnId);
						try (ResultSet r = pstmtSearch.executeQuery()) {
							if (r.next()) {
								remit_lm_time = r.getTimestamp(1);
							}
						}
					}
					
					if (remit_lm_time != null) {
						try (PreparedStatement pstmtAddPoint = conn.prepareStatement("update member set remit_point = remit_point + ? where member_id = ?")) {
							pstmtAddPoint.setInt(1, point_used);
							pstmtAddPoint.setLong(2, member_id);
							pstmtAddPoint.executeUpdate();
						}
						
						try (PreparedStatement pstmtDeletePointHistory = conn.prepareStatement("delete from member_point where member_id = ? and lm_time = ?")) {
							pstmtDeletePointHistory.setLong(1, member_id);
							pstmtDeletePointHistory.setTimestamp(2, remit_lm_time);
							pstmtDeletePointHistory.executeUpdate();
						}
					}
				}
				
				int total_create_count = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer_ibon, C.total_create_count, true));
				total_create_count = total_create_count - 1;
				if (total_create_count < 0) {
					total_create_count = 0;
				}
				fi.getConnection().setGlobalConfig(C.money_transfer_ibon, C.total_create_count, String.valueOf(total_create_count), true);
			}
			
			String lmUser = fi.getSessionInfo().getUserName();
			
			String lmTimeStatusColumnName;
			Timestamp lmTimeStatusColumnValue;
			switch (newTransferStatus) {
				case pending:
					lmTimeStatusColumnName = C.lm_time_pending;
					lmTimeStatusColumnValue = lm_time_pending == null ? currentTime : lm_time_pending;
					break;
				case paid:
					lmTimeStatusColumnName = C.lm_time_paid;
					lmTimeStatusColumnValue = lm_time_paid == null ? currentTime : lm_time_paid;
					break;
				case process:
					lmTimeStatusColumnName = C.lm_time_process;
					lmTimeStatusColumnValue = lm_time_process == null ? currentTime : lm_time_process;
					break;
				case transfered:
					lmTimeStatusColumnName = C.lm_time_transfer;
					lmTimeStatusColumnValue = lm_time_transfer == null ? currentTime : lm_time_transfer;
					break;
				case cancel:
					lmTimeStatusColumnName = C.lm_time_cancel;
					lmTimeStatusColumnValue = lm_time_cancel == null ? currentTime : lm_time_cancel;
					break;
				case failed:
					lmTimeStatusColumnName = C.lm_time_failed;
					lmTimeStatusColumnValue = lm_time_failed == null ? currentTime : lm_time_failed;
					break;
				default:
					throw new Exception("transfer status not implemented");
			}
			
			StringBuilder sql = new StringBuilder();
			sql.append("update money_transfer set transfer_status_id = ?, lm_time = ?, lm_user = ?, transfer_through_bank_name = ?, ")
			.append(lmTimeStatusColumnName).append(" = ? where txn_id = ?");
			
			pstmtUpdate = conn.prepareStatement(sql.toString());
			
			pstmtUpdate.setInt(1, newTransferStatus.getId());
			pstmtUpdate.setTimestamp(2, currentTime);
			pstmtUpdate.setString(3, lmUser);
			if (bank == null)
				pstmtUpdate.setNull(4, Types.VARCHAR);
			else
				pstmtUpdate.setString(4, bank.name());
			pstmtUpdate.setTimestamp(5, lmTimeStatusColumnValue);
			pstmtUpdate.setLong(6, txnId);
			pstmtUpdate.executeUpdate();
			
			pstmtLog.setLong(1, txnId);
			pstmtLog.setTimestamp(2, currentTime);
			pstmtLog.setString(3, lmUser);
			pstmtLog.setInt(4, oldTransferStatus.getId());
			pstmtLog.setInt(5, newTransferStatus.getId());
			pstmtLog.setString(6, comment);
			if (bank == null)
				pstmtLog.setNull(7, Types.VARCHAR);
			else
				pstmtLog.setString(7, bank.name());
			pstmtLog.executeUpdate();
		} finally {
			pstmt.close();
			if (pstmtUpdate != null)
				pstmtUpdate.close();
			pstmtLog.close();
		}
	}
	
	public void insert(FunctionItem fi, MoneyTransferModel m) throws Exception {
		String enable_create_invoice_console = fi.getConnection().getGlobalConfig(C.money_transfer, C.enable_create_invoice_console);
		if (!enable_create_invoice_console.equals("1")) {
			throw new Exception("create invoice disabled");
		}
		
		Connection conn = fi.getConnection().getConnection();
		
		int total_amount_ntd;
		try (PreparedStatement pstmtMember = conn.prepareStatement("select transfer_amount_ntd from member where member_id = ? for update")) {
			pstmtMember.setLong(1, m.member_id);
			try (ResultSet r = pstmtMember.executeQuery()) {
				r.next();
				total_amount_ntd = r.getInt(1);
			}
		}
		
		PreparedStatement pstmt = null;
		PreparedStatement pstmtStatus = conn.prepareStatement("insert into money_transfer_status (txn_id, lm_time, lm_user, new_status_id) values (?,?,?,?)");
		try {
			m.lm_time = fi.getConnection().getCurrentTime();
			m.lm_user = fi.getSessionInfo().getUserName();
			m.transfer_status_id = TransferStatus.pending;
			m.kurs_value = Double.parseDouble(fi.getConnection().getGlobalConfig(C.member, C.kurs_value));
			m.transfer_amount_idr = (int)(m.kurs_value * m.transfer_amount_ntd);
			m.total = m.service_charge + m.transfer_amount_ntd;
			
			if (m.payment_id == PaymentType.mini_mart) {
				int mini_mart_max_amount = Integer.parseInt(fi.getConnection().getGlobalConfig(C.money_transfer, C.mini_mart_max_amount));
				if (m.total > mini_mart_max_amount)
					throw new Exception("total amount exceed " + Stringify.getCurrency(mini_mart_max_amount));
			}
			
			total_amount_ntd = total_amount_ntd + m.transfer_amount_ntd;
			long quota = Long.parseLong(fi.getConnection().getGlobalConfig(C.money_transfer, C.total_transfer_amount_ntd));
			if (quota > 0 && total_amount_ntd > quota) {
				throw new Exception("exceed transfer rupiah quota");
			}
			try (PreparedStatement pstmtUpd = conn.prepareStatement("update member set transfer_amount_ntd = ? where member_id = ?")) {
				pstmtUpd.setLong(1, total_amount_ntd);
				pstmtUpd.setLong(2, m.member_id);
				pstmtUpd.executeUpdate();
			}
			
			Calendar c = Calendar.getInstance();
			c.setTime(m.lm_time);
			StringBuilder sb = new StringBuilder(15);
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;
			int date = c.get(Calendar.DAY_OF_MONTH);
			sb.append(year).append(StringUtils.leftPad(String.valueOf(month), 2, '0')).append(StringUtils.leftPad(String.valueOf(date), 2, '0'));
			long seq = fi.getConnection().getSeq("txn_id_" + sb.toString(), true);
			sb.append(StringUtils.leftPad(String.valueOf(seq), 6, '0'));
			
			m.txn_id = Long.parseLong(sb.toString());
			
			switch (m.payment_id) {
				case family_mart:
					throw new Exception("obsolete payment type [family_mart]");
				case mini_mart:
					m.payment_info = generateSevenElevenPaymentInfo();
					try {
						createOrderFamilyMart(fi, m);
					} catch (Exception e) {
						m.family_mart_error_message = e.getMessage();
					}
					break;
				case cash:
					m.payment_info = generateCashPaymentInfo();
					break;
				default:
					break;
			}
			
			String lmTimeStatusColumnName;
			switch (m.transfer_status_id) {
				case pending:
					lmTimeStatusColumnName = C.lm_time_pending;
					break;
				case paid:
					lmTimeStatusColumnName = C.lm_time_paid;
					break;
				case process:
					lmTimeStatusColumnName = C.lm_time_process;
					break;
				case transfered:
					lmTimeStatusColumnName = C.lm_time_transfer;
					break;
				case cancel:
					lmTimeStatusColumnName = C.lm_time_cancel;
					break;
				case failed:
					lmTimeStatusColumnName = C.lm_time_failed;
					break;
				default:
					throw new Exception("transfer status not implemented");
			}
			
			StringBuilder sql = new StringBuilder();
			sql.append("insert into money_transfer (txn_id, member_id, recipient_id, payment_id, payment_info, transfer_status_id, kurs_value, transfer_amount_ntd, transfer_amount_idr, service_charge, total, is_print, lm_time, lm_user, lm_time_expire,")
			.append(lmTimeStatusColumnName).append(") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,cast(date_add(?, interval (select cast(config_value as decimal(4,3)) * 24 * 60 * 60 from global_config where group_name = 'money_transfer_ibon' and config_name = 'expire_date_console') second) as datetime),?)");
			
			pstmt = conn.prepareStatement(sql.toString());
			
			pstmt.setLong(1, m.txn_id);
			pstmt.setLong(2, m.member_id);
			pstmt.setInt(3, m.recipient_id);
			pstmt.setInt(4, m.payment_id.getId());
			pstmt.setString(5, m.payment_info);
			pstmt.setInt(6, m.transfer_status_id.getId());
			pstmt.setDouble(7, m.kurs_value);
			pstmt.setInt(8, m.transfer_amount_ntd);
			pstmt.setLong(9, m.transfer_amount_idr);
			pstmt.setInt(10, m.service_charge);
			pstmt.setInt(11, m.total);
			pstmt.setInt(12, m.is_print ? 1 : 0);
			pstmt.setTimestamp(13, m.lm_time);
			pstmt.setString(14, m.lm_user);
			pstmt.setTimestamp(15, m.lm_time);
			pstmt.setTimestamp(16, m.lm_time);
			pstmt.executeUpdate();
			
			pstmtStatus.setLong(1, m.txn_id);
			pstmtStatus.setTimestamp(2, m.lm_time);
			pstmtStatus.setString(3, m.lm_user);
			pstmtStatus.setInt(4, m.transfer_status_id.getId());
			pstmtStatus.executeUpdate();
			
			fi.getConnection().logHistory(IndogoTable.money_transfer, m.txn_id, HistoryAction.add, null, m.lm_time, m.lm_user,
					new HistoryData(C.member_id, null, String.valueOf(m.member_id)),
					new HistoryData(C.recipient_id, null, String.valueOf(m.recipient_id)),
					new HistoryData(C.payment_id, null, String.valueOf(m.payment_id.getId())),
					new HistoryData(C.payment_info, null, m.payment_info),
					new HistoryData(C.transfer_status_id, null, String.valueOf(m.transfer_status_id.getId())),
					new HistoryData(C.kurs_value, null, String.valueOf(m.kurs_value)),
					new HistoryData(C.transfer_amount_ntd, null, String.valueOf(m.transfer_amount_ntd)),
					new HistoryData(C.transfer_amount_idr, null, String.valueOf(m.transfer_amount_idr)),
					new HistoryData(C.service_charge, null, String.valueOf(m.service_charge)),
					new HistoryData(C.total, null, String.valueOf(m.total)),
					new HistoryData(C.is_print, null, String.valueOf(m.is_print)));
		} finally {
			if (pstmt != null)
				pstmt.close();
			pstmtStatus.close();
		}
	}
	
	public MoneyTransferModel getData(FunctionItem fi, long txn_id) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select txn_id, member_id, recipient_id, payment_id, payment_info, transfer_status_id, kurs_value, transfer_amount_ntd, transfer_amount_idr, service_charge, total, is_print, lm_time, lm_user from money_transfer where txn_id = ?");
		try {
			pstmt.setLong(1, txn_id);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					MoneyTransferModel m = new MoneyTransferModel();
					m.txn_id = r.getLong(1);
					m.member_id = r.getLong(2);
					m.recipient_id = r.getInt(3);
					m.payment_id = PaymentType.get(r.getInt(4));
					m.payment_info = r.getString(5);
					m.transfer_status_id = TransferStatus.get(r.getInt(6));
					m.kurs_value = r.getDouble(7);
					m.transfer_amount_ntd = r.getInt(8);
					m.transfer_amount_idr = r.getLong(9);
					m.service_charge = r.getInt(10);
					m.total = r.getInt(11);
					m.is_print = r.getInt(12) == 1;
					m.lm_time = r.getTimestamp(13);
					m.lm_user = r.getString(14);
					return m;
				} else {
					return null;
				}
			} finally {
				r.close();
			}
		} finally {
			pstmt.close();
		}
	}
	
	public void createOrderFamilyMart(FunctionItem fi, MoneyTransferModel m) throws Exception {
		RecipientModel recipient = new MemberConfiguration().getRecipient(fi, m.member_id, m.recipient_id);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
		IConnection ic = fi.getConnection();
		Hashtable<String, String> conf = ic.getGlobalConfig(C.money_transfer_family);
		
		int httpConnectionTimeout = Integer.parseInt(conf.get(C.http_connection_timeout));
		int httpSoTimeout = Integer.parseInt(conf.get(C.http_so_timeout));
		int httpRequestTimeout = Integer.parseInt(conf.get(C.http_request_timeout));
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(httpSoTimeout)
				.setConnectTimeout(httpConnectionTimeout)
				.setConnectionRequestTimeout(httpRequestTimeout)
				.build();
		
		SocketConfig socketConfig = SocketConfig.custom()
				.setSoTimeout(httpSoTimeout)
				.build();
		
		double interval = Double.parseDouble(fi.getConnection().getGlobalConfig(C.money_transfer_ibon, C.expire_date_console));
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(m.lm_time);
		calendar.add(Calendar.SECOND, (int) (interval * 24 * 60 * 60));
		Date expireDate = calendar.getTime();
		
		String url = conf.get(C.family_url);
		String m_taxID = conf.get(C.family_cuid); //您的統編
		String m_termino = conf.get(C.family_termino); //廠商代碼+代收代號(共7碼)
		String m_date = dateFormat.format(m.lm_time);        //日期 YYYYMMDD
		String m_time = timeFormat.format(m.lm_time);      //時間 HHMMSS
		String m_orderNo = String.valueOf(m.txn_id);     //您的訂單編號
		String m_amount = String.valueOf(m.total);      //金額
		String m_pinCode = m.payment_info == null ? "" : m.payment_info;    //PIN Code
		String m_endDate = dateFormat.format(expireDate);     //繳款截止日期 YYYYMMDD
		String m_endTime = timeFormat.format(expireDate);     //繳款截止時間 HHMMSS
		String m_payType = conf.get(C.family_paytype);     //繳款類別
		String m_prdDesc = conf.get(C.family_prddesc);    //商品簡述
		String m_payCompany = conf.get(C.family_paycompany); //付款廠商
		String m_tradeType = "1";     //1:要號, 3:二次列印
		String m_desc1 = recipient.recipient_name;      //備註1
		String m_desc2 = "Bank Code: " + recipient.bank_code;      //備註2
		String m_desc3 = "Bank Name: " + recipient.bank_name;      //備註3
		String m_desc4 = "Acc: " + recipient.bank_acc;      //備註4
		String m_accountNo = conf.get(C.family_system_id);  //您的帳號
		String m_password = conf.get(C.family_system_pwd);   //您的密碼
		
		String familyMartApiName;
		String familyMartXmlVer;
		if (m_pinCode.length() > 0) {
			familyMartApiName = "NewOrder_PINCODE";
			familyMartXmlVer = "05.11";
		} else {
			familyMartApiName = "NewOrder";
			familyMartXmlVer = "05.01";
		}
		
		StringBuilder sb = new StringBuilder(100);
		sb.append("<?xml version='1.0' encoding='UTF-8'?>");
		sb.append("<soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>");
			sb.append("<soap:Body>");
				sb.append("<").append(familyMartApiName).append(" xmlns='http://tempuri.org/'>");
					sb.append("<TX_WEB>");
						sb.append("<HEADER>");
							sb.append("<XML_VER>").append(familyMartXmlVer).append("</XML_VER>");
							sb.append("<XML_FROM>").append(m_taxID).append("</XML_FROM>");
							sb.append("<TERMINO>").append(m_termino).append("</TERMINO>");
							sb.append("<XML_TO>").append(conf.get(C.family_xml_to)).append("</XML_TO>");
							sb.append("<BUSINESS>B000001</BUSINESS>");
							sb.append("<XML_DATE>").append(m_date).append("</XML_DATE>");
							sb.append("<XML_TIME>").append(m_time).append("</XML_TIME>");
							sb.append("<STATCODE>0000</STATCODE>");
							sb.append("<STATDESC></STATDESC>");
						sb.append("</HEADER>");
						sb.append("<AP>"); 
							sb.append("<ORDER_NO>").append(m_orderNo).append("</ORDER_NO>");
							sb.append("<ACCOUNT>").append(m_amount).append("</ACCOUNT>");
							sb.append("<PIN_CODE>").append(m_pinCode).append("</PIN_CODE>");
							sb.append("<END_DATE>").append(m_endDate).append("</END_DATE>");
							sb.append("<END_TIME>").append(m_endTime).append("</END_TIME>");
							sb.append("<PAY_TYPE>").append(m_payType).append("</PAY_TYPE>");
							sb.append("<PRD_DESC>").append(m_prdDesc).append("</PRD_DESC>");
							sb.append("<PAY_COMP>").append(m_payCompany).append("</PAY_COMP>");
							sb.append("<TRADE_TYPE>").append(m_tradeType).append("</TRADE_TYPE>");
							sb.append("<DESC1>").append(m_desc1).append("</DESC1>");
							sb.append("<DESC2>").append(m_desc2).append("</DESC2>");
							sb.append("<DESC3>").append(m_desc3).append("</DESC3>");
							sb.append("<DESC4>").append(m_desc4).append("</DESC4>");
							sb.append("<STATUS>S</STATUS>");
							sb.append("<DESC></DESC>");
						sb.append("</AP>");
					sb.append("</TX_WEB>");
					sb.append("<ACCOUNT_NO>").append(m_accountNo).append("</ACCOUNT_NO>");
					sb.append("<PASSWORD>").append(m_password).append("</PASSWORD>");
				sb.append("</").append(familyMartApiName).append(">");
			sb.append("</soap:Body>");
		sb.append("</soap:Envelope>");

		CloseableHttpClient httpClient = HttpClients.custom()
		        .setDefaultRequestConfig(requestConfig)
		        .setDefaultSocketConfig(socketConfig)
		        .build();
		try {
			StringEntity stringEntity = new StringEntity(sb.toString());
			
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Content-Type", "text/xml;charset=utf-8");
			httpPost.addHeader("SOAPAction", "http://tempuri.org/" + familyMartApiName);
			httpPost.setEntity(stringEntity);
			CloseableHttpResponse response = httpClient.execute(httpPost);
			try {
				HttpEntity entity = response.getEntity();
				try {
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						InputStream inputStream = entity.getContent();
						String reply = IOUtils.toString(inputStream, "UTF-8");
						
						int idxStatus = reply.indexOf("<STATUS>");
						if (idxStatus < 0) {
							LogInfo info = new LogInfo(fi.getSessionInfo().getUserRowId(), fi.getSessionInfo().getUserName(), fi.getSID(), "famiport_reply");
							info.setMessage("cannot find tag <STATUS> in response xml\n\n" + reply);
							info.setVerbose(1);
							ic.log(info);
							throw new Exception("cannot find tag <STATUS> in response xml");
						}
						
						String status = reply.substring(idxStatus + 8, idxStatus + 9);
						if (!status.equals("S")) {
							LogInfo info = new LogInfo(fi.getSessionInfo().getUserRowId(), fi.getSessionInfo().getUserName(), fi.getSID(), "famiport_reply");
							info.setMessage("famiport reply status is not S\n\n" + reply);
							info.setVerbose(1);
							ic.log(info);
							throw new Exception("famiport reply status is not S");
						}
						
						int idxStatCode = reply.indexOf("<STATCODE>");
						if (idxStatCode >= 0) {
							String statCode = reply.substring(idxStatCode + 10, reply.indexOf("</STATCODE>", idxStatCode));
							if (!statCode.equals("0000")) {
								throw new Exception("famiport reply statcode is " + statCode);
							}
						}
						
						if (m_pinCode.length() == 0) {
							int idxPin = reply.indexOf("<PIN_CODE>");
							if (idxPin < 0) {
								LogInfo info = new LogInfo(fi.getSessionInfo().getUserRowId(), fi.getSessionInfo().getUserName(), fi.getSID(), "famiport_reply");
								info.setMessage("<PIN_CODE> not found\n\n" + reply);
								info.setVerbose(1);
								ic.log(info);
								throw new Exception("<PIN_CODE> not found");
							}
							
							int idxPinEnd = reply.indexOf("</PIN_CODE>");
							if (idxPinEnd < 0) {
								LogInfo info = new LogInfo(fi.getSessionInfo().getUserRowId(), fi.getSessionInfo().getUserName(), fi.getSID(), "famiport_reply");
								info.setMessage("</PIN_CODE> not found\n\n" + reply);
								info.setVerbose(1);
								ic.log(info);
								throw new Exception("</PIN_CODE> not found");
							}
							
							String pinCode = reply.substring(idxPin + 10, idxPinEnd);
							m.payment_info = pinCode;
						}
					} else {
						throw new Exception(response.getStatusLine().toString());
					}
				} finally {
					EntityUtils.consume(entity);
				}
			} finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}
	}
	
	public List<MoneyTransferStatusModel> getStatusChangeHistory(FunctionItem fi, long txnId) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select lm_time, lm_user, old_status_id, new_status_id, comment from money_transfer_status where txn_id = ? order by lm_time desc");
		try {
			pstmt.setLong(1, txnId);
			ResultSet r = pstmt.executeQuery();
			try {
				List<MoneyTransferStatusModel> rows = new ArrayList<>();
				while (r.next()) {
					MoneyTransferStatusModel row = new MoneyTransferStatusModel();
					row.txn_id = txnId;
					row.lm_time = r.getTimestamp(1);
					row.lm_user = r.getString(2);
					row.old_status = TransferStatus.get(r.getInt(3));
					row.new_status = TransferStatus.get(r.getInt(4));
					row.comment = r.getString(5);
					rows.add(row);
				}
				return rows;
			} finally {
				r.close();
			}
		} finally {
			pstmt.close();
		}
	}
	
	private void print(FunctionItem fi, Document doc, MoneyTransferModel moneyTransferModel, MemberModel memberModel, RecipientModel recipientModel, String printLabel, String arcPhotoDirectory, String signaturePhotoDirectory, MemberConfiguration memberConfiguration, boolean useStamp, boolean useStampForBank, int checkBoxSelection) throws Exception {
		if (!FontFactory.isRegistered(C.arial)) {
			String path = fi.getConnection().getGlobalConfig(C.money_transfer, C.font_arial);
			FontFactory.register(path, C.arial);
		}
		if (!FontFactory.isRegistered(C.alger)) {
			String path = fi.getConnection().getGlobalConfig(C.money_transfer, C.font_alger);
			FontFactory.register(path, C.alger);
		}
		
		Hashtable<String, String> conf = fi.getConnection().getGlobalConfig(printLabel);
		
		String companyName = conf.get(C.company_name);
		String companyBankAccount = conf.get(C.company_bank_account);
		String companyPhoneNo = conf.get(C.company_phone_no);
		String companyUid = conf.get(C.company_uid);
		String companyAddress = conf.get(C.company_address);
		
		Font FontArialLarge = FontFactory.getFont(C.arial, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12, Font.NORMAL);
		Font FontArialNormal = FontFactory.getFont(C.arial, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.NORMAL);
		Font FontArialNormalUnderline = FontFactory.getFont(C.arial, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.UNDERLINE);
		Font FontArialSmall = FontFactory.getFont(C.arial, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 8, Font.NORMAL);
		Font FontArialSmallUnderline = FontFactory.getFont(C.arial, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 8, Font.UNDERLINE);
		
		Paragraph p;
		
		p = new Paragraph("外籍勞工薪資結匯申報委託書\nPower of Attorney for Foreign Workers on\nDeclaration of Foreign Exchange Settlement Related to Salaries\n ", FontArialLarge);
		p.setAlignment(Element.ALIGN_CENTER);
		doc.add(p);
		
		p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.add(new Phrase("委託人 ", FontArialNormal));
		p.add(new Phrase(memberModel.member_name, FontArialNormalUnderline));
		p.add(new Phrase(" 為合法入境工作領有外僑居留證之外籍勞工，因無法至指定銀行辦理結匯，茲委託", FontArialNormal));
		p.add(new Phrase(companyName, FontArialNormalUnderline));
		p.add(new Phrase("(請擇一勾選填寫)", FontArialNormal));
		doc.add(p);
		
		p = new Paragraph();
		p.setAlignment(Element.ALIGN_JUSTIFIED);
		p.add(new Phrase(memberModel.member_name, FontArialNormalUnderline));
		p.add(new Phrase(", hereinafter as the consignor, is a legitimate foreign worker immigrated into Taiwan bearing the valid Alien Resident Certificate. I am not available to settle the foreign exchange remittance at an authorized bank personally, thereby appoint the ", FontArialNormal));
		p.add(new Phrase(companyName, FontArialNormalUnderline));
		p.add(new Phrase(" on my behalf to deal the foreign exchange settlement related to my salaries through the authorized bank.\n(Please check the proper item and fill in the necessary information.)", FontArialNormal));
		doc.add(p);
		
		com.itextpdf.text.List l = new com.itextpdf.text.List(false, 10);
		if (checkBoxSelection == 1) {
			l.setListSymbol("\u2611");
		} else {
			l.setListSymbol("\u2610");
		}
		l.add(new ListItem(new Phrase("代為經由指定銀行將委託人之薪資結匯匯至委託人指定之國外帳戶(" + companyBankAccount + ")後，再交付受款人。\nThe fund must be remitted to the consignor’s appointed foreign account (" + companyBankAccount + ") and deliver to my appointed recipients afterward.", FontArialNormal)));
		if (checkBoxSelection == 2) {
			l.setListSymbol("\u2611");
		} else {
			l.setListSymbol("\u2610");
		}
		l.add(new ListItem(new Phrase("代為經由指定銀行將委託人之薪資結匯匯至受款人。\nThe remmitance must be delivered directly to the data of my appointed recipients.", FontArialNormal)));
		doc.add(l);
		
		p = new Paragraph();
		p.add(" ");
		doc.add(p);
		
		String dateString = String.valueOf(moneyTransferModel.txn_id / 1000000);
		Calendar c = Calendar.getInstance();
		c.setTime(DateFormat.getInstance().parse(dateString));
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DATE);
		
		String[] phoneNumbers = memberModel.phone_no.split("\\.");
		String phoneNumber = "";
		for (String s : phoneNumbers) {
			if (s.length() > 0) {
				phoneNumber = s;
				break;
			}
		}

		PdfPCell cell;
		PdfPTable table;
		
		// begin
		table = new PdfPTable(2);
		// draw 1st row
		cell = createCell(FontArialSmall, recipientModel.recipient_name, "受款人", "Recipient", 7, 1, 9);
		cell.setPadding(4);
		table.addCell(cell);
		cell = createCell(FontArialSmall, "", "聯絡電話", "Telephone", 2, 0.5f, 8);
		cell.setPadding(4);
		table.addCell(cell);
		// draw 2nd row
		cell = createCell(FontArialSmall, recipientModel.bank_name, "受款行", "Recipient’s Bank", 7, 1, 9);
		cell.setPadding(4);
		table.addCell(cell);
		cell = createCell(FontArialSmall, "", "分行", "Branch", 2, 0.5f, 8);
		cell.setPadding(4);
		table.addCell(cell);
		// draw 3rd row
		cell = createCell(FontArialSmall, recipientModel.bank_acc, "受款人帳號", "Recipient’s Account No", 7, 1, 9);
		cell.setPadding(4);
		table.addCell(cell);
		cell = createCell(FontArialSmall, Stringify.getString(moneyTransferModel.payment_info), "訂單編號", "Invoice", 2, 0.5f, 8);
		cell.setPadding(4);
		table.addCell(cell);
		// blank
		cell = new PdfPCell();
		cell.setColspan(4);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		// draw 4th row
		cell = createCell(FontArialSmall, memberModel.member_name, "委託人", "Consignor", 4, 1, 10);
		cell.setPadding(4);
		table.addCell(cell);
		cell = createCell(FontArialSmall, String.format("%,d", moneyTransferModel.transfer_amount_ntd), "委託結匯金額", "Amount Remitted", 7, 1, 5);
		cell.setPadding(4);
		table.addCell(cell);
		// draw 5th row
		cell = createCell(FontArialSmall, "INDONESIA", "國籍", "Nationality", 4, 1, 10);
		cell.setPadding(4);
		table.addCell(cell);
		cell = createCell(FontArialSmall, memberModel.arc_no, "外僑居留證號碼", "Alien Resident Certificate No.", 7, 1, 5);
		cell.setPadding(4);
		table.addCell(cell);
		// draw 6th row
		cell = createCell(FontArialSmall, phoneNumber, "在台聯絡電話", "Telephone", 4, 1, 10);
		cell.setPadding(4);
		table.addCell(cell);
		cell = createCell(FontArialSmall, DateFormat.getInstance().formatShort(memberModel.arc_expire_date), "外僑居留證有效期限", "Date of Expiry", 7, 1, 5);
		cell.setPadding(4);
		table.addCell(cell);
		// blank
		cell = new PdfPCell();
		cell.setColspan(4);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		// draw 7th row
		cell = createCell(FontArialSmall, companyName, "受託人", "Consignee", 4, 1, 10);
		cell.setPadding(4);
		table.addCell(cell);
		cell = createCell(FontArialSmall, companyPhoneNo, "連絡電話", "Telephone", 4, 1, 10);
		cell.setPadding(4);
		table.addCell(cell);
		// draw 8th row
		cell = createCell(FontArialSmall, companyUid, "統一編號", "Unform No.", 4, 1, 10);
		cell.setPadding(4);
		table.addCell(cell);
		cell = createCell(FontArialSmall, companyAddress, "地址", "Address", 4, 1, 10);
		cell.setPadding(4);
		table.addCell(cell);
		// end
		table.setWidthPercentage(95);
		doc.add(table);

		Image imgSignature = null;
		byte[] b = memberConfiguration.readImage(fi, signaturePhotoDirectory, memberModel.signature_photo_basename, memberModel.member_id);
		if (b != null) {
			float imageWidth = Float.parseFloat(fi.getConnection().getGlobalConfig(C.money_transfer, C.print_image_signature_width));
			float imageHeight = Float.parseFloat(fi.getConnection().getGlobalConfig(C.money_transfer, C.print_image_signature_height));
			imgSignature = Image.getInstance(b);
			imgSignature.scaleToFit(imageWidth, imageHeight);
		}
		
		Image imgArc = null;
		b = memberConfiguration.readImage(fi, arcPhotoDirectory, memberModel.arc_photo_basename, memberModel.member_id);
		if (b != null) {
			float imageWidth = Float.parseFloat(fi.getConnection().getGlobalConfig(C.money_transfer, C.print_image_arc_width));
			float imageHeight = Float.parseFloat(fi.getConnection().getGlobalConfig(C.money_transfer, C.print_image_arc_height));
			imgArc = Image.getInstance(b);
			imgArc.scaleToFit(imageWidth, imageHeight);
		}
		
		table = new PdfPTable(new float[] {1f, 1.5f, 1f, 1f, 1.5f, 1f});
		cell = new PdfPCell(new Phrase("委託人 Consignor:", FontArialSmall));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPaddingTop(12f);
		cell.setPaddingBottom(3f);
		cell.setNoWrap(true);
		table.addCell(cell);
		if (imgSignature != null)
			cell = new PdfPCell(imgSignature);
		else
			cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setPaddingTop(6f);
		cell.setPaddingBottom(3f);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase("（簽章）Signature", FontArialSmall));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPaddingTop(12f);
		cell.setPaddingBottom(3f);
		cell.setNoWrap(true);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase("受託人 Consignee: ", FontArialSmall));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPaddingTop(12f);
		cell.setPaddingBottom(3f);
		cell.setNoWrap(true);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(companyName, FontArialSmallUnderline));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPaddingTop(12f);
		cell.setPaddingBottom(3f);
		cell.setNoWrap(true);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase("（簽章） Signature", FontArialSmall));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPaddingTop(12f);
		cell.setPaddingBottom(3f);
		cell.setNoWrap(true);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase("中華民國 " + (year - 1911) + " 年 " + month + " 月 " + day + " 日", FontArialSmall));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setPaddingTop(3f);
		cell.setPaddingBottom(6f);
		cell.setNoWrap(true);
		cell.setColspan(3);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase("Date (YY/MM/DD): " + year + " / " + month + " / " + day, FontArialSmall));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setPaddingTop(3f);
		cell.setPaddingBottom(6f);
		cell.setNoWrap(true);
		cell.setColspan(3);
		table.addCell(cell);
		table.setWidthPercentage(95);
		doc.add(table);
		
		if (conf.containsKey(C.seal_1_path) && useStamp) {
			String seal_1_path = conf.get(C.seal_1_path);
			String seal_1_x = conf.get(C.seal_1_x);
			String seal_1_y = conf.get(C.seal_1_y);
			String seal_1_w = conf.get(C.seal_1_w);
			String seal_1_h = conf.get(C.seal_1_h);
			
			String seal_2_path = conf.get(C.seal_2_path);
			String seal_2_x = conf.get(C.seal_2_x);
			String seal_2_y = conf.get(C.seal_2_y);
			String seal_2_w = conf.get(C.seal_2_w);
			String seal_2_h = conf.get(C.seal_2_h);
			
			float x1 = Float.parseFloat(seal_1_x);
			float y1 = Float.parseFloat(seal_1_y);
			float w1 = Float.parseFloat(seal_1_w);
			float h1 = Float.parseFloat(seal_1_h);

			float x2 = Float.parseFloat(seal_2_x);
			float y2 = Float.parseFloat(seal_2_y);
			float w2 = Float.parseFloat(seal_2_w);
			float h2 = Float.parseFloat(seal_2_h);
			
			Image imgSeal1 = Image.getInstance(readImage(seal_1_path));
			imgSeal1.setAbsolutePosition(x1, y1);
			imgSeal1.scaleToFit(w1, h1);
			doc.add(imgSeal1);
			
			Image imgSeal2 = Image.getInstance(readImage(seal_2_path));
			imgSeal2.setAbsolutePosition(x2, y2);
			imgSeal2.scaleToFit(w2, h2);
			doc.add(imgSeal2);
		}
		
		if (imgArc != null) {
			String arc_x = conf.get(C.arc_x);
			String arc_y = conf.get(C.arc_y);
			
			float x1 = Float.parseFloat(arc_x);
			float y1 = Float.parseFloat(arc_y);
			
			imgArc.setAbsolutePosition(x1, y1);
			doc.add(imgArc);
		}
		
		if (conf.containsKey(C.bank_stamp_path) && useStampForBank) {
			String bank_stamp_path = conf.get(C.bank_stamp_path);
			String bank_stamp_x = conf.get(C.bank_stamp_x);
			String bank_stamp_y = conf.get(C.bank_stamp_y);
			String bank_stamp_w = conf.get(C.bank_stamp_w);
			String bank_stamp_h = conf.get(C.bank_stamp_h);
			
			float x1 = Float.parseFloat(bank_stamp_x);
			float y1 = Float.parseFloat(bank_stamp_y);
			float w1 = Float.parseFloat(bank_stamp_w);
			float h1 = Float.parseFloat(bank_stamp_h);
			
			Image imgBankStamp = Image.getInstance(readImage(bank_stamp_path));
			imgBankStamp.setAbsolutePosition(x1, y1);
			imgBankStamp.scaleToFit(w1, h1);
			imgBankStamp.setRotationDegrees(45);
			doc.add(imgBankStamp);
		}
	}
	
	public byte[] readImage(String uri) throws Exception {
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			manager.init();
			
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setStrictHostKeyChecking(opts, "no");
			builder.setUserDirIsRoot(opts, true);
			builder.setTimeout(opts, 10000);
			FileObject file = manager.resolveFile(uri, opts);
			if (file.exists()) {
				FileContent content = file.getContent();
				InputStream stream = content.getInputStream();
				try {
					return IOUtils.toByteArray(stream);
				} finally {
					stream.close();
				}
			} else {
				return null;
			}
		} finally {
			manager.close();
		}
	}

	public PdfPCell createCell(Font f, String value, String title1, String title2, float f1, float f2, float f3) {
		PdfPTable t2 = new PdfPTable(new float[] {f1, f2, f3});
		PdfPCell c2 = new PdfPCell(new Phrase(title1, f));
		c2.setBorder(Rectangle.NO_BORDER);
		t2.addCell(c2);
		c2 = new PdfPCell(new Phrase(":", f));
		c2.setBorder(Rectangle.NO_BORDER);
		t2.addCell(c2);
		c2 = new PdfPCell(new Phrase(value, f));
		c2.setBorder(Rectangle.NO_BORDER);
		c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
		c2.setRowspan(2);
		t2.addCell(c2);
		c2 = new PdfPCell(new Phrase(title2, f));
		c2.setBorder(Rectangle.NO_BORDER);
		t2.addCell(c2);
		c2 = new PdfPCell(new Phrase(":", f));
		c2.setBorder(Rectangle.NO_BORDER);
		t2.addCell(c2);
		return new PdfPCell(t2);
	}
	
	private void print(FunctionItem fi, Document doc, Long[] txnIds, String printLabel, boolean useStamp, boolean useStampForBank, int checkBoxSelection) throws Exception {
		String signaturePhotoDirectory = fi.getConnection().getGlobalConfig(C.member, C.signature_photo_directory);
		String arcPhotoDirectory = fi.getConnection().getGlobalConfig(C.member, C.arc_photo_directory);
		
		MemberConfiguration member = new MemberConfiguration();
		for (int i = 0; i < txnIds.length;) {
			long txnId = txnIds[i];
			MoneyTransferModel moneyTransferModel = this.getData(fi, txnId);
			MemberModel memberModel = member.getData(fi, moneyTransferModel.member_id);
			RecipientModel recipientModel = member.getRecipient(fi, moneyTransferModel.member_id, moneyTransferModel.recipient_id);
			
			memberModel.member_name = memberModel.member_name.toUpperCase();
			recipientModel.recipient_name = recipientModel.recipient_name.toUpperCase();
			
			print(fi, doc, moneyTransferModel, memberModel, recipientModel, printLabel, arcPhotoDirectory, signaturePhotoDirectory, member, useStamp, useStampForBank, checkBoxSelection);
			
			i++;
			if (i < txnIds.length) {
				doc.newPage();
			}
		}
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmtUpdate = conn.prepareStatement("update money_transfer set is_print = 1 where txn_id = ?");
		try {
			for (int i = 0; i < txnIds.length; i++) {
				long txnId = txnIds[i];
				
				pstmtUpdate.setLong(1, txnId);
				pstmtUpdate.executeUpdate();
				fi.getConnection().logHistory(IndogoTable.money_transfer, txnId, HistoryAction.update, null, fi.getConnection().getCurrentTime(), fi.getSessionInfo().getUserName(),
						new HistoryData(C.is_print, null, "true"));
			}
		} finally {
			pstmtUpdate.close();
		}
	}
	
	public String print(FunctionItem fi, long txnId, String printLabel, boolean useStamp, boolean useStampForBank, int checkBoxSelection) throws Exception {
		String filename = txnId + ".pdf";
		
		Document doc = new Document();
		PdfWriter.getInstance(doc, new FileOutputStream(new File(fi.getTempFolder(), filename), false));
		doc.setMargins(15, 15, 15, 15);
		doc.setMarginMirroring(true);
		doc.open();
		try {
			print(fi, doc, new Long[] { txnId }, printLabel, useStamp, useStampForBank, checkBoxSelection);
			return filename;
		}
		finally {
			try {
				doc.close();
			} catch (Exception ignore) {}
		}
	}
	
	public String printAllAsPdf(FunctionItem fi, Long[] txnIds, String printLabel, boolean useStamp, boolean useStampForBank, int checkBoxSelection) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		try {
			String filename = "all.pdf";
			Document doc = new Document();
			PdfWriter.getInstance(doc, new FileOutputStream(new File(fi.getTempFolder(), filename), false));
			doc.setMargins(15, 15, 15, 15);
			doc.setMarginMirroring(true);
			doc.open();
			try {
				print(fi, doc, txnIds, printLabel, useStamp, useStampForBank, checkBoxSelection);
			}
			finally {
				try {
					doc.close();
				} catch (Exception ignore) {}
			}
			conn.commit();
			return filename;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	public String printAllAsZip(FunctionItem fi, Long[] txnIds, String printLabel, boolean useStamp, boolean useStampForBank, int checkBoxSelection) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		try {
			String filename = "all.zip";
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(fi.getTempFolder(), filename), false));
			try {
				for (long txnId : txnIds) {
					String name = this.print(fi, txnId, printLabel, useStamp, useStampForBank, checkBoxSelection);
					ZipEntry e = new ZipEntry(name);
					zipOut.putNextEntry(e);
					FileInputStream pdfStream = new FileInputStream(new File(fi.getTempFolder(), name));
					try {
						IOUtils.copy(pdfStream, zipOut);
					} finally {
						pdfStream.close();
					}
					zipOut.closeEntry();
				}
			} finally {
				zipOut.close();
			}
			conn.commit();
			return filename;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	public String printAllAsImage(FunctionItem fi, Long[] txnIds, String printLabel, boolean useStamp, boolean useStampForBank, int checkBoxSelection) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		try {
			String filename = "all.pdf";
			File fileAllPdf = new File(fi.getTempFolder(), filename);
			Document doc = new Document();
			PdfWriter.getInstance(doc, new FileOutputStream(fileAllPdf, false));
			doc.setMargins(15, 15, 15, 15);
			doc.setMarginMirroring(true);
			doc.open();
			try {
				print(fi, doc, txnIds, printLabel, useStamp, useStampForBank, checkBoxSelection);
			}
			finally {
				try {
					doc.close();
				} catch (Exception ignore) {}
			}
			
			PDDocument document = PDDocument.load(fileAllPdf);
			if (txnIds.length < 2) {
				String imageFilename = txnIds[0] + ".jpg";
				try {
					PDFRenderer pdfRenderer = new PDFRenderer(document);
					BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
					ImageIO.write(bim, "jpg", new File(fi.getTempFolder(), imageFilename));
				} finally {
					document.close();
					FileUtils.deleteQuietly(fileAllPdf);
				}
				conn.commit();
				return imageFilename;
			} else {
				String zipFilename = "all.zip";
				try {
					PDFRenderer pdfRenderer = new PDFRenderer(document);
					ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(fi.getTempFolder(), zipFilename), false));
					try {
						for (int i = 0; i < txnIds.length; i++) {
							String imageFilename = txnIds[i] + ".jpg";
							File fileImage = new File(fi.getTempFolder(), imageFilename);
							BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);
							ImageIO.write(bim, "jpg", fileImage);
							try {
								ZipEntry e = new ZipEntry(imageFilename);
								zipOut.putNextEntry(e);
								FileInputStream pdfStream = new FileInputStream(new File(fi.getTempFolder(), imageFilename));
								try {
									IOUtils.copy(pdfStream, zipOut);
								} finally {
									pdfStream.close();
								}
								zipOut.closeEntry();
							} finally {
								FileUtils.deleteQuietly(fileImage);
							}
						}
					} finally {
						zipOut.close();
					}
				} finally {
					document.close();
					FileUtils.deleteQuietly(fileAllPdf);
				}
				conn.commit();
				return zipFilename;
			}
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	private final static char[] ALLOWED_CHARS = "ABCDFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
	
	public String generateSevenElevenPaymentInfo() {
		StringBuilder sb = new StringBuilder(11);
		sb.append("GRT");
		SecureRandom r = new SecureRandom();
		for (int i = 0; i < 11; i++) {
			sb.append(ALLOWED_CHARS[r.nextInt(ALLOWED_CHARS.length)]);
		}
		return sb.toString();
	}
	
	public String generateCashPaymentInfo() {
		StringBuilder sb = new StringBuilder(11);
		sb.append("CSH");
		SecureRandom r = new SecureRandom();
		for (int i = 0; i < 11; i++) {
			sb.append(ALLOWED_CHARS[r.nextInt(ALLOWED_CHARS.length)]);
		}
		return sb.toString();
	}

}
