package com.indogo.relay.member;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import com.indogo.model.member.BankCodeModel;
import com.indogo.relay.member.BankCodeConfiguration.OrderBy;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
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

public class RecipientView extends AbstractFunction implements ITablePage {
	
	private String recipient_photo_url;

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.member_recipient_v;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<TablePageColumn>();
		cols.add(new TablePageColumn(C.recipient_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Recipient Name"));
		cols.add(new TablePageColumn(C.recipient_name_2, C.columnTypeString, C.columnDirectionDefault, true, false, "Recipient Name 2"));
		cols.add(new TablePageColumn(C.bank_code, C.columnTypeString, C.columnDirectionDefault, true, false, "Bank Code"));
		cols.add(new TablePageColumn(C.bank_name, C.columnTypeString, C.columnDirectionDefault, true, false, "Bank Name"));
		cols.add(new TablePageColumn(C.swift_code, C.columnTypeString, C.columnDirectionDefault, true, false, "SWIFT"));
		cols.add(new TablePageColumn(C.bank_acc, C.columnTypeString, C.columnDirectionDefault, true, false, "Bank Account"));
		cols.add(new TablePageColumn(C.birthday, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Birthday"));
		cols.add(new TablePageColumn(C.id_image, C.columnTypeString, C.columnDirectionNone, false, true, "ID Picture"));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, C.lm_user));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, true));
		cols.add(new TablePageColumn(C.recipient_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.recipient_id, true, true));
		cols.add(new TablePageColumn(C.is_verified, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_verified, true, true));
		cols.add(new TablePageColumn(C.is_hidden, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_hidden, true, true));
		cols.add(new TablePageColumn(C.id_filename, C.columnTypeString, C.columnDirectionDefault, true, false, C.id_filename, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		long member_id = r.unwrap().getLong(C.member_id);
		int recipient_id = r.unwrap().getInt(C.recipient_id);
		String id_filename = r.getString(C.id_filename);
		String id_image = getRecipientPhotoUrl(recipient_photo_url, member_id, recipient_id, id_filename);
		
		cols.get(C.recipient_name).setValue(r.getString(C.recipient_name));
		cols.get(C.recipient_name_2).setValue(r.getString(C.recipient_name_2));
		cols.get(C.bank_code).setValue(r.getString(C.bank_code));
		cols.get(C.bank_name).setValue(r.getString(C.bank_name));
		cols.get(C.swift_code).setValue(r.getString(C.swift_code));
		cols.get(C.bank_acc).setValue(r.getString(C.bank_acc));
		cols.get(C.birthday).setValue(r.getDate(C.birthday));
		cols.get(C.id_image).setValue(id_image);
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.member_id).setValue(String.valueOf(member_id));
		cols.get(C.recipient_id).setValue(String.valueOf(recipient_id));
		cols.get(C.is_verified).setValue(r.getInt(C.is_verified));
		cols.get(C.is_hidden).setValue(r.getInt(C.is_hidden));
		cols.get(C.id_filename).setValue(id_filename);
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
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
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
		Connection conn = fi.getConnection().getConnection();
		
		String type_name = Helper.getString(params, C.type_name, true);
		if (type_name.equals(C.birthday)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			int recipient_id = Helper.getInt(params, C.recipient_id, true);
			Timestamp birthday = Helper.getTimestamp(params, C.birthday, false);
			
			try {
				updateBirthday(fi, member_id, recipient_id, birthday);
				conn.commit();
				return Stringify.getDate(birthday);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (type_name.equals(C.id_image)) {
			Hashtable<String, FileItem> uploadFiles = fi.getUploadedFiles();
			if (uploadFiles.size() > 0) {
				FileItem photo = uploadFiles.get(C.id_image);
				if (photo != null && photo.getName().length() > 0) {
					long member_id = Helper.getLong(params, C.member_id, true);
					int recipient_id = Helper.getInt(params, C.recipient_id, true);
					
					try {
						String filename = updateIdImage(fi, member_id, recipient_id, photo);
						conn.commit();
						
						String recipient_photo_url = fi.getConnection().getGlobalConfig(C.member, C.recipient_photo_url);
						return recipient_photo_url + "/" + member_id + "/" + recipient_id + "/" + filename + ".png";
					} catch (Exception e) {
						conn.rollback();
						throw e;
					}
				}
			}
		} else if (type_name.equals(C.recipient_name_2)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			int recipient_id = Helper.getInt(params, C.recipient_id, true);
			String recipient_name_2 = Helper.getString(params, C.recipient_name_2, false);
			
			try {
				updateRecipientName2(fi, member_id, recipient_id, recipient_name_2);
				conn.commit();
				return recipient_name_2;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		}
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
	
	public void updateBirthday(FunctionItem fi, long member_id, int recipient_id, Timestamp birthday) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member_recipient set birthday = ? where member_id = ? and recipient_id = ?"))) {
			pstmt.setTimestamp(1, birthday);
			pstmt.setLong(2, member_id);
			pstmt.setInt(3, recipient_id);
			pstmt.executeUpdate();
		}
	}
	
	public String updateIdImage(FunctionItem fi, long member_id, int recipient_id, FileItem photo) throws Exception {
		L l = fi.getLanguage();
		Connection conn = fi.getConnection().getConnection();
		
		String old_id_filename;
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select id_filename from member_recipient where member_id = ? and recipient_id = ? for update"))) {
			pstmt.setLong(1, member_id);
			pstmt.setInt(2, recipient_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					old_id_filename = r.getString(1);
				} else {
					throw new Exception(l.data_not_exist("member_id=" + member_id + ", recipient_id=" + recipient_id));
				}
			}
		}
		
		String new_id_filename = UUID.randomUUID().toString().replaceAll("-", "");
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member_recipient set id_filename = ? where member_id = ? and recipient_id = ?"))) {
			pstmt.setString(1, new_id_filename);
			pstmt.setLong(2, member_id);
			pstmt.setInt(3, recipient_id);
			pstmt.executeUpdate();
		}
		
