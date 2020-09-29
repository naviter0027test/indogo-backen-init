package com.indogo.relay.member;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.poi.util.IOUtils;

import com.indogo.IndogoTable;
import com.indogo.MemberSex;
import com.indogo.MemberStatus;
import com.indogo.TransferMoneyThroughBank;
import com.indogo.bni.BniHostToHost;
import com.indogo.bri.BriHostToHost;
import com.indogo.model.member.BankCodeModel;
import com.indogo.model.member.MemberModel;
import com.indogo.model.member.RecipientModel;
import com.indogo.model.member.VerifyResult;
import com.indogo.relay.member.BankCodeConfiguration.OrderBy;
import com.lionpig.webui.database.HistoryAction;
import com.lionpig.webui.database.HistoryData;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.database.RoleNameListModel;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.func.TablePage;
import com.lionpig.webui.http.func.TablePageRowPrinter;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.DateFormat;
import com.lionpig.webui.http.util.Helper;
import com.lionpig.webui.http.util.Stringify;

public class MemberConfiguration implements IFunction, ITablePage {
	
	private static Pattern arcNoPattern = Pattern.compile("[A-Z][A-Z][0-9]{8}");
	private static Pattern phoneNoPattern = Pattern.compile("[0-9]{1,10}");

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
		cols.add(new TablePageColumn(C.phone_no, C.columnTypeString, C.columnDirectionDefault, true, false, "Phone No"));
		cols.add(new TablePageColumn(C.arc_no, C.columnTypeString, C.columnDirectionDefault, true, false, "ARC"));
		cols.add(new TablePageColumn(C.arc_expire_date, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "ARC Expire"));
		cols.add(new TablePageColumn(C.address, C.columnTypeString, C.columnDirectionDefault, true, false, "Address"));
		cols.add(new TablePageColumn(C.birthday, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Birthday"));
		cols.add(new TablePageColumn(C.email, C.columnTypeString, C.columnDirectionDefault, true, false, "Email"));
		cols.add(new TablePageColumn(C.recipient_button, C.columnTypeString, C.columnDirectionNone, false, true, "Recipient"));
		cols.add(new TablePageColumn(C.sex_name, C.columnTypeString, C.columnDirectionNone, false, true, "Sex"));
		cols.add(new TablePageColumn(C.status_name, C.columnTypeString, C.columnDirectionNone, false, true, "Member Status"));
		cols.add(new TablePageColumn(C.print, C.columnTypeString, C.columnDirectionNone, false, true, "Print Status"));
		cols.add(new TablePageColumn(C.wallet, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Wallet"));
		cols.add(new TablePageColumn(C.app_verify_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, C.app_verify_time));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, false));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, C.lm_user));
		cols.add(new TablePageColumn(C.status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.status_id, true, true));
		cols.add(new TablePageColumn(C.signature_need_fix, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.signature_need_fix, true, true));
		cols.add(new TablePageColumn(C.sex_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.signature_need_fix, true, true));
		cols.add(new TablePageColumn(C.is_print, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_print, true, true));
		return cols;
	}
	
	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r, boolean isHtml,
			TablePageRowAttribute rowAttr) throws Exception {
		int status_id = r.unwrap().unwrap().getInt(C.status_id);
		MemberSex sex = MemberSex.get(r.unwrap().getInt(C.sex_id));
		int is_print = r.unwrap().unwrap().getInt(C.is_print);

		cols.get(C.action).setValue(C.emptyString);
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
		cols.get(C.member_name).setValue(r.getString(C.member_name));
		cols.get(C.phone_no).setValue(r.getString(C.phone_no));
		cols.get(C.arc_no).setValue(r.getString(C.arc_no));
		cols.get(C.arc_expire_date).setValue(r.getDate(C.arc_expire_date));
		cols.get(C.address).setValue(r.getString(C.address));
		cols.get(C.birthday).setValue(r.getDate(C.birthday));
		cols.get(C.email).setValue(r.getString(C.email));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.recipient_button).setValue(C.recipient_button_html);
		cols.get(C.status_id).setValue(String.valueOf(status_id));
		cols.get(C.signature_need_fix).setValue(r.getInt(C.signature_need_fix));
		cols.get(C.sex_name).setValue(sex.getShortName());
		cols.get(C.sex_id).setValue(String.valueOf(sex.getId()));
		cols.get(C.status_name).setValue(status_id == 0 ? C.ACTIVE : C.INACTIVE);
		cols.get(C.is_print).setValue(String.valueOf(is_print));
		cols.get(C.print).setValue(C.emptyString);
		cols.get(C.wallet).setValue(r.getIntCurrency(C.wallet));
		cols.get(C.app_verify_time).setValue(r.getTimestamp(C.app_verify_time));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
	}
	
	public static String formatArcPhotoUrl(FunctionItem fi, long member_id, String arc_photo_basename) throws Exception {
		String arcPhotoUrl = fi.getConnection().getGlobalConfig(C.member, C.arc_photo_url);
		long level = member_id % 5000;
		StringBuilder sb = new StringBuilder();
		sb.append(arcPhotoUrl).append("/").append(level).append("/").append(member_id).append("/").append(arc_photo_basename).append("_original.png");
		return sb.toString();
	}
	
	public static String formatSignaturePhotoUrl(FunctionItem fi, long member_id, String signature_photo_basename) throws Exception {
		String signaturePhotoUrl = fi.getConnection().getGlobalConfig(C.member, C.signature_photo_url);
		long level = member_id % 5000;
		StringBuilder sb = new StringBuilder();
		sb.append(signaturePhotoUrl).append("/").append(level).append("/").append(member_id).append("/").append(signature_photo_basename).append("_original.png");
		return sb.toString();
	}
	
	public static String getAddressInHtmlOptionFormat(FunctionItem fi) throws Exception {
		Statement stmt = fi.getConnection().getConnection().createStatement();
		try {
			StringBuilder sb = new StringBuilder(100);
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(stmt.executeQuery("select city_cname, city_ename, seq from address_city order by seq"))) {
				while (r.next()) {
					sb.append("<option value=\"").append(r.getString(1)).append(C.char_30).append(r.getString(2)).append("\">").append(r.getString(1)).append(" (").append(r.getString(2)).append(")</option>");
				}
			}
			return sb.toString();
		} finally {
			stmt.close();
		}
	}

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		Connection conn = fi.getConnection().getConnection();
		
		if (action.equals(C.init)) {
			StringBuilder sb = new StringBuilder(100);
			
			sb.append(getAddressInHtmlOptionFormat(fi));
		
			String arcPhotoUrl = fi.getConnection().getGlobalConfig("MEMBER", "ARC_PHOTO_URL");
			sb.append(C.char_31).append(arcPhotoUrl).append("/nophoto_320_240.png");
			
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
			
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			sb.append(C.char_31).append(roles.size());
			for (RoleNameListModel role : roles) {
				sb.append(C.char_31).append(role.ROLE_ID);
			}
			
			StringBuilder sbSex = new StringBuilder();
			int sbSexSize = 0;
			for (MemberSex e : EnumSet.allOf(MemberSex.class)) {
				sbSex.append(C.char_31).append(e.getId()).append(C.char_31).append(e.getShortName());
				sbSexSize++;
			}
			sb.append(C.char_31).append(sbSexSize).append(sbSex);
			
			return sb.toString();
		} else if (action.equals(C.insert))
			return insert(fi);
		else if (action.equals(C.update))
			return update(fi);
		else if (action.equals(C.delete))
			return delete(fi);
		else if (action.equals(C.getDataForUpdate)) {
			long memberId = Helper.getLong(params, C.member_id, true);
			return getDataForUpdate(fi, memberId);
		} else if (action.equals(C.kurs_refresh)) {
			String kursValue = fi.getConnection().getGlobalConfig(C.member, C.kurs_value);
			String kursLmTime = fi.getConnection().getGlobalConfig(C.member, C.kurs_lm_time);
			StringBuilder sb = new StringBuilder();
			sb.append(kursValue).append(C.char_31).append(kursLmTime);
			return sb.toString();
		} else if (action.equals(C.getAddressArea)) {
			String city = Helper.getString(params, "city", true);
			PreparedStatement pstmt = conn.prepareStatement("select area_cname, area_ename, zipcode from address_area where city_cname = ? order by zipcode");
			try {
				pstmt.setString(1, city);
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
					StringBuilder sb = new StringBuilder(100);
					while (r.next()) {
						sb.append("<option value=\"").append(r.getString(1)).append((char)31).append(r.getString(2)).append("\">").append(r.getString(3)).append(" ").append(r.getString(1)).append(" (").append(r.getString(2)).append(")</option>");
					}
					return sb.toString();
				}
			} finally {
				pstmt.close();
			}
		} else if (action.equals(C.getAddressRoad)) {
			String city = Helper.getString(params, "city", true);
			String area = Helper.getString(params, "area", true);
			PreparedStatement pstmt = conn.prepareStatement("select road_cname, road_ename, zipcode, seq from address_road where city_cname = ? and area_cname = ? order by seq");
			try {
				pstmt.setString(1, city);
				pstmt.setString(2, area);
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
					StringBuilder sb = new StringBuilder(100);
					while (r.next()) {
						sb.append("<option value=\"").append(r.getString(1)).append((char)31).append(r.getString(2)).append((char)31).append(r.getString(3)).append("\">").append(r.getString(1)).append(" (").append(r.getString(2)).append(")</option>");
					}
					return sb.toString();
				}
			} finally {
				pstmt.close();
			}
		} else if (action.equals(C.getRecipients)) {
			long memberId = Helper.getLong(params, C.member_id, true);
			return getRecipients(fi, memberId);
		} else if (action.equals(C.updateRecipients)) {
			long memberId = Helper.getLong(params, C.member_id, true);
			String rowsString = Helper.getString(params, "rows", true);
			String[] rows = StringUtils.splitPreserveAllTokens(rowsString, C.char_30);
			PreparedStatement pstmt = conn.prepareStatement("select recipient_name, bank_acc, bank_code, lm_time, is_hidden, recipient_name_2 from member_recipient where member_id = ? and recipient_id = ? for update");
			PreparedStatement pstmtInsert = conn.prepareStatement("insert into member_recipient (member_id, recipient_id, recipient_name, bank_acc, bank_code, lm_time, lm_user, is_hidden, is_verified, recipient_name_2) values (?,?,?,?,?,?,?,?,?,?)");
			PreparedStatement pstmtUpdate = conn.prepareStatement("update member_recipient set recipient_name = ?, bank_acc = ?, bank_code = ?, lm_time = ?, lm_user = ?, is_hidden = ?, is_verified = ?, recipient_name_2 = ? where member_id = ? and recipient_id = ?");
			PreparedStatement pstmtDelete = conn.prepareStatement("delete from member_recipient where member_id = ? and recipient_id = ?");
			try {
				Timestamp currentTime = fi.getConnection().getCurrentTime();
				String lmUser = fi.getSessionInfo().getUserName();
				
				HashMap<Integer, String[]> map = new HashMap<>();
				for (String row : rows) {
					String[] cells = StringUtils.splitPreserveAllTokens(row, C.char_31);
					String actionType = cells[0];
					int recipientId;
					if (actionType.equals(C.insert)) {
						recipientId = (int)fi.getConnection().getSeq(C.member_id + "_" + memberId, true);
					} else
						recipientId = Integer.parseInt(cells[1]);
					map.put(recipientId, cells);
				}
				int[] recipientIds = new int[map.keySet().size()];
				int i = 0;
				for (int recipientId : map.keySet()) {
					recipientIds[i] = recipientId;
					i++;
				}
				Arrays.sort(recipientIds);
				
				for (int recipientId : recipientIds) {
					String[] cells = map.get(recipientId);
					String actionType = cells[0];
					String recipientName = cells[2].toUpperCase();
					String bankAcc = cells[3];
					String bankCode = cells[4];
					String lmTimeString = cells[5];
					int isHidden = Integer.parseInt(cells[6]);
					int isVerified = Integer.parseInt(cells[7]);
					String recipient_name_2 = cells[8];
					if (recipient_name_2.length() == 0) {
						recipient_name_2 = recipientName;
					}
					
					if (actionType.equals(C.update) || actionType.equals(C.delete)) {
						Timestamp lmTime = new Timestamp(DateFormat.getInstance().parse(lmTimeString).getTime());
						pstmt.setLong(1, memberId);
						pstmt.setInt(2, recipientId);
						ResultSet r = pstmt.executeQuery();
						try {
							if (r.next()) {
								String oldRecipientName = r.getString(1);
								String oldBankAcc = r.getString(2);
								String oldBankCode = r.getString(3);
								Timestamp oldLmTime = r.getTimestamp(4);
								int oldIsHidden = r.getInt(5);
								String oldRecipientName2 = r.getString(6);
								
								if (!lmTime.equals(oldLmTime))
									throw new Exception(String.format(C.data_already_updated_by_another_user, "member_id = " + memberId + ", recipient_id = " + recipientId));
								
								if (actionType.equals(C.update)) {
									List<HistoryData> history = new ArrayList<>();
									history.add(new HistoryData(C.member_id, String.valueOf(memberId), String.valueOf(memberId)));
									history.add(new HistoryData(C.recipient_id, String.valueOf(recipientId), String.valueOf(recipientId)));
									if (!Helper.isEquals(oldRecipientName, recipientName))
										history.add(new HistoryData(C.recipient_name, oldRecipientName, recipientName));
									if (!Helper.isEquals(oldBankAcc, bankAcc))
										history.add(new HistoryData(C.bank_acc, oldBankAcc, bankAcc));
									if (!Helper.isEquals(oldBankCode, bankCode))
										history.add(new HistoryData(C.bank_code, oldBankCode, bankCode));
									if (oldIsHidden != isHidden)
										history.add(new HistoryData(C.is_hidden, String.valueOf(oldIsHidden), String.valueOf(isHidden)));
									if (!Helper.isEquals(oldRecipientName2, recipient_name_2))
										history.add(new HistoryData(C.recipient_name_2, oldRecipientName2, recipient_name_2));
	
									if (history.size() > 2) {
										pstmtUpdate.setString(1, recipientName);
										pstmtUpdate.setString(2, bankAcc);
										pstmtUpdate.setString(3, bankCode);
										pstmtUpdate.setTimestamp(4, currentTime);
										pstmtUpdate.setString(5, lmUser);
										pstmtUpdate.setInt(6, isHidden);
										pstmtUpdate.setInt(7, isVerified);
										pstmtUpdate.setString(8, recipient_name_2);
										pstmtUpdate.setLong(9, memberId);
										pstmtUpdate.setInt(10, recipientId);
										pstmtUpdate.executeUpdate();
										
										fi.getConnection().logHistory(IndogoTable.member_recipient, memberId, HistoryAction.update, null, currentTime, fi.getSessionInfo().getUserName(), Helper.toHistoryDataArray(history));
									}
								} else if (actionType.equals(C.delete)) {
									pstmtDelete.setLong(1, memberId);
									pstmtDelete.setInt(2, recipientId);
									pstmtDelete.executeUpdate();
									
									List<HistoryData> history = new ArrayList<>();
									history.add(new HistoryData(C.member_id, String.valueOf(memberId), null));
									history.add(new HistoryData(C.recipient_id, String.valueOf(recipientId), null));
									history.add(new HistoryData(C.recipient_name, oldRecipientName, null));
									history.add(new HistoryData(C.bank_acc, oldBankAcc, null));
									history.add(new HistoryData(C.bank_code, oldBankCode, null));
									history.add(new HistoryData(C.recipient_name_2, oldRecipientName2, null));
									fi.getConnection().logHistory(IndogoTable.member_recipient, memberId, HistoryAction.delete, null, currentTime, fi.getSessionInfo().getUserName(), Helper.toHistoryDataArray(history));
								}
							} else {
								throw new Exception(String.format(C.data_not_exist, "member_id = " + memberId + ", recipient_id = " + recipientId));
							}
						} finally {
							r.close();
						}
					} else if (actionType.equals(C.insert)) {
						pstmtInsert.setLong(1, memberId);
						pstmtInsert.setInt(2, recipientId);
						pstmtInsert.setString(3, recipientName);
						pstmtInsert.setString(4, bankAcc);
						pstmtInsert.setString(5, bankCode);
						pstmtInsert.setTimestamp(6, currentTime);
						pstmtInsert.setString(7, lmUser);
						pstmtInsert.setInt(8, isHidden);
						pstmtInsert.setInt(9, isVerified);
						pstmtInsert.setString(10, recipient_name_2);
						pstmtInsert.executeUpdate();
						
						fi.getConnection().logHistory(IndogoTable.member_recipient, memberId, HistoryAction.add, null, currentTime, lmUser,
								new HistoryData(C.member_id, null, String.valueOf(memberId)),
								new HistoryData(C.recipient_id, null, String.valueOf(recipientId)),
								new HistoryData(C.recipient_name, null, recipientName),
								new HistoryData(C.bank_acc, null, bankAcc),
								new HistoryData(C.bank_code, null, bankCode),
								new HistoryData(C.recipient_name_2, null, recipient_name_2));
					}
				}
				
				conn.commit();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				pstmt.close();
				pstmtInsert.close();
				pstmtUpdate.close();
			}
			
			return "1";
		} else if (action.equals(C.getRecipient)) {
			long memberId = Helper.getLong(params, C.member_id, true);
			int recipientId = Helper.getInt(params, C.recipient_id, true);
			RecipientModel recipientModel = this.getRecipient(fi, memberId, recipientId);
			return recipientModel.toString();
		} else if (action.equals(C.verify_recipient)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			int recipient_id = Helper.getInt(params, C.recipient_id, true);
			boolean auto_update_name = Helper.getInt(params, C.auto_update_name, false) == 1;
			String bank_name = Helper.getString(params, C.bank_name, false);
			try {
				TransferMoneyThroughBank bank;
				if (bank_name == null) {
					bank_name = fi.getConnection().getGlobalConfig(C.h2h, C.default_acc_verification);
				}
				
				try {
					bank = TransferMoneyThroughBank.valueOf(bank_name);
				} catch (Exception ignore) {
					bank = TransferMoneyThroughBank.BNI;
				}
				
				VerifyResult result = this.verifyRecipient(fi, member_id, recipient_id, auto_update_name, bank);
				if (result.already_verified)
					throw new Exception("recipient has been verified");
				if (result.bni_name_check_failed) {
					conn.rollback();
					return "0" + C.char_31 + result.recipient_name + C.char_31 + result.bni_recipient_name;
				}
				conn.commit();
				return "1" + C.char_31 + result.recipient_name;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.resolve_recipient_name)) {
			String bank_code = Helper.getString(params, C.bank_code, true);
			String bank_acc = Helper.getString(params, C.bank_acc, true);
			
			TransferMoneyThroughBank bank;
			try {
				bank = TransferMoneyThroughBank.valueOf(fi.getConnection().getGlobalConfig(C.h2h, C.default_acc_verification));
			} catch (Exception ignore) {
				bank = TransferMoneyThroughBank.BNI;
			}
			
			switch (bank) {
				case BNI:
					try (BniHostToHost bni = new BniHostToHost(fi.getConnection())) {
						return bni.getAccountName(bank_code, bank_acc);
					}
				case BRI:
					BriHostToHost bri = new BriHostToHost(fi.getConnection());
					String token = bri.requestToken();
					return bri.inquiryAccount(token, bank_code, bank_acc);
				default:
					throw new Exception(String.format(C.function_not_yet_implemented, "verifyRecipient via bank " + bank));
			}
		} else if (action.equals(C.updateRecipient)) {
			RecipientModel recipientModel = new RecipientModel();
			recipientModel.member_id = Helper.getLong(params, C.member_id, true);
			recipientModel.recipient_id = Helper.getInt(params, C.recipient_id, true);
			recipientModel.bank_acc = Helper.getString(params, C.bank_acc, true);
			recipientModel.bank_code = Helper.getString(params, C.bank_code, true);
			recipientModel.lm_time = Helper.getTimestamp(params, C.lm_time, true);
			
			try {
				TransferMoneyThroughBank bank;
				try {
					bank = TransferMoneyThroughBank.valueOf(fi.getConnection().getGlobalConfig(C.h2h, C.default_acc_verification));
				} catch (Exception ignore) {
					bank = TransferMoneyThroughBank.BNI;
				}
				
				switch (bank) {
					case BNI:
						try (BniHostToHost bni = new BniHostToHost(fi.getConnection())) {
							recipientModel.recipient_name = bni.getAccountName(recipientModel.bank_code, recipientModel.bank_acc);
							recipientModel.is_verified = true;
						}
						break;
					case BRI:
						BriHostToHost bri = new BriHostToHost(fi.getConnection());
						String token = bri.requestToken();
						recipientModel.recipient_name = bri.inquiryAccount(token, recipientModel.bank_code, recipientModel.bank_acc);
						recipientModel.is_verified = true;
						break;
					default:
						throw new Exception(String.format(C.function_not_yet_implemented, "verifyRecipient via bank " + bank));
				}
				
				this.updateRecipient(fi, recipientModel);
				conn.commit();
				return recipientModel.toString();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.insertRecipient)) {
			RecipientModel recipientModel = new RecipientModel();
			recipientModel.member_id = Helper.getLong(params, C.member_id, true);
			recipientModel.bank_acc = Helper.getString(params, C.bank_acc, true);
			recipientModel.bank_code = Helper.getString(params, C.bank_code, true);
			
			TransferMoneyThroughBank bank;
			try {
				bank = TransferMoneyThroughBank.valueOf(fi.getConnection().getGlobalConfig(C.h2h, C.default_acc_verification));
			} catch (Exception ignore) {
				bank = TransferMoneyThroughBank.BNI;
			}
			
			switch (bank) {
				case BNI:
					try (BniHostToHost bni = new BniHostToHost(fi.getConnection())) {
						recipientModel.recipient_name = bni.getAccountName(recipientModel.bank_code, recipientModel.bank_acc);
						recipientModel.is_verified = true;
					}
					break;
				case BRI:
					BriHostToHost bri = new BriHostToHost(fi.getConnection());
					String token = bri.requestToken();
					recipientModel.recipient_name = bri.inquiryAccount(token, recipientModel.bank_code, recipientModel.bank_acc);
					recipientModel.is_verified = true;
					break;
				default:
					throw new Exception(String.format(C.function_not_yet_implemented, "verifyRecipient via bank " + bank));
			}
			
			try {
				insertRecipient(fi, recipientModel);
				
				List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
				filter.add(new TablePageFilter("member_id", "NUMBER", "=", String.valueOf(recipientModel.member_id), null));
				filter.add(new TablePageFilter("recipient_id", "NUMBER", "=", String.valueOf(recipientModel.recipient_id), null));
				TablePage p = new TablePage();
				RecipientView view = new RecipientView();
				String s = p.getRows(view, fi, 1, 1, view.getColumns(fi), null, filter, null, null, null);
				
				conn.commit();
				
				return s;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.update_member_status)) {
			List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
			boolean isAllowed = false;
			for (RoleNameListModel role : roles) {
				if (role.ROLE_ID == 1 || role.ROLE_ID == 2 || role.ROLE_ID == 3 || role.ROLE_ID == 4) {
					isAllowed = true;
					break;
				}
			}
			
			if (!isAllowed) {
				throw new Exception(String.format(C.function_not_allowed, "Ban or Release Member"));
			}
			
			long member_id = Helper.getLong(params, C.member_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			int status_id = Helper.getInt(params, C.status_id, true);
			try {
				Timestamp new_lm_time = updateMemberStatus(fi, member_id, lm_time, status_id);
				conn.commit();
				return Stringify.getTimestamp(new_lm_time);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.hide_recipient)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			int recipient_id = Helper.getInt(params, C.recipient_id, true);
			boolean is_hidden = Helper.getInt(params, C.is_hidden, true) == 1;
			try {
				hideRecipient(fi, member_id, recipient_id, is_hidden);
				conn.commit();
				return C.emptyString;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.arc_photo_upload)) {
			Hashtable<String, FileItem> uploadFiles = fi.getUploadedFiles();
			if (uploadFiles.size() > 0) {
				FileItem photo = uploadFiles.get(C.arc_photo_upload);
				if (photo != null && photo.getName().length() > 0) {
					String arc_photo_basename = Helper.getString(params, C.arc_photo_basename, false);
					arc_photo_basename = saveImage(fi, photo.getInputStream(), fi.getConnection().getGlobalConfig(C.member, C.arc_photo_directory), arc_photo_basename);
					
					String baseUrl = fi.getConnection().getGlobalConfig(C.member, C.arc_photo_url);
					
					StringBuilder sb = new StringBuilder();
					sb.append(arc_photo_basename).append(C.char_31)
					.append(baseUrl).append("/temp/").append(fi.getSessionInfo().getUserRowId()).append("/").append(arc_photo_basename).append("_original.png");
					return sb.toString();
				} else
					throw new Exception("input [arc_photo_upload] cannot be emtpy");
			} else
				throw new Exception("input [arc_photo_upload] cannot be emtpy");
		} else if (action.equals(C.signature_photo_upload)) {
			Hashtable<String, FileItem> uploadFiles = fi.getUploadedFiles();
			if (uploadFiles.size() > 0) {
				FileItem photo = uploadFiles.get(C.signature_photo_upload);
				if (photo != null && photo.getName().length() > 0) {
					String signature_photo_basename = Helper.getString(params, C.signature_photo_basename, false);
					signature_photo_basename = saveImage(fi, photo.getInputStream(), fi.getConnection().getGlobalConfig(C.member, C.signature_photo_directory), signature_photo_basename);
					
					String baseUrl = fi.getConnection().getGlobalConfig(C.member, C.signature_photo_url);
					
					StringBuilder sb = new StringBuilder();
					sb.append(signature_photo_basename).append(C.char_31)
					.append(baseUrl).append("/temp/").append(fi.getSessionInfo().getUserRowId()).append("/").append(signature_photo_basename).append("_original.png");
					return sb.toString();
				} else
					throw new Exception("input [signature_photo_upload] cannot be emtpy");
			} else
				throw new Exception("input [signature_photo_upload] cannot be emtpy");
		} else if (action.equals(C.check_recipient_name_with_bni)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			int recipient_id = Helper.getInt(params, C.recipient_id, true);
			String bank_code, bank_acc, recipient_name;
			PreparedStatement pstmt = conn.prepareStatement("select bank_code, bank_acc, recipient_name from member_recipient where member_id = ? and recipient_id = ?");
			try {
				ResultSet r = pstmt.executeQuery();
				try {
					if (r.next()) {
						bank_code = r.getString(1);
						bank_acc = r.getString(2);
						recipient_name = r.getString(3);
					} else
						throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id + ", recipient_id = " + recipient_id));
				} finally {
					r.close();
				}
			} finally {
				pstmt.close();
			}
			
			try (BniHostToHost bni = new BniHostToHost(fi.getConnection())) {
				String accountName = bni.getAccountName(bank_code, bank_acc);
				return recipient_name + C.char_31 + accountName;
			}
		} else if (action.equals(C.export_bank_format)) {
			TablePage tablePage = new TablePage();
			
			final List<String> member_ids = new ArrayList<>();
			final Timestamp currentTime = fi.getConnection().getCurrentTime();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd");
			String filename = "COMBINED_foreign_" + dateFormat.format(currentTime) + ".txt";
			File file = new File(fi.getTempFolder(), filename);
			try (PrintWriter pw = new PrintWriter(file)) {
				tablePage.printRows(fi, this, fi.getRequestParameters(), new TablePageRowPrinter() {
					@Override
					public void print(Hashtable<String, TablePageColumn> htColumn) {
						String member_id = htColumn.get(C.member_id).getValue();
						String member_name = htColumn.get(C.member_name).getValue();
						String arc_no = htColumn.get(C.arc_no).getValue();
						String birthday = htColumn.get(C.birthday).getValue();
						String sex_name = htColumn.get(C.sex_name).getValue();
						String status_name = htColumn.get(C.status_name).getValue();
						
						pw.print(StringUtils.rightPad(member_name, 60, ' '));
						pw.print("|FOREIGN");
						pw.print(StringUtils.rightPad(arc_no, 20, ' '));
						pw.print("|                                                  |                                                  |                                                  |                                                  |                                                  |                                                  |                                                  |                                                  |                                        |INDIVIDUAL                                        |                                        |                                        |                                        |                                        |                                        |                                        |                                        |        |ID        |INDONESIA                                         |TW                            |TAIWAN                                            |");
						pw.print(birthday);
						pw.print('|');
						pw.print(sex_name);
						pw.print("|CIF164              |FOREIGN");
						pw.print(StringUtils.rightPad(arc_no, 17, ' '));
						pw.print("|00|");
						pw.print(StringUtils.rightPad(status_name, 8, ' '));
						pw.print("|");
						pw.print(dateFormat2.format(currentTime));
						pw.println();
						pw.println();
						
						member_ids.add(member_id);
					}
				});
			}
			
			if (member_ids.size() > 0) {
				try (PreparedStatement pstmt = conn.prepareStatement("update member set is_print = 1 where member_id = ?")) {
					for (String member_id : member_ids) {
						pstmt.setLong(1, Long.parseLong(member_id));
						pstmt.executeUpdate();
					}
				}
				conn.commit();
			}
			
			return filename + C.char_31 + Stringify.getTimestamp(currentTime);
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}
	
	public String getDataForUpdate(FunctionItem fi, long memberId) throws Exception {
		StringBuilder sb = new StringBuilder(50);
		
		MemberModel m = this.getData(fi, memberId);
		
		sb.append(Stringify.getString(m.member_name)).append(C.char_31)
		.append(Stringify.getString(m.member_login_id)).append(C.char_31)
		.append(Stringify.getString(m.phone_no)).append(C.char_31)
		.append(Stringify.getString(m.arc_no)).append(C.char_31)
		.append(Stringify.getDate(m.arc_expire_date)).append(C.char_31)
		.append(Stringify.getString(m.address)).append(C.char_31)
		.append(Stringify.getDate(m.birthday)).append(C.char_31)
		.append(Stringify.getTimestamp(m.lm_time)).append(C.char_31)
		.append(Stringify.getString(m.arc_photo_basename)).append(C.char_31)
		.append(Stringify.getString(m.signature_photo_basename)).append(C.char_31)
		.append(Stringify.getString(m.email)).append(C.char_31)
		.append(formatArcPhotoUrl(fi, memberId, m.arc_photo_basename)).append(C.char_31)
		.append(formatSignaturePhotoUrl(fi, memberId, m.signature_photo_basename)).append(C.char_31)
		.append(m.signature_need_fix ? 1 : 0).append(C.char_31)
		.append(String.valueOf(m.sex.getId()));
		
		return sb.toString();
	}
	
	public Timestamp updateMemberStatus(FunctionItem fi, long member_id, Timestamp lm_time, int status_id) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("select lm_time, status_id from member where member_id = ? for update");
		PreparedStatement pstmtUpdate = conn.prepareStatement("update member set status_id = ?, lm_time = ?, lm_user = ? where member_id = ?");
		try {
			Timestamp old_lm_time;
			int old_status_id;
			pstmt.setLong(1, member_id);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					old_lm_time = r.getTimestamp(1);
					old_status_id = r.getInt(2);
				} else
					throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id));
			} finally {
				r.close();
			}
			
			if (!old_lm_time.equals(lm_time))
				throw new Exception(String.format(C.data_already_updated_by_another_user, "member_id = " + member_id));
			if (status_id == old_status_id)
				throw new Exception(C.nothing_to_update);
			
			lm_time = fi.getConnection().getCurrentTime();
			
			pstmtUpdate.setInt(1, status_id);
			pstmtUpdate.setTimestamp(2, lm_time);
			pstmtUpdate.setString(3, fi.getSessionInfo().getUserName());
			pstmtUpdate.setLong(4, member_id);
			pstmtUpdate.executeUpdate();
			
			return lm_time;
		} finally {
			pstmt.close();
		}
	}
	
	private String insert(FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		MemberModel m = new MemberModel();
		m.member_name = Helper.getString(params, C.member_name, true).toUpperCase();
		m.phone_no = Helper.getString(params, C.phone_no, true);
		m.arc_no = Helper.getString(params, C.arc_no, true).toUpperCase();
		m.arc_expire_date = Helper.getTimestamp(params, C.arc_expire_date, true);
		m.address = Helper.getString(params, C.address, true);
		m.birthday = Helper.getTimestamp(params, C.birthday, true);
		m.email = Helper.getString(params, C.email, false);
		m.arc_photo_basename = Helper.getString(params, C.arc_photo_basename, true);
		m.signature_photo_basename = Helper.getString(params, C.signature_photo_basename, false);
		m.sex = MemberSex.get(Helper.getInt(params, C.sex_id, true));
		
		Connection conn = fi.getConnection().getConnection();
		try {
			insert(fi, m);

			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.member_id, C.columnTypeNumber, C.operationEqual, String.valueOf(m.member_id), null));
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
		MemberModel m = new MemberModel();
		m.member_id = Helper.getLong(params, C.member_id, true);
		m.lm_time = Helper.getTimestamp(params, C.lm_time, true);
		m.member_name = Helper.getString(params, C.member_name, true).toUpperCase();
		m.phone_no = Helper.getString(params, C.phone_no, true);
		m.arc_no = Helper.getString(params, C.arc_no, true).toUpperCase();
		m.arc_expire_date = Helper.getTimestamp(params, C.arc_expire_date, true);
		m.address = Helper.getString(params, C.address, true);
		m.birthday = Helper.getTimestamp(params, C.birthday, true);
		m.email = Helper.getString(params, C.email, false);
		m.arc_photo_basename = Helper.getString(params, C.arc_photo_basename, true);
		m.signature_photo_basename = Helper.getString(params, C.signature_photo_basename, false);
		m.sex = MemberSex.get(Helper.getInt(params, C.sex_id, true));
		
		Connection conn = fi.getConnection().getConnection();
		try {
			update(fi, m);

			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.member_id, C.columnTypeNumber, C.operationEqual, String.valueOf(m.member_id), null));
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
		MemberModel m = new MemberModel();
		m.member_id = Helper.getLong(params, C.member_id, true);
		m.lm_time = Helper.getTimestamp(params, C.lm_time, true);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			delete(fi, m);

			conn.commit();
			
			return "1";
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}
	
	public void insert(FunctionItem fi, MemberModel m) throws Exception {
		if (!arcNoPattern.matcher(m.arc_no).matches()) {
			throw new Exception("arc no length must be 10, with 2 alphabets and 8 digits");
		}
		
		String[] phoneNos = StringUtils.split(m.phone_no, '.');
		for (String s : phoneNos) {
			if (s.length() > 0) {
				if (!phoneNoPattern.matcher(s).matches()) {
					throw new Exception("phone no [" + s + "] must be all digits with maximum length is 10");
				}
			}
		}
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("insert into member (member_id, member_name, phone_no, arc_no, arc_expire_date, address, birthday, lm_time, lm_user, email, arc_photo_basename, signature_photo_basename, signature_need_fix, sex_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		try {
			m.member_id = fi.getConnection().getSeq(C.member_id, true);
			m.lm_time = fi.getConnection().getCurrentTime();
			m.lm_user = fi.getSessionInfo().getUserName();
			
			moveImage(fi, fi.getConnection().getGlobalConfig(C.member, C.arc_photo_directory), m.arc_photo_basename, m.member_id);
			
			if (m.signature_photo_basename != null) {
				moveImage(fi, fi.getConnection().getGlobalConfig(C.member, C.signature_photo_directory), m.signature_photo_basename, m.member_id);
				m.signature_need_fix = false;
			} else {
				m.signature_need_fix = true;
			}
			
			pstmt.setLong(1, m.member_id);
			pstmt.setString(2, m.member_name);
			pstmt.setString(3, m.phone_no);
			pstmt.setString(4, m.arc_no);
			pstmt.setTimestamp(5, m.arc_expire_date);
			pstmt.setString(6, m.address);
			pstmt.setTimestamp(7, m.birthday);
			pstmt.setTimestamp(8, m.lm_time);
			pstmt.setString(9, m.lm_user);
			pstmt.setString(10, m.email);
			pstmt.setString(11, m.arc_photo_basename);
			pstmt.setString(12, m.signature_photo_basename);
			pstmt.setInt(13, m.signature_need_fix ? 1 : 0);
			pstmt.setInt(14, m.sex.getId());
			pstmt.executeUpdate();
			
			fi.getConnection().logHistory(IndogoTable.member, m.member_id, HistoryAction.add, null, m.lm_time, m.lm_user,
					new HistoryData(C.member_name, null, m.member_name),
					new HistoryData(C.phone_no, null, m.phone_no),
					new HistoryData(C.arc_no, null, m.arc_no),
					new HistoryData(C.arc_expire_date, null, Stringify.getTimestamp(m.arc_expire_date, null)),
					new HistoryData(C.address, null, m.address),
					new HistoryData(C.birthday, null, Stringify.getTimestamp(m.birthday, null)),
					new HistoryData(C.email, null, m.email),
					new HistoryData(C.arc_photo_basename, null, m.arc_photo_basename),
					new HistoryData(C.signature_photo_basename, null, m.signature_photo_basename),
					new HistoryData(C.sex_id, null, String.valueOf(m.sex.getId()))
					);
		} finally {
			pstmt.close();
		}
	}
	
	private String saveImage(FunctionItem fi, InputStream imageSource, String remoteUri, String deleteOldFilename) throws Exception {
		int userRowId = fi.getSessionInfo().getUserRowId();
		String baseName = UUID.randomUUID().toString().replaceAll("-", "");
		
		BufferedImage bufferedImage = ImageIO.read(imageSource);
		File file = new File(fi.getTempFolder(), baseName);
		if (!ImageIO.write(bufferedImage, "png", file)) {
			throw new Exception("cannot save photo");
		}
		
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			manager.init();
			
			FileObject localFile = manager.resolveFile(file.getAbsolutePath());
			
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setStrictHostKeyChecking(opts, "no");
			builder.setUserDirIsRoot(opts, true);
			builder.setTimeout(opts, 10000);
			FileObject remoteBase = manager.resolveFile(remoteUri, opts);
			FileObject remoteFile = manager.resolveFile(remoteBase, Stringify.concat("temp/", String.valueOf(userRowId), "/", baseName, "_original.png"), opts);
			localFile.moveTo(remoteFile);
			
			if (!Helper.isNullOrEmpty(deleteOldFilename)) {
				remoteFile = manager.resolveFile(Stringify.concat(remoteUri, "/temp/", String.valueOf(userRowId), "/", deleteOldFilename, "_original.png"), opts);
				try {
					remoteFile.delete();
				} catch (Exception ignore) {}
			}
			
			return baseName;
		} finally {
			manager.close();
		}
	}
	
	private void moveImage(FunctionItem fi, String remoteUri, String baseName, long member_id) throws Exception {
		int userRowId = fi.getSessionInfo().getUserRowId();
		long level = member_id % 5000;
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			manager.init();
			
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setStrictHostKeyChecking(opts, "no");
			builder.setUserDirIsRoot(opts, true);
			builder.setTimeout(opts, 10000);
			FileObject fromFile = manager.resolveFile(Stringify.concat(remoteUri, "/temp/", String.valueOf(userRowId), "/", baseName, "_original.png"), opts);
			FileObject toBase = manager.resolveFile(Stringify.concat(remoteUri, "/", String.valueOf(level), "/", String.valueOf(member_id)), opts);
			toBase.createFolder();
			FileObject toFile = manager.resolveFile(toBase, Stringify.concat(baseName, "_original.png"), opts);
			fromFile.moveTo(toFile);
		} finally {
			manager.close();
		}
	}
	
	public byte[] readImage(FunctionItem fi, String remoteUri, String baseName, long member_id) throws Exception {
		long level = member_id % 5000;
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			manager.init();
			
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setStrictHostKeyChecking(opts, "no");
			builder.setUserDirIsRoot(opts, true);
			builder.setTimeout(opts, 10000);
			FileObject file = manager.resolveFile(Stringify.concat(remoteUri, "/", String.valueOf(level), "/", String.valueOf(member_id), "/", baseName, "_original.png"), opts);
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
	
	public void update(FunctionItem fi, MemberModel m, boolean throwOnNoNewData) throws Exception {
		if (!arcNoPattern.matcher(m.arc_no).matches()) {
			throw new Exception("arc no length must be 10, with 2 alphabets and 8 digits");
		}
		
		String[] phoneNos = StringUtils.split(m.phone_no, '.');
		for (String s : phoneNos) {
			if (s.length() > 0) {
				if (!phoneNoPattern.matcher(s).matches()) {
					throw new Exception("phone no [" + s + "] must be all digits with maximum length is 10");
				}
			}
		}
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmtLock = conn.prepareStatement("select member_name, phone_no, arc_no, arc_expire_date, address, birthday, lm_time, arc_photo_basename, email, signature_photo_basename, signature_need_fix, sex_id from member where member_id = ? for update");
		PreparedStatement pstmt = conn.prepareStatement("update member set member_name = ?, phone_no = ?, arc_no = ?, arc_expire_date = ?, address = ?, birthday = ?, lm_time = ?, arc_photo_basename = ?, lm_user = ?, email = ?, signature_photo_basename = ?, signature_need_fix = ?, sex_id = ? where member_id = ?");
		try {
			String memberName;
			String phoneNo;
			String arcNo;
			Timestamp arcExpireDate;
			String address;
			Timestamp birthday;
			Timestamp lmTime;
			String arcPhotoBasename;
			String email;
			String signaturePhotoBasename;
			int signature_need_fix;
			int sex_id;
			
			pstmtLock.setLong(1, m.member_id);
			ResultSet r = pstmtLock.executeQuery();
			try {
				if (r.next()) {
					memberName = r.getString(1);
					phoneNo = r.getString(2);
					arcNo = r.getString(3);
					arcExpireDate = r.getTimestamp(4);
					address = r.getString(5);
					birthday = r.getTimestamp(6);
					lmTime = r.getTimestamp(7);
					arcPhotoBasename = r.getString(8);
					email = r.getString(9);
					signaturePhotoBasename = r.getString(10);
					signature_need_fix = r.getInt(11);
					sex_id = r.getInt(12);
				} else {
					throw new Exception(String.format(C.data_not_exist, "member_id = " + m.member_id));
				}
			} finally {
				r.close();
			}
			
			if (lmTime.getTime() != m.lm_time.getTime()) {
				throw new Exception(String.format(C.data_already_updated_by_another_user, "member_id = " + m.member_id));
			}
			
			m.lm_time = fi.getConnection().getCurrentTime();
			m.lm_user = fi.getSessionInfo().getUserName();
			
			List<HistoryData> h = new ArrayList<>();
			if (!Helper.isEquals(m.member_name, memberName))
				h.add(new HistoryData(C.member_name, memberName, m.member_name));
			if (!Helper.isEquals(m.phone_no, phoneNo))
				h.add(new HistoryData(C.phone_no, phoneNo, m.phone_no));
			if (!Helper.isEquals(m.arc_no, arcNo))
				h.add(new HistoryData(C.arc_no, arcNo, m.arc_no));
			if (!Helper.isEquals(m.arc_expire_date, arcExpireDate))
				h.add(new HistoryData(C.arc_expire_date, Helper.toNullString(arcExpireDate), Helper.toNullString(m.arc_expire_date)));
			if (!Helper.isEquals(m.address, address))
				h.add(new HistoryData(C.address, address, m.address));
			if (!Helper.isEquals(m.birthday, birthday))
				h.add(new HistoryData(C.birthday, Helper.toNullString(birthday), Helper.toNullString(m.birthday)));
			if (!Helper.isEquals(m.arc_photo_basename, arcPhotoBasename)) {
				moveImage(fi, fi.getConnection().getGlobalConfig(C.member, C.arc_photo_directory), m.arc_photo_basename, m.member_id);
				h.add(new HistoryData(C.arc_photo_basename, arcPhotoBasename, m.arc_photo_basename));
			}
			if (!Helper.isEquals(m.signature_photo_basename, signaturePhotoBasename)) {
				if (m.signature_photo_basename != null) {
					moveImage(fi, fi.getConnection().getGlobalConfig(C.member, C.signature_photo_directory), m.signature_photo_basename, m.member_id);
				}
				h.add(new HistoryData(C.signature_photo_basename, signaturePhotoBasename, m.signature_photo_basename));
				signature_need_fix = 0;
			}
			if (!Helper.isEquals(m.email, email)) {
				h.add(new HistoryData(C.email, email, m.email));
			}
			if (m.sex.getId() != sex_id) {
				h.add(new HistoryData(C.sex_id, String.valueOf(sex_id), String.valueOf(m.sex.getId())));
			}
			
			if (h.size() == 0) {
				if (throwOnNoNewData)
					throw new Exception(C.nothing_to_update);
				else
					return;
			}
			
			pstmt.setString(1, m.member_name);
			pstmt.setString(2, m.phone_no);
			pstmt.setString(3, m.arc_no);
			pstmt.setTimestamp(4, m.arc_expire_date);
			pstmt.setString(5, m.address);
			pstmt.setTimestamp(6, m.birthday);
			pstmt.setTimestamp(7, m.lm_time);
			pstmt.setString(8, m.arc_photo_basename);
			pstmt.setString(9, m.lm_user);
			pstmt.setString(10, m.email);
			pstmt.setString(11, m.signature_photo_basename);
			pstmt.setInt(12, signature_need_fix);
			pstmt.setInt(13, m.sex.getId());
			pstmt.setLong(14, m.member_id);
			pstmt.executeUpdate();
			
			fi.getConnection().logHistory(IndogoTable.member, m.member_id, HistoryAction.update, null, m.lm_time, m.lm_user, Helper.toHistoryDataArray(h));
		} finally {
			pstmtLock.close();
			pstmt.close();
		}
	}
	
	public void update(FunctionItem fi, MemberModel m) throws Exception {
		update(fi, m, true);
	}
	
	public void delete(FunctionItem fi, MemberModel m) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmtLock = conn.prepareStatement("select member_name, member_login_id, member_password, phone_no, arc_no, arc_expire_date, address, birthday, lm_time, email from member where member_id = ? for update");
		PreparedStatement pstmt = conn.prepareStatement("delete from member where member_id = ?");
		PreparedStatement pstmtDeleteRecipient = conn.prepareStatement("delete from member_recipient where member_id = ?");
		try {
			String memberName;
			String memberLoginId;
			String memberPassword;
			String phoneNo;
			String arcNo;
			Timestamp arcExpireDate;
			String address;
			Timestamp birthday;
			Timestamp lmTime;
			String email;
			
			pstmtLock.setLong(1, m.member_id);
			ResultSet r = pstmtLock.executeQuery();
			try {
				if (r.next()) {
					memberName = r.getString(1);
					memberLoginId = r.getString(2);
					memberPassword = r.getString(3);
					phoneNo = r.getString(4);
					arcNo = r.getString(5);
					arcExpireDate = r.getTimestamp(6);
					address = r.getString(7);
					birthday = r.getTimestamp(8);
					lmTime = r.getTimestamp(9);
					email = r.getString(10);
				} else {
					throw new Exception(String.format(C.data_not_exist, "member_id = " + m.member_id));
				}
			} finally {
				r.close();
			}
			
			if (lmTime.getTime() != m.lm_time.getTime()) {
				throw new Exception(String.format(C.data_already_updated_by_another_user, "member_id = " + m.member_id));
			}
			
			pstmtDeleteRecipient.setLong(1, m.member_id);
			pstmtDeleteRecipient.executeUpdate();
			
			pstmt.setLong(1, m.member_id);
			pstmt.executeUpdate();
			
			List<HistoryData> h = new ArrayList<>();
			h.add(new HistoryData(C.member_name, memberName, null));
			h.add(new HistoryData(C.member_login_id, memberLoginId, null));
			h.add(new HistoryData(C.member_password, memberPassword, null));
			h.add(new HistoryData(C.phone_no, phoneNo, null));
			h.add(new HistoryData(C.arc_no, arcNo, null));
			h.add(new HistoryData(C.arc_expire_date, Helper.toNullString(arcExpireDate), null));
			h.add(new HistoryData(C.address, address, null));
			h.add(new HistoryData(C.birthday, Helper.toNullString(birthday), null));
			h.add(new HistoryData(C.email, email, null));
			
			fi.getConnection().logHistory(IndogoTable.member, m.member_id, HistoryAction.delete, null, fi.getConnection().getCurrentTime(), fi.getSessionInfo().getUserName(), Helper.toHistoryDataArray(h));
		} finally {
			pstmtLock.close();
			pstmt.close();
			pstmtDeleteRecipient.close();
		}
	}

	private String getRecipients(FunctionItem fi, long memberId) throws Exception {
		PreparedStatement pstmt = fi.getConnection().getConnection().prepareStatement("select recipient_id, recipient_name, bank_acc, bank_code, bank_name, lm_time, is_verified, is_hidden, recipient_name_2 from member_recipient_v where member_id = ? order by recipient_name");
		try {
			pstmt.setLong(1, memberId);
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				StringBuilder sb = new StringBuilder(50);
				while (r.next()) {
					sb.append(r.getInt(1)).append(C.char_31)
					.append(r.getString(2)).append(C.char_31)
					.append(r.getString(3)).append(C.char_31)
					.append(r.getString(4)).append(C.char_31)
					.append(r.getString(5)).append(C.char_31)
					.append(r.getTimestamp(6)).append(C.char_31)
					.append(r.getInt(7)).append(C.char_31)
					.append(r.getInt(8)).append(C.char_31)
					.append(r.getString(9)).append(C.char_30);
				}
				if (sb.length() > 0)
					sb.delete(sb.length() - 1, sb.length());
				return sb.toString();
			}
		} finally {
			pstmt.close();
		}
	}
	
	public RecipientModel getRecipient(FunctionItem fi, long memberId, int recipientId) throws Exception {
		PreparedStatement pstmt = fi.getConnection().getConnection().prepareStatement("select recipient_name, bank_acc, bank_code, bank_name, lm_time, lm_user, is_verified, is_hidden from member_recipient_v where member_id = ? and recipient_id = ?");
		try {
			pstmt.setLong(1, memberId);
			pstmt.setInt(2, recipientId);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					RecipientModel m = new RecipientModel();
					m.member_id = memberId;
					m.recipient_id = recipientId;
					m.recipient_name = r.getString(1);
					m.bank_acc = r.getString(2);
					m.bank_code = r.getString(3);
					m.bank_name = r.getString(4);
					m.lm_time = r.getTimestamp(5);
					m.lm_user = r.getString(6);
					m.is_verified = r.getInt(7) == 1;
					m.is_hidden = r.getInt(8) == 1;
					return m;
				} else {
					throw new Exception(String.format(C.data_not_exist, "member_id = " + memberId + ", recipient_id = " + recipientId));
				}
			} finally {
				r.close();
			}
		} finally {
			pstmt.close();
		}
	}
	
	public MemberModel getData(FunctionItem fi, long memberId) throws Exception {
		PreparedStatement pstmt = fi.getConnection().getConnection().prepareStatement("select member_name, member_login_id, phone_no, arc_no, arc_expire_date, address, birthday, lm_time, arc_photo_basename, email, signature_photo_basename, status_id, signature_need_fix, sex_id, app_phone_no from member where member_id = ?");
		try {
			pstmt.setLong(1, memberId);
			try (ResultSet r = pstmt.executeQuery()) {
				if (r.next()) {
					MemberModel m = new MemberModel();
					m.member_id = memberId;
					m.member_name = r.getString(1);
					m.member_login_id = r.getString(2);
					m.phone_no = r.getString(3);
					m.arc_no = r.getString(4);
					m.arc_expire_date = r.getTimestamp(5);
					m.address = r.getString(6);
					m.birthday = r.getTimestamp(7);
					m.lm_time = r.getTimestamp(8);
					m.arc_photo_basename = r.getString(9);
					m.email = r.getString(10);
					m.signature_photo_basename = r.getString(11);
					m.status = MemberStatus.get(r.getInt(12));
					m.signature_need_fix = r.getInt(13) == 1;
					m.sex = MemberSex.get(r.getInt(14));
					m.app_phone_no = r.getString(15);
					return m;
				} else {
					throw new Exception(String.format(C.data_not_exist, "member_id = " + memberId));
				}
			}
		} finally {
			pstmt.close();
		}
	}
	
	public void insertRecipient(FunctionItem fi, RecipientModel recipientModel) throws Exception {
		if (recipientModel.member_id == null)
			throw new NullPointerException(C.member_id);
		
		PreparedStatement pstmt = fi.getConnection().getConnection().prepareStatement("insert into member_recipient (member_id, recipient_id, recipient_name, bank_code, bank_acc, lm_time, lm_user, is_verified, recipient_name_2) values (?,?,?,?,?,?,?,?,?)");
		try {
			recipientModel.recipient_id = (int)fi.getConnection().getSeq(C.member_id + "_" + recipientModel.member_id, true);
			recipientModel.lm_time = fi.getConnection().getCurrentTime();
			recipientModel.lm_user = fi.getSessionInfo().getUserName();
			
			pstmt.setLong(1, recipientModel.member_id);
			pstmt.setInt(2, recipientModel.recipient_id);
			pstmt.setString(3, recipientModel.recipient_name);
			pstmt.setString(4, recipientModel.bank_code);
			pstmt.setString(5, recipientModel.bank_acc);
			pstmt.setTimestamp(6, recipientModel.lm_time);
			pstmt.setString(7, recipientModel.lm_user);
			pstmt.setInt(8, recipientModel.is_verified ? 1 : 0);
			pstmt.setString(9, recipientModel.recipient_name);
			pstmt.executeUpdate();
			
			fi.getConnection().logHistory(IndogoTable.member_recipient, recipientModel.member_id, HistoryAction.add, null, recipientModel.lm_time, recipientModel.lm_user,
					new HistoryData(C.member_id, null, String.valueOf(recipientModel.member_id)),
					new HistoryData(C.recipient_id, null, String.valueOf(recipientModel.recipient_id)),
					new HistoryData(C.recipient_name, null, recipientModel.recipient_name),
					new HistoryData(C.bank_acc, null, recipientModel.bank_acc),
					new HistoryData(C.bank_code, null, recipientModel.bank_code),
					new HistoryData(C.is_verified, null, recipientModel.is_verified ? "1" : "0"));
		} finally {
			pstmt.close();
		}
	}
	
	public void updateRecipient(FunctionItem fi, RecipientModel recipientModel) throws Exception {
		if (recipientModel.member_id == null)
			throw new NullPointerException(C.member_id);
		if (recipientModel.recipient_id == null)
			throw new NullPointerException(C.recipient_id);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmtLock = conn.prepareStatement("select recipient_name, bank_acc, bank_code, lm_time, lm_user, is_verified from member_recipient where member_id = ? and recipient_id = ? for update");
		PreparedStatement pstmtUpdate = conn.prepareStatement("update member_recipient set recipient_name = ?, bank_acc = ?, bank_code = ?, lm_time = ?, lm_user = ?, is_verified = ? where member_id = ? and recipient_id = ?");
		try {
			String old_recipient_name;
			String old_bank_acc;
			String old_bank_code;
			Timestamp old_lm_time;
			String old_lm_user;
			boolean old_is_verified;
			pstmtLock.setLong(1, recipientModel.member_id);
			pstmtLock.setInt(2, recipientModel.recipient_id);
			ResultSet r = pstmtLock.executeQuery();
			try {
				if (r.next()) {
					old_recipient_name = r.getString(1);
					old_bank_acc = r.getString(2);
					old_bank_code = r.getString(3);
					old_lm_time = r.getTimestamp(4);
					old_lm_user = r.getString(5);
					old_is_verified = r.getInt(6) == 1;
				} else
					throw new Exception(String.format(C.data_not_exist, "member_id = " + recipientModel.member_id + ", recipient_id = " + recipientModel.recipient_id));
			} finally {
				r.close();
			}
			
			if (old_lm_time.getTime() != recipientModel.lm_time.getTime())
				throw new Exception(String.format(C.data_already_updated_by_another_user, "member_id = " + recipientModel.member_id + ", recipient_id = " + recipientModel.recipient_id));
			
			List<HistoryData> historys = new ArrayList<>();
			if (!old_recipient_name.equalsIgnoreCase(recipientModel.recipient_name)) {
				recipientModel.is_verified = false;
				historys.add(new HistoryData(C.recipient_name, old_recipient_name, recipientModel.recipient_name));
			}
			if (!old_bank_acc.equals(recipientModel.bank_acc)) {
				recipientModel.is_verified = false;
				historys.add(new HistoryData(C.bank_acc, old_bank_acc, recipientModel.bank_acc));
			}
			if (!old_bank_code.equals(recipientModel.bank_code)) {
				recipientModel.is_verified = false;
				historys.add(new HistoryData(C.bank_acc, old_bank_code, recipientModel.bank_code));
			}
			if (old_is_verified != recipientModel.is_verified)
				historys.add(new HistoryData(C.is_verified, old_is_verified ? "1" : "0", recipientModel.is_verified ? "1" : "0"));
			
			if (historys.size() == 0)
				return;
			
			recipientModel.lm_time = fi.getConnection().getCurrentTime();
			recipientModel.lm_user = fi.getSessionInfo().getUserName();
			
			historys.add(new HistoryData(C.lm_time, Stringify.getTimestamp(old_lm_time), Stringify.getTimestamp(recipientModel.lm_time)));
			historys.add(new HistoryData(C.lm_user, old_lm_user, recipientModel.lm_user));
			
			pstmtUpdate.setString(1, recipientModel.recipient_name);
			pstmtUpdate.setString(2, recipientModel.bank_acc);
			pstmtUpdate.setString(3, recipientModel.bank_code);
			pstmtUpdate.setTimestamp(4, recipientModel.lm_time);
			pstmtUpdate.setString(5, recipientModel.lm_user);
			pstmtUpdate.setInt(6, recipientModel.is_verified ? 1 : 0);
			pstmtUpdate.setLong(7, recipientModel.member_id);
			pstmtUpdate.setInt(8, recipientModel.recipient_id);
			pstmtUpdate.executeUpdate();
			
			fi.getConnection().logHistory(IndogoTable.member_recipient, recipientModel.member_id, HistoryAction.update, null, recipientModel.lm_time, recipientModel.lm_user, historys.toArray(new HistoryData[0]));
		} finally {
			pstmtLock.close();
		}
	}
	
	public VerifyResult verifyRecipient(FunctionItem fi, long member_id, int recipient_id, boolean auto_update_name, TransferMoneyThroughBank bank) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmtLock = conn.prepareStatement("select is_verified, recipient_name, bank_code, bank_acc from member_recipient where member_id = ? and recipient_id = ? for update");
		PreparedStatement pstmt = fi.getConnection().getConnection().prepareStatement("update member_recipient set is_verified = 1, recipient_name = ? where member_id = ? and recipient_id = ?");
		try {
			boolean is_verified;
			String recipient_name, bank_code, bank_acc;
			pstmtLock.setLong(1, member_id);
			pstmtLock.setInt(2, recipient_id);
			ResultSet r = pstmtLock.executeQuery();
			try {
				if (r.next()) {
					is_verified = r.getInt(1) == 1;
					recipient_name = r.getString(2);
					bank_code = r.getString(3);
					bank_acc = r.getString(4);
				} else {
					throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id + ", recipient_id = " + recipient_id));
				}
			} finally {
				r.close();
			}
			
			if (is_verified) {
				VerifyResult verifyResult = new VerifyResult();
				verifyResult.already_verified = true;
				verifyResult.recipient_name = recipient_name;
				verifyResult.bni_recipient_name = C.emptyString;
				return verifyResult;
			}
			
			if (Boolean.parseBoolean(fi.getConnection().getGlobalConfig(C.bni_h2h, C.verify_recipient))) {
				String bank_recipient_name;
				switch (bank) {
					case BNI:
						try (BniHostToHost bni = new BniHostToHost(fi.getConnection())) {
							bank_recipient_name = bni.getAccountName(bank_code, bank_acc);
						}
						break;
					case BRI:
						BriHostToHost bri = new BriHostToHost(fi.getConnection());
						String token = bri.requestToken();
						bank_recipient_name = bri.inquiryAccount(token, bank_code, bank_acc);
						break;
					default:
						throw new Exception(String.format(C.function_not_yet_implemented, "verifyRecipient via bank " + bank));
				}
				
				if (auto_update_name) {
					if (!recipient_name.startsWith(bank_recipient_name)) {
						recipient_name = bank_recipient_name;
					}
				} else if (!recipient_name.equals(bank_recipient_name) && !recipient_name.startsWith(bank_recipient_name)) {
					VerifyResult verifyResult = new VerifyResult();
					verifyResult.bni_name_check_failed = true;
					verifyResult.recipient_name = recipient_name;
					verifyResult.bni_recipient_name = bank_recipient_name;
					return verifyResult;
				}
			}
			
			pstmt.setString(1, recipient_name);
			pstmt.setLong(2, member_id);
			pstmt.setInt(3, recipient_id);
			pstmt.executeUpdate();
			
			VerifyResult verifyResult = new VerifyResult();
			verifyResult.recipient_name = recipient_name;
			return verifyResult;
		} finally {
			pstmtLock.close();
			pstmt.close();
		}
	}
	
	public void hideRecipient(FunctionItem fi, long member_id, int recipient_id, boolean is_hidden) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmtLock = conn.prepareStatement("select is_hidden from member_recipient where member_id = ? and recipient_id = ? for update");
		PreparedStatement pstmt = fi.getConnection().getConnection().prepareStatement("update member_recipient set is_hidden = ? where member_id = ? and recipient_id = ?");
		try {
			boolean old_is_hidden;
			pstmtLock.setLong(1, member_id);
			pstmtLock.setInt(2, recipient_id);
			ResultSet r = pstmtLock.executeQuery();
			try {
				if (r.next()) {
					old_is_hidden = r.getInt(1) == 1;
				} else {
					throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id + ", recipient_id = " + recipient_id));
				}
			} finally {
				r.close();
			}
			
			if (old_is_hidden == is_hidden) {
				return;
			}
			
			pstmt.setInt(1, is_hidden ? 1 : 0);
			pstmt.setLong(2, member_id);
			pstmt.setInt(3, recipient_id);
			pstmt.executeUpdate();
			
			fi.getConnection().logHistory(IndogoTable.member_recipient, member_id, HistoryAction.update, null, fi.getConnection().getCurrentTime(), fi.getSessionInfo().getUserName(),
					new HistoryData(C.recipient_id, String.valueOf(recipient_id), String.valueOf(recipient_id)),
					new HistoryData(C.is_verified, old_is_hidden ? "1" : "0", is_hidden ? "1" : "0"));
		} finally {
			pstmtLock.close();
			pstmt.close();
		}
	}
	
