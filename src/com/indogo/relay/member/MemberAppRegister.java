package com.indogo.relay.member;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.indogo.IndogoTable;
import com.indogo.MemberHistory;
import com.indogo.MemberSex;
import com.indogo.model.member.MemberModel;
import com.lionpig.webui.database.HistoryAction;
import com.lionpig.webui.database.HistoryData;
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

public class MemberAppRegister implements IFunction, ITablePage {

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
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString));
		cols.add(new TablePageColumn(C.app_member_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Name"));
		cols.add(new TablePageColumn(C.app_lm_time, C.columnTypeDateTime, C.columnDirectionDesc, true, false, "Update Time"));
		cols.add(new TablePageColumn(C.arc_no, C.columnTypeString, C.columnDirectionDefault, true, false, "ARC"));
		cols.add(new TablePageColumn(C.app_phone_no, C.columnTypeString, C.columnDirectionDefault, true, false, "Phone No"));
		cols.add(new TablePageColumn(C.status, C.columnTypeString, C.columnDirectionNone, false, true, "Status"));
		cols.add(new TablePageColumn(C.app_register_followup, C.columnTypeString, C.columnDirectionDefault, true, false, "Follow Up"));
		cols.add(new TablePageColumn(C.app_register_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Register Time"));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, false));
		cols.add(new TablePageColumn(C.is_wait_confirm, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_wait_confirm, true, false));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.status_id, true, false));
		cols.add(new TablePageColumn(C.signature_need_fix, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.signature_need_fix, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		int is_wait_confirm = r.unwrap().unwrap().getInt(C.is_wait_confirm);
		String status;
		switch (is_wait_confirm) {
			case 2:
				status = "New";
				break;
			case 4:
				status = "Update";
				break;
			default:
				status = C.emptyString;
				break;
		}
		cols.get(C.app_register_time).setValue(r.getTimestamp(C.app_register_time));
		cols.get(C.app_lm_time).setValue(r.getTimestamp(C.app_lm_time));
		cols.get(C.app_member_name).setValue(r.getString(C.app_member_name));
		cols.get(C.arc_no).setValue(r.getString(C.arc_no));
		cols.get(C.app_phone_no).setValue(r.getString(C.app_phone_no));
		cols.get(C.status).setValue(status);
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
		cols.get(C.is_wait_confirm).setValue(String.valueOf(is_wait_confirm));
		cols.get(C.app_register_followup).setValue(r.getString(C.app_register_followup));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.status_id).setValue(r.getInt(C.status_id));
		cols.get(C.signature_need_fix).setValue(r.getInt(C.signature_need_fix));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		filter.add(new TablePageFilter(C.is_wait_confirm, C.columnTypeNumber, C.operationIn, "2,4", null));
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
			StringBuilder sb = new StringBuilder();
			sb.append(MemberConfiguration.getAddressInHtmlOptionFormat(fi));
			sb.append(C.char_31).append(fi.getConnection().getGlobalConfig(C.kotsms, C.msg_footnote));
			
			StringBuilder sbSex = new StringBuilder();
			int sbSexSize = 0;
			for (MemberSex e : EnumSet.allOf(MemberSex.class)) {
				sbSex.append(C.char_31).append(e.getId()).append(C.char_31).append(e.getShortName());
				sbSexSize++;
			}
			sb.append(C.char_31).append(sbSexSize).append(sbSex);
			
			return sb.toString();
		} else if (action.equals(C.getDataForUpdate)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			PreparedStatement pstmt = conn.prepareStatement("select member_name, phone_no, arc_no, arc_photo_basename, arc_expire_date, signature_photo_basename, address, birthday, email, app_member_name, app_phone_no, app_email, lm_time, member_login_id, app_register_followup, app_arc_image, app_signature_image, app_address_image, signature_need_fix, sex_id from member where member_id = ?");
			try {
				pstmt.setLong(1, member_id);
				
				StringBuilder sb = new StringBuilder();
				String arc_no, member_login_id, app_arc_image, app_signature_image, app_address_image;
				ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery());
				try {
					if (r.next()) {
						arc_no = r.getString(C.arc_no);
						member_login_id = r.getString(C.member_login_id);
						app_arc_image = r.getString(C.app_arc_image);
						app_signature_image = r.getString(C.app_signature_image);
						app_address_image = r.getString(C.app_address_image);
						
						sb.append(r.getString(C.member_name)).append(C.char_31)
						.append(r.getString(C.phone_no)).append(C.char_31)
						.append(arc_no).append(C.char_31)
						.append(MemberConfiguration.formatArcPhotoUrl(fi, member_id, r.getString(C.arc_photo_basename))).append(C.char_31)
						.append(r.getDate(C.arc_expire_date)).append(C.char_31)
						.append(MemberConfiguration.formatSignaturePhotoUrl(fi, member_id, r.getString(C.signature_photo_basename))).append(C.char_31)
						.append(r.getString(C.address)).append(C.char_31)
						.append(r.getDate(C.birthday)).append(C.char_31)
						.append(r.getString(C.email)).append(C.char_31)
						.append(r.getString(C.app_member_name)).append(C.char_31)
						.append(r.getString(C.app_phone_no)).append(C.char_31)
						.append(r.getString(C.app_email)).append(C.char_31)
						.append(r.getTimestamp(C.lm_time)).append(C.char_31)
						.append(member_login_id).append(C.char_31)
						.append(r.getString(C.app_register_followup)).append(C.char_31)
						.append(r.getString(C.signature_need_fix)).append(C.char_31)
						.append(r.getInt(C.sex_id));
					} else
						throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id));
				} finally {
					r.close();
				}
				
				String address_photo_url = fi.getConnection().getGlobalConfig(C.member, C.address_photo_url);
				sb.append(C.char_31).append(address_photo_url).append("/temp/").append(app_address_image.length() > 0 ? app_address_image : member_login_id).append(C.ext_png);
				
				String arc_photo_url = fi.getConnection().getGlobalConfig(C.member, C.arc_photo_url);
				sb.append(C.char_31).append(arc_photo_url).append("/temp/").append(app_arc_image.length() > 0 ? app_arc_image : member_login_id).append(C.ext_png);
				
				String signature_photo_url = fi.getConnection().getGlobalConfig(C.member, C.signature_photo_url);
				sb.append(C.char_31).append(signature_photo_url).append("/temp/").append(app_signature_image.length() > 0 ? app_signature_image : member_login_id).append(C.ext_png);
				
				return sb.toString();
			} finally {
				pstmt.close();
			}
		} else if (action.equals(C.update)) {
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
			int arc_photo_action = Helper.getInt(params, C.arc_photo_action, true);
			int signature_photo_action = Helper.getInt(params, C.signature_photo_action, true);
			m.arc_photo_basename = Helper.getString(params, C.arc_photo_basename, false);
			m.signature_photo_basename = Helper.getString(params, C.signature_photo_basename, false);
			m.sex = MemberSex.get(Helper.getInt(params, C.sex_id, true));
			
			PreparedStatement pstmtGet = conn.prepareStatement("select friend_id, app_phone_no, member_login_id, arc_photo_basename, signature_photo_basename, is_wait_confirm, app_arc_image, app_signature_image, app_address_image from member where member_id = ?");
			PreparedStatement pstmtUpdate = conn.prepareStatement("update member set is_wait_confirm = 3, remit_point = remit_point + ?, app_verify_time = ? where member_id = ?");
			PreparedStatement pstmtUpdatePoint = conn.prepareStatement("insert into member_point (member_id, lm_time, remit_point, reason_id) values (?,?,?,1)");
			PreparedStatement pstmtLock = conn.prepareStatement("select member_id from member where member_id = ? for update");
			try {
				Long friend_id = null;
				String app_phone_no, member_login_id, original_arc_photo_basename, original_signature_photo_basename, app_arc_image, app_signature_image, app_address_image;
				int is_wait_confirm;
				pstmtGet.setLong(1, m.member_id);
				ResultSet r = pstmtGet.executeQuery();
				try {
					if (r.next()) {
						long l = r.getLong(1);
						if (!r.wasNull()) {
							friend_id = l;
						}
						app_phone_no = r.getString(2);
						member_login_id = r.getString(3);
						original_arc_photo_basename = r.getString(4);
						original_signature_photo_basename = r.getString(5);
						is_wait_confirm = r.getInt(6);
						app_arc_image = r.getString(7);
						app_signature_image = r.getString(8);
						app_address_image = r.getString(9);
					} else
						throw new Exception(String.format(C.data_not_exist, "member_id = " + m.member_id));
				} finally {
					r.close();
				}
				
				// fix:
				// deadlock can happens if this member also have friend
				if (is_wait_confirm == 2 && friend_id != null) {
					List<Long> lockList = new ArrayList<>();
					lockList.add(m.member_id);
					lockList.add(friend_id);
					Collections.sort(lockList);
					for (Long l : lockList) {
						pstmtLock.setLong(1, l.longValue());
						try (ResultSet rLock = pstmtLock.executeQuery()) {
							rLock.next();
						}
					}
				}
				
				switch (is_wait_confirm) {
					case 2:
					case 4:
						break;
					default:
						throw new Exception(C.incorrect_status);
				}
				
				switch (arc_photo_action) {
					case 1:
						m.arc_photo_basename = this.copyImage(fi, fi.getConnection().getGlobalConfig(C.member, C.arc_photo_directory), member_login_id, app_arc_image);
						break;
					case 2:
						break;
					case 3:
						m.arc_photo_basename = original_arc_photo_basename;
						break;
				}
				
				switch (signature_photo_action) {
					case 1:
						m.signature_photo_basename = this.copyImage(fi, fi.getConnection().getGlobalConfig(C.member, C.signature_photo_directory), member_login_id, app_signature_image);
						break;
					case 2:
						break;
					case 3:
						m.signature_photo_basename = original_signature_photo_basename;
						break;
				}
				
				MemberConfiguration member = new MemberConfiguration();
				member.update(fi, m, false);
				
				if (is_wait_confirm == 2) {
					if (friend_id != null) {
						int app_share_bonus = Integer.parseInt(fi.getConnection().getGlobalConfig(C.remit_point, C.app_share_bonus));
						int app_share_50th_bonus = Integer.parseInt(fi.getConnection().getGlobalConfig(C.remit_point, C.app_share_50th_bonus));
						int app_share_50th_threshold = Integer.parseInt(fi.getConnection().getGlobalConfig(C.remit_point, C.app_share_50th_threshold));
						
						PreparedStatement pstmtFriend = conn.prepareStatement("select app_share_count from member where member_id = ? for update");
						PreparedStatement pstmtAddPoint = conn.prepareStatement("update member set remit_point = remit_point + ?, app_share_count = ? where member_id = ?");
						PreparedStatement pstmtAddPointHist = conn.prepareStatement("insert into member_point (member_id, lm_time, remit_point, friend_id, reason_id) values (?,?,?,?,?)");
						try {
							int app_share_count;
							pstmtFriend.setLong(1, friend_id);
							r = pstmtFriend.executeQuery();
							try {
								if (r.next()) {
									app_share_count = r.getInt(1);
								} else
									throw new Exception(String.format(C.data_not_exist, "friend_id = " + friend_id));
							} finally {
								r.close();
							}
							
							app_share_count++;
							
							pstmtAddPoint.setInt(1, app_share_bonus);
							pstmtAddPoint.setInt(2, app_share_count);
							pstmtAddPoint.setLong(3, friend_id);
							pstmtAddPoint.executeUpdate();
							
							pstmtAddPointHist.setLong(1, friend_id);
							pstmtAddPointHist.setTimestamp(2, m.lm_time);
							pstmtAddPointHist.setInt(3, app_share_bonus);
							pstmtAddPointHist.setLong(4, m.member_id);
							pstmtAddPointHist.setInt(5, 2);
							pstmtAddPointHist.executeUpdate();
							
							if (app_share_count >= app_share_50th_threshold) {
								app_share_count = app_share_count - app_share_50th_threshold;
								
								pstmtAddPoint.setInt(1, app_share_50th_bonus);
								pstmtAddPoint.setInt(2, app_share_count);
								pstmtAddPoint.setLong(3, friend_id);
								pstmtAddPoint.executeUpdate();
								
								pstmtAddPointHist.setLong(1, friend_id);
								pstmtAddPointHist.setTimestamp(2, new Timestamp(m.lm_time.getTime() + 1000));
								pstmtAddPointHist.setInt(3, app_share_50th_bonus);
								pstmtAddPointHist.setNull(4, Types.NUMERIC);
								pstmtAddPointHist.setInt(5, 7);
								pstmtAddPointHist.executeUpdate();
							}
						} finally {
							pstmtAddPoint.close();
							pstmtAddPointHist.close();
						}
					}
					
					int app_register_bonus = Integer.parseInt(fi.getConnection().getGlobalConfig(C.remit_point, C.app_register_bonus));
					
					pstmtUpdate.setInt(1, app_register_bonus);
					pstmtUpdate.setTimestamp(2, m.lm_time);
					pstmtUpdate.setLong(3, m.member_id);
					pstmtUpdate.executeUpdate();
					
					pstmtUpdatePoint.setLong(1, m.member_id);
					pstmtUpdatePoint.setTimestamp(2, m.lm_time);
					pstmtUpdatePoint.setInt(3, app_register_bonus);
					pstmtUpdatePoint.executeUpdate();
					
					// log for KPI: new member verification
					fi.getConnection().logHistory(IndogoTable.member_app_register, m.member_id, HistoryAction.update, "new_member", m.lm_time, m.lm_user, new HistoryData[0]);
					
					try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into member_history (member_id, lm_time, lm_user, hst_id) values (?,?,?,?)"))) {
						pstmt.setLong(1, m.member_id);
						pstmt.setTimestamp(2, m.lm_time);
						pstmt.setString(3, fi.getSessionInfo().getUserName());
						pstmt.setInt(4, MemberHistory.accept_member.getId());
						pstmt.executeUpdate();
					}
					
					conn.commit();
					
					deleteImage(fi.getConnection().getGlobalConfig(C.member, C.arc_photo_directory), member_login_id, app_arc_image);
					deleteImage(fi.getConnection().getGlobalConfig(C.member, C.signature_photo_directory), member_login_id, app_signature_image);
					deleteImage(fi.getConnection().getGlobalConfig(C.member, C.address_photo_directory), member_login_id, app_address_image);
					
					// send sms
					try {
						String account = fi.getConnection().getGlobalConfig(C.kotsms, C.account);
						String password = fi.getConnection().getGlobalConfig(C.kotsms, C.password);
						String register_complete_message = URLEncoder.encode(fi.getConnection().getGlobalConfig(C.kotsms, C.register_complete_message), "UTF-8");
						String url = "http://api.kotsms.com.tw/kotsmsapi-1.php?username=" + account + "&password=" + password + "&dstaddr=" + app_phone_no + "&smbody=" + register_complete_message;
						HttpClient client = HttpClientBuilder.create().build();
						HttpGet request = new HttpGet(url);
						HttpResponse response = client.execute(request);
						BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						try {
							StringBuffer result = new StringBuffer();
							String line;
							while ((line = rd.readLine()) != null) {
								result.append(line);
							}
						} finally {
							rd.close();
						}
					} catch (Exception ignore) {}
				} else if (is_wait_confirm == 4) {
					pstmtUpdate.setInt(1, 0);
					pstmtUpdate.setTimestamp(2, m.lm_time);
					pstmtUpdate.setLong(3, m.member_id);
					pstmtUpdate.executeUpdate();
					
					// log for KPI: update member verification
					fi.getConnection().logHistory(IndogoTable.member_app_register, m.member_id, HistoryAction.update, "update_member", m.lm_time, m.lm_user, new HistoryData[0]);
					
					conn.commit();
					
					deleteImage(fi.getConnection().getGlobalConfig(C.member, C.arc_photo_directory), member_login_id, app_arc_image);
					deleteImage(fi.getConnection().getGlobalConfig(C.member, C.signature_photo_directory), member_login_id, app_signature_image);
					deleteImage(fi.getConnection().getGlobalConfig(C.member, C.address_photo_directory), member_login_id, app_address_image);
				}
				
				return C.emptyString;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				pstmtUpdate.close();
				pstmtGet.close();
				pstmtLock.close();
				pstmtUpdatePoint.close();
			}
		} else if (action.equals(C.download_app_image_arc) || action.equals(C.download_app_image_signature) || action.equals(C.download_app_image_address)) {
			String member_login_id = Helper.getString(params, C.member_login_id, true);
			String configName, columnName;
			if (action.equals(C.download_app_image_arc)) {
				configName = C.arc_photo_directory;
				columnName = C.app_arc_image;
			} else if (action.equals(C.download_app_image_signature)) {
				configName = C.signature_photo_directory;
				columnName = C.app_signature_image;
			} else if (action.equals(C.download_app_image_address)) {
				configName = C.address_photo_directory;
				columnName = C.app_address_image;
			} else
				throw new Exception("unknown action");
			String remoteUri = fi.getConnection().getGlobalConfig(C.member, configName);
			
			String app_image;
			PreparedStatement pstmt = conn.prepareStatement("select " + columnName + " from member where member_login_id = ?");
			try {
				pstmt.setString(1, member_login_id);
				try (ResultSet r = pstmt.executeQuery()) {
					if (r.next()) {
						app_image = r.getString(1);
					} else {
						throw new Exception(String.format(C.data_not_exist, "member_login_id = " + member_login_id));
					}
				}
			} finally {
				pstmt.close();
			}
			
			return downloadImage(fi, remoteUri, member_login_id, app_image);
		} else if (action.equals(C.ask_for_followup)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			String app_register_followup = Helper.getString(params, C.app_register_followup, true);
			
			PreparedStatement pstmtGet = conn.prepareStatement("select lm_time from member where member_id = ? for update");
			PreparedStatement pstmtUpdate = conn.prepareStatement("update member set app_register_followup = ?, lm_time = ? where member_id = ?");
			try {
				Timestamp old_lm_time;
				pstmtGet.setLong(1, member_id);
				ResultSet r = pstmtGet.executeQuery();
				try {
					if (r.next()) {
						old_lm_time = r.getTimestamp(1);
					} else
						throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id));
				} finally {
					r.close();
				}
				
				if (old_lm_time.getTime() != lm_time.getTime()) {
					throw new Exception(String.format(C.data_already_updated_by_another_user, "member_id = " + member_id));
				}
				
				pstmtUpdate.setString(1, app_register_followup);
				pstmtUpdate.setTimestamp(2, fi.getConnection().getCurrentTime());
				pstmtUpdate.setLong(3, member_id);
				pstmtUpdate.executeUpdate();
				
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into member_history (member_id, lm_time, lm_user, hst_id, hst_desc) values (?,?,?,?,?)"))) {
					pstmt.setLong(1, member_id);
					pstmt.setTimestamp(2, lm_time);
					pstmt.setString(3, fi.getSessionInfo().getUserName());
					pstmt.setInt(4, MemberHistory.ask_for_followup.getId());
					pstmt.setString(5, app_register_followup);
					pstmt.executeUpdate();
				}
				
				conn.commit();
				
				return C.emptyString;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				pstmtGet.close();
				pstmtUpdate.close();
			}
		} else if (action.equals(C.clear_followup)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			PreparedStatement pstmtGet = conn.prepareStatement("select lm_time from member where member_id = ? for update");
			PreparedStatement pstmtUpdate = conn.prepareStatement("update member set app_register_followup = null, lm_time = ? where member_id = ?");
			try {
				Timestamp old_lm_time;
				pstmtGet.setLong(1, member_id);
				ResultSet r = pstmtGet.executeQuery();
				try {
					if (r.next()) {
						old_lm_time = r.getTimestamp(1);
					} else
						throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id));
				} finally {
					r.close();
				}
				
				if (old_lm_time.getTime() != lm_time.getTime()) {
					throw new Exception(String.format(C.data_already_updated_by_another_user, "member_id = " + member_id));
				}
				
				Timestamp currentTime = fi.getConnection().getCurrentTime();
				pstmtUpdate.setTimestamp(1, currentTime);
				pstmtUpdate.setLong(2, member_id);
				pstmtUpdate.executeUpdate();
				
				conn.commit();
				
				return Stringify.getTimestamp(currentTime);
			}  catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				pstmtGet.close();
			}
		} else if (action.equals(C.send_sms)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			String sms_message = Helper.getString(params, C.sms_message, true);
			PreparedStatement pstmt = conn.prepareStatement("select app_phone_no from member where member_id = ?");
			try {
				pstmt.setLong(1, member_id);
				String app_phone_no;
				try (ResultSet r = pstmt.executeQuery()) {
					if (r.next()) {
						app_phone_no = r.getString(1);
					} else
						throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id));
				}
				
				String sms_account = fi.getConnection().getGlobalConfig(C.kotsms, C.account);
				String sms_password = fi.getConnection().getGlobalConfig(C.kotsms, C.password);
				int httpConnectionTimeout = Integer.parseInt(fi.getConnection().getGlobalConfig(C.kotsms, C.http_connection_timeout));
				int httpSoTimeout = Integer.parseInt(fi.getConnection().getGlobalConfig(C.kotsms, C.http_so_timeout));
				int httpRequestTimeout = Integer.parseInt(fi.getConnection().getGlobalConfig(C.kotsms, C.http_request_timeout));
				
				RequestConfig requestConfig = RequestConfig.custom()
						.setSocketTimeout(httpSoTimeout)
						.setConnectTimeout(httpConnectionTimeout)
						.setConnectionRequestTimeout(httpRequestTimeout)
						.build();
				
				SocketConfig socketConfig = SocketConfig.custom()
						.setSoTimeout(httpSoTimeout)
						.build();
				
				CloseableHttpClient httpClient = HttpClients.custom()
				        .setDefaultRequestConfig(requestConfig)
				        .setDefaultSocketConfig(socketConfig)
				        .build();
				try {
					URLCodec codec = new URLCodec();
					StringBuilder sb = new StringBuilder();
					sb.append("http://api.kotsms.com.tw/kotsmsapi-1.php?username=").append(sms_account).append("&password=").append(sms_password).append("&dstaddr=").append(app_phone_no).append("&smbody=").append(codec.encode(sms_message));
					HttpGet httpGet = new HttpGet(sb.toString());
					CloseableHttpResponse response = httpClient.execute(httpGet);
					try {
						HttpEntity entity = response.getEntity();
						try {
							if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
								InputStream inputStream = entity.getContent();
								String reply = IOUtils.toString(inputStream, "UTF-8");
								String header1 = reply.substring(0, 7);
								String header2 = reply.substring(0, 8);
								boolean sendError = true;
								if (header1.equals("kmsgid=")) {
									if (!header2.equals("kmsgid=-")) {
										sendError = false;
									}
								}
								
								if (sendError) {
									throw new Exception("send sms failed: " + reply);
								}
								
								return C.emptyString;
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
			} finally {
				pstmt.close();
			}
		} else if (action.equals(C.mark_as_fixme)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			boolean signature_need_fix = Helper.getInt(params, C.signature_need_fix, true) == 1;
			PreparedStatement pstmtGet = conn.prepareStatement("select lm_time from member where member_id = ? for update");
			PreparedStatement pstmt = conn.prepareStatement("update member set signature_need_fix = ? where member_id = ?");
			try {
				pstmtGet.setLong(1, member_id);
				ResultSet r = pstmtGet.executeQuery();
				try {
					if (r.next()) {
					} else
						throw new Exception(String.format(C.data_not_exist, "member_id = " + member_id));
				} finally {
					r.close();
				}
				
				pstmt.setInt(1, signature_need_fix ? 1 : 0);
				pstmt.setLong(2, member_id);
				pstmt.executeUpdate();
				conn.commit();
				return C.emptyString;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				pstmt.close();
				pstmtGet.close();
			}
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}
	
	private final static char[] ALLOWED_CHARS = "abcdfghjklmnpqrstuvwxyz23456789".toCharArray();
	
	public String generatePassword() {
		StringBuilder sb = new StringBuilder(6);
		SecureRandom r = new SecureRandom();
		for (int i = 0; i < 6; i++) {
			sb.append(ALLOWED_CHARS[r.nextInt(ALLOWED_CHARS.length)]);
		}
		return sb.toString();
	}
	
	private String copyImage(FunctionItem fi, String remoteUri, String member_login_id, String app_image) throws Exception {
		String baseName = UUID.randomUUID().toString().replaceAll("-", "") + "_app";
		int userRowId = fi.getSessionInfo().getUserRowId();
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			manager.init();
			
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setStrictHostKeyChecking(opts, "no");
			builder.setUserDirIsRoot(opts, true);
			builder.setTimeout(opts, 10000);
			FileObject fromFile = manager.resolveFile(Stringify.concat(remoteUri, "/", C.temp, "/", Helper.isNullOrEmpty(app_image) ? member_login_id : app_image, C.ext_png), opts);
			FileObject toBase = manager.resolveFile(Stringify.concat(remoteUri, "/", C.temp, "/", String.valueOf(userRowId)), opts);
			toBase.createFolder();
			FileObject toFile = manager.resolveFile(toBase, Stringify.concat(baseName, "_original.png"), opts);
			toFile.copyFrom(fromFile, Selectors.SELECT_SELF);
		} finally {
			manager.close();
		}
		return baseName;
	}
	
	private String downloadImage(FunctionItem fi, String remoteUri, String member_login_id, String app_image) throws Exception {
		String name = member_login_id + C.ext_png;
		File localFile = new File(fi.getTempFolder(), name);
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			manager.init();
			
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setStrictHostKeyChecking(opts, "no");
			builder.setUserDirIsRoot(opts, true);
			builder.setTimeout(opts, 10000);
			FileObject fromFile = manager.resolveFile(Stringify.concat(remoteUri, "/", C.temp, "/", Helper.isNullOrEmpty(app_image) ? member_login_id : app_image, C.ext_png), opts);
			FileObject toFile = manager.resolveFile(localFile.getAbsolutePath());
			toFile.copyFrom(fromFile, Selectors.SELECT_SELF);
		} finally {
			manager.close();
		}
		return name;
	}
	
	private void deleteImage(String remoteUri, String member_login_id, String app_image) {
		try {
			StandardFileSystemManager manager = new StandardFileSystemManager();
			try {
				manager.init();
				
				FileSystemOptions opts = new FileSystemOptions();
				SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
				builder.setStrictHostKeyChecking(opts, "no");
				builder.setUserDirIsRoot(opts, true);
				builder.setTimeout(opts, 10000);
				FileObject fromFile = manager.resolveFile(Stringify.concat(remoteUri, "/", C.temp, "/", Helper.isNullOrEmpty(app_image) ? member_login_id : app_image, C.ext_png), opts);
				fromFile.delete();
			} finally {
				manager.close();
			}
		} catch (Throwable ignore) {}
	}

}