		BufferedImage bufferedImage = ImageIO.read(photo.getInputStream());
		File file = new File(fi.getTempFolder(), new_id_filename);
		if (!ImageIO.write(bufferedImage, "png", file)) {
			throw new Exception("cannot save photo");
		}
		
		String recipient_photo_directory = fi.getConnection().getGlobalConfig(C.member, C.recipient_photo_directory);
		
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			manager.init();
			
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setStrictHostKeyChecking(opts, "no");
			builder.setUserDirIsRoot(opts, true);
			builder.setTimeout(opts, 10000);
			
			FileObject remoteBase = manager.resolveFile(recipient_photo_directory + "/" + member_id + "/" + recipient_id, opts);
			remoteBase.createFolder();
			
			if (old_id_filename != null) {
				FileObject remoteFile = manager.resolveFile(remoteBase, old_id_filename + ".png", opts);
				try {
					remoteFile.delete();
				} catch (Exception ignore) {}
			}
			
			FileObject localFile = manager.resolveFile(file.getAbsolutePath());
			FileObject newFile = manager.resolveFile(remoteBase, new_id_filename + ".png", opts);
			localFile.moveTo(newFile);
		} finally {
			manager.close();
		}

		return new_id_filename;
	}
	
	public void updateRecipientName2(FunctionItem fi, long member_id, int recipient_id, String recipient_name_2) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member_recipient set recipient_name_2 = ? where member_id = ? and recipient_id = ?"))) {
			pstmt.setString(1, recipient_name_2);
			pstmt.setLong(2, member_id);
			pstmt.setInt(3, recipient_id);
			pstmt.executeUpdate();
		}
	}
	
	public static String getRecipientPhotoUrl(String recipient_photo_url, long member_id, int recipient_id, String id_filename) {
		if (Helper.isNullOrEmpty(id_filename)) {
			return C.emptyString;
		} else {
			return recipient_photo_url + "/" + member_id + "/" + recipient_id + "/" + id_filename + ".png";
		}
	}

}