//	public void batchUpdateRecipients(FunctionItem fi, long memberId, RecipientModel[] models) throws Exception {
//		Connection conn = fi.getConnection().getConnection();
//		PreparedStatement pstmt = conn.prepareStatement("select recipient_name, bank_acc, bank_code, lm_time from member_recipient where member_id = ? and recipient_id = ? for update");
//		PreparedStatement pstmtInsert = conn.prepareStatement("insert into member_recipient (member_id, recipient_id, recipient_name, bank_acc, bank_code, lm_time, lm_user) values (?,?,?,?,?,?,?)");
//		PreparedStatement pstmtUpdate = conn.prepareStatement("update member_recipient set recipient_name = ?, bank_acc = ?, bank_code = ?, lm_time = ?, lm_user = ? where member_id = ? and recipient_id = ?");
//		PreparedStatement pstmtDelete = conn.prepareStatement("delete from member_recipient where member_id = ? and recipient_id = ?");
//		try {
//			Timestamp currentTime = fi.getConnection().getCurrentTime();
//			String lmUser = fi.getSessionInfo().getUserName();
//			
//			HashMap<Integer, String[]> map = new HashMap<>();
//			for (String row : rows) {
//				String[] cells = StringUtils.splitPreserveAllTokens(row, C.char_31);
//				String actionType = cells[0];
//				int recipientId;
//				if (actionType.equals(C.insert)) {
//					recipientId = (int)fi.getConnection().getSeq(C.member_id + "_" + memberId, true);
//				} else
//					recipientId = Integer.parseInt(cells[1]);
//				map.put(recipientId, cells);
//			}
//			int[] recipientIds = new int[map.keySet().size()];
//			int i = 0;
//			for (int recipientId : map.keySet()) {
//				recipientIds[i] = recipientId;
//				i++;
//			}
//			Arrays.sort(recipientIds);
//			
//			for (int recipientId : recipientIds) {
//				String[] cells = map.get(recipientId);
//				String actionType = cells[0];
//				String recipientName = cells[2];
//				String bankAcc = cells[3];
//				String bankCode = cells[4];
//				String lmTimeString = cells[5];
//				
//				if (actionType.equals(C.update) || actionType.equals(C.delete)) {
//					Timestamp lmTime = new Timestamp(DateFormat.getInstance().parse(lmTimeString).getTime());
//					pstmt.setLong(1, memberId);
//					pstmt.setInt(2, recipientId);
//					ResultSet r = pstmt.executeQuery();
//					try {
//						if (r.next()) {
//							String oldRecipientName = r.getString(1);
//							String oldBankAcc = r.getString(2);
//							String oldBankCode = r.getString(3);
//							Timestamp oldLmTime = r.getTimestamp(4);
//							if (!lmTime.equals(oldLmTime))
//								throw new Exception(String.format(C.data_already_updated_by_another_user, "member_id = " + memberId + ", recipient_id = " + recipientId));
//							
//							if (actionType.equals(C.update)) {
//								List<HistoryData> history = new ArrayList<>();
//								history.add(new HistoryData(C.member_id, String.valueOf(memberId), String.valueOf(memberId)));
//								history.add(new HistoryData(C.recipient_id, String.valueOf(recipientId), String.valueOf(recipientId)));
//								if (!Helper.isEquals(oldRecipientName, recipientName))
//									history.add(new HistoryData(C.recipient_name, oldRecipientName, recipientName));
//								if (!Helper.isEquals(oldBankAcc, bankAcc))
//									history.add(new HistoryData(C.bank_acc, oldBankAcc, bankAcc));
//								if (!Helper.isEquals(oldBankCode, bankCode))
//									history.add(new HistoryData(C.bank_code, oldBankCode, bankCode));
//
//								if (history.size() > 2) {
//									pstmtUpdate.setString(1, recipientName);
//									pstmtUpdate.setString(2, bankAcc);
//									pstmtUpdate.setString(3, bankCode);
//									pstmtUpdate.setTimestamp(4, currentTime);
//									pstmtUpdate.setString(5, lmUser);
//									pstmtUpdate.setLong(6, memberId);
//									pstmtUpdate.setInt(7, recipientId);
//									pstmtUpdate.executeUpdate();
//									
//									fi.getConnection().logHistory(IndogoTable.member_recipient, memberId, HistoryAction.update, null, currentTime, fi.getSessionInfo().getUserName(), Helper.toHistoryDataArray(history));
//								}
//							} else if (actionType.equals(C.delete)) {
//								pstmtDelete.setLong(1, memberId);
//								pstmtDelete.setInt(2, recipientId);
//								pstmtDelete.executeUpdate();
//								
//								List<HistoryData> history = new ArrayList<>();
//								history.add(new HistoryData(C.member_id, String.valueOf(memberId), null));
//								history.add(new HistoryData(C.recipient_id, String.valueOf(recipientId), null));
//								history.add(new HistoryData(C.recipient_name, oldRecipientName, null));
//								history.add(new HistoryData(C.bank_acc, oldBankAcc, null));
//								history.add(new HistoryData(C.bank_code, oldBankCode, null));
//								fi.getConnection().logHistory(IndogoTable.member_recipient, memberId, HistoryAction.delete, null, currentTime, fi.getSessionInfo().getUserName(), Helper.toHistoryDataArray(history));
//							}
//						} else {
//							throw new Exception(String.format(C.data_not_exist, "member_id = " + memberId + ", recipient_id = " + recipientId));
//						}
//					} finally {
//						r.close();
//					}
//				} else if (actionType.equals(C.insert)) {
//					pstmtInsert.setLong(1, memberId);
//					pstmtInsert.setInt(2, recipientId);
//					pstmtInsert.setString(3, recipientName);
//					pstmtInsert.setString(4, bankAcc);
//					pstmtInsert.setString(5, bankCode);
//					pstmtInsert.setTimestamp(6, currentTime);
//					pstmtInsert.setString(7, lmUser);
//					pstmtInsert.executeUpdate();
//					
//					fi.getConnection().logHistory(IndogoTable.member_recipient, memberId, HistoryAction.add, null, currentTime, lmUser,
//							new HistoryData(C.member_id, null, String.valueOf(memberId)),
//							new HistoryData(C.recipient_id, null, String.valueOf(recipientId)),
//							new HistoryData(C.recipient_name, null, recipientName),
//							new HistoryData(C.bank_acc, null, bankAcc),
//							new HistoryData(C.bank_code, null, bankCode));
//				}
//			}
//			
//			conn.commit();
//		} catch (Exception e) {
//			conn.rollback();
//			throw e;
//		} finally {
//			pstmt.close();
//			pstmtInsert.close();
//			pstmtUpdate.close();
//		}
//	}
}
