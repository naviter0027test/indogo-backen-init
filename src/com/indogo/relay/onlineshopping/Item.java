package com.indogo.relay.onlineshopping;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.imgscalr.Scalr;

import com.lionpig.language.L;
import com.indogo.model.onlineshopping.ItemCompositeModel;
import com.indogo.model.onlineshopping.ItemModel;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.func.TablePage;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;
import com.lionpig.webui.http.util.Stringify;

public class Item extends AbstractFunction implements ITablePage {
	
	private static String IMAGE_BASE_URL = null;
	private final static String s1 = "<img src=\"";
	private final static String s2 = "no_item_thumbnail.png";
	private final static String s3 = "\"/>";
	private final static String s4 = "no_item.png";

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.item_with_inventory_v;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString, true));
		cols.add(new TablePageColumn(C.item_image, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString, true));
		cols.add(new TablePageColumn(C.item_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.item_name()));
		cols.add(new TablePageColumn(C.item_desc, C.columnTypeString, C.columnDirectionDefault, true, false, l.item_desc()));
		cols.add(new TablePageColumn(C.price_sale, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.price_sale()));
		cols.add(new TablePageColumn(C.barcode_id, C.columnTypeString, C.columnDirectionNone, false, true, "Vendor Barcode"));
		cols.add(new TablePageColumn(C.category_name, C.columnTypeString, C.columnDirectionNone, true, false, l.category_name()));
		cols.add(new TablePageColumn(C.color_name, C.columnTypeString, C.columnDirectionNone, true, false, l.color_name()));
		cols.add(new TablePageColumn(C.size_name, C.columnTypeString, C.columnDirectionNone, true, false, l.size_name()));
		cols.add(new TablePageColumn(C.location, C.columnTypeString, C.columnDirectionDefault, true, false, l.location()));
		cols.add(new TablePageColumn(C.expired_date, C.columnTypeDate, C.columnDirectionDefault, true, false, l.expired_date()));
		cols.add(new TablePageColumn(C.item_point, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_point()));
		cols.add(new TablePageColumn(C.inventory_qty, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Inventory Qty"));
		cols.add(new TablePageColumn(C.item_filename, C.columnTypeString, C.columnDirectionNone, false, false, l.item_filename(), false, true));
		cols.add(new TablePageColumn(C.item_disabled, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_disabled(), false, true));
		cols.add(new TablePageColumn(C.item_hide, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_hide(), false, true));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, l.lm_time()));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, l.lm_user()));
		cols.add(new TablePageColumn(C.item_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_id(), true, true));
		cols.add(new TablePageColumn(C.category_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.category_id(), true, true));
		cols.add(new TablePageColumn(C.color_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.color_id(), true, true));
		cols.add(new TablePageColumn(C.size_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.size_id(), true, true));
		cols.add(new TablePageColumn(C.is_composite, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_composite, false, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		int item_id = r.unwrap().unwrap().getInt(C.item_id);
		String item_filename = r.unwrap().getString(C.item_filename);
		
		cols.get(C.action).setValue(C.emptyString);
		cols.get(C.item_image).setValue(resolveItemImageUrl(fi, item_id, item_filename));
		cols.get(C.item_name).setValue(r.getString(C.item_name));
		cols.get(C.item_desc).setValue(r.getString(C.item_desc));
		cols.get(C.price_sale).setValue(r.getIntCurrency(C.price_sale));
		cols.get(C.barcode_id).setValue(C.emptyString);
		cols.get(C.category_name).setValue(r.getString(C.category_name));
		cols.get(C.color_name).setValue(r.getString(C.color_name));
		cols.get(C.size_name).setValue(r.getString(C.size_name));
		cols.get(C.inventory_qty).setValue(r.getInt(C.inventory_qty));
		cols.get(C.item_filename).setValue(item_filename);
		cols.get(C.item_disabled).setValue(r.getInt(C.item_disabled));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.item_id).setValue(Stringify.getString(item_id));
		cols.get(C.category_id).setValue(r.getInt(C.category_id));
		cols.get(C.color_id).setValue(r.getInt(C.color_id));
		cols.get(C.size_id).setValue(r.getInt(C.size_id));
		cols.get(C.location).setValue(r.getString(C.location));
		cols.get(C.expired_date).setValue(r.getDate(C.expired_date));
		cols.get(C.item_point).setValue(r.getInt(C.item_point));
		cols.get(C.item_hide).setValue(r.getInt(C.item_hide));
		cols.get(C.is_composite).setValue(r.getInt(C.is_composite));
	}
	
	public static String resolveItemImageUrl(FunctionItem fi, int item_id, String item_filename) throws Exception {
		if (IMAGE_BASE_URL == null) {
			IMAGE_BASE_URL = fi.getConnection().getGlobalConfig(C.item, C.image_base_url);
		}
		
		int level = item_id % 5000;
		StringBuilder sb = new StringBuilder();
		sb.append(s1).append(IMAGE_BASE_URL).append(C.slash);
		if (item_filename == null)
			sb.append(s2);
		else
			sb.append(level).append(C.slash).append(item_filename).append(C.underscore).append(C.thumbnail).append(C.dot).append(C.png);
		
		sb.append(s3);
		
		return sb.toString();
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
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		if (action.equals(C.upload_image)) {
			Hashtable<String, FileItem> uploadFiles = fi.getUploadedFiles();
			if (uploadFiles.size() > 0) {
				FileItem uploadFile = uploadFiles.get(C.upload_image);
				if (uploadFile != null && uploadFile.getName().length() > 0) {
					InputStream stream = uploadFile.getInputStream();
					String old_filename = Helper.getString(params, C.item_filename, false);
					
					String item_filename = saveImageToTemp(fi, stream, old_filename);
					
					return new StringBuilder().append(item_filename)
							.append(C.char_31).append(fi.getConnection().getGlobalConfig(C.item, C.image_temp_url)).append(C.slash).append(item_filename).append(C.dot).append(C.png).toString();
				} else
					throw new Exception(l.input_must_be_provided(C.upload_image));
			} else
				throw new Exception(l.input_must_be_provided(C.upload_image));
		} else if (action.equals(C.add_barcode)) {
			int item_id = Helper.getInt(params, C.item_id, true);
			String barcode_id = Helper.getString(params, C.barcode_id, true);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into barcode (item_id, barcode_id) values (?,?)"))) {
				pstmt.setInt(1, item_id);
				pstmt.setString(2, barcode_id);
				pstmt.executeUpdate();
			}
			conn.commit();
			return C.emptyString;
		} else if (action.equals(C.delete_barcode)) {
			String barcode_id = Helper.getString(params, C.barcode_id, true);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from barcode where barcode_id = ?"))) {
				pstmt.setString(1, barcode_id);
				pstmt.executeUpdate();
			}
			conn.commit();
			return C.emptyString;
		} else if (action.equals(C.get_barcode)) {
			int item_id = Helper.getInt(params, C.item_id, true);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select barcode_id from barcode where item_id = ?"))) {
				pstmt.setInt(1, item_id);
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQueryWrapper())) {
					StringBuilder sb = new StringBuilder();
					while (r.next()) {
						sb.append(r.getString(1)).append(C.char_31);
					}
					if (sb.length() > 0) {
						sb.delete(sb.length() - 1, sb.length());
					}
					return sb.toString();
				}
			}
		} else if (action.equals(C.seq_no)) {
			String item_name_prefix = Helper.getString(params, C.item_name_prefix, true);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select max(cast(substring(item_name, ?) as int)) + 1 from item where item_name like ? and length(item_name) = ?"))) {
				pstmt.setInt(1, item_name_prefix.length() + 1);
				pstmt.setString(2, item_name_prefix + "%");
				pstmt.setInt(3, item_name_prefix.length() + 8);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						Integer seq = r.getInt(1);
						if (seq == null) {
							return "1";
						} else {
							return String.valueOf(seq);
						}
					} else {
						return "1";
					}
				}
			}
		} else if (action.equals(C.sql_temp_table)) {
			List<String> item_names = Helper.getStringArrayByReadLine(params, C.item_name);
			try {
				int count = sqlTempTable(fi, item_names);
				conn.commit();
				return String.valueOf(count);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.item_disabled)) {
			int item_id = Helper.getInt(params, C.item_id, true);
			int item_disabled;
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select item_disabled from item where item_id = ? for update"))) {
				pstmt.setInt(1, item_id);
				try (ResultSet r = pstmt.executeQuery()) {
					if (r.next()) {
						item_disabled = r.getInt(1);
					} else {
						throw new Exception(l.data_not_exist("item_id = " + item_id));
					}
				}
			}
			
			if (item_disabled == 0) {
				item_disabled = 1;
			} else {
				item_disabled = 0;
			}
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update item set item_disabled = ? where item_id = ?"))) {
				pstmt.setInt(1, item_disabled);
				pstmt.setInt(2, item_id);
				pstmt.executeUpdate();
			}
			
			conn.commit();
			return String.valueOf(item_disabled);
		} else if (action.equals(C.item_hide)) {
			int item_id = Helper.getInt(params, C.item_id, true);
			int item_hide;
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select item_hide from item where item_id = ? for update"))) {
				pstmt.setInt(1, item_id);
				try (ResultSet r = pstmt.executeQuery()) {
					if (r.next()) {
						item_hide = r.getInt(1);
					} else {
						throw new Exception(l.data_not_exist("item_id = " + item_id));
					}
				}
			}
			
			if (item_hide == 0) {
				item_hide = 1;
			} else {
				item_hide = 0;
			}
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update item set item_hide = ? where item_id = ?"))) {
				pstmt.setInt(1, item_hide);
				pstmt.setInt(2, item_id);
				pstmt.executeUpdate();
			}
			
			conn.commit();
			return String.valueOf(item_hide);
		} else {
			throw new Exception(l.unknown_action(action));
		}
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		sb.append(l.item_maintenance_title())
		.append(C.char_31).append(l.item_image())
		.append(C.char_31).append(l.item_name())
		.append(C.char_31).append(l.item_desc())
		.append(C.char_31).append(l.price_sale())
		.append(C.char_31).append(l.category_name())
		.append(C.char_31).append(l.color_name())
		.append(C.char_31).append(l.size_name())
		.append(C.char_31).append(l.category_maintenance_title())
		.append(C.char_31).append(l.imei_flag())
		.append(C.char_31).append(l.color_maintenance_title())
		.append(C.char_31).append(l.size_maintenance_title())
		.append(C.char_31).append(l.button_title_add_checked_item_to_list())
		.append(C.char_31).append(l.item_name_prefix())
		.append(C.char_31).append(l.location())
		.append(C.char_31).append(ItemCategory.getAll(fi));
		
		Statement stmt = fi.getConnection().getConnection().createStatement();
		try {
			ResultSetWrapperStringify r;
			
			r = new ResultSetWrapperStringify(stmt.executeQuery(s.item_color_get_all()));
			try {
				List<String> list = new ArrayList<>();
				while (r.next()) {
					list.add(r.getInt(1));
					list.add(r.getString(2));
				}
				sb.append(C.char_31).append(list.size()/2);
				for (String str : list) {
					sb.append(C.char_31).append(str);
				}
			} finally {
				r.close();
			}
			
			r = new ResultSetWrapperStringify(stmt.executeQuery(s.item_size_get_all()));
			try {
				List<String> list = new ArrayList<>();
				while (r.next()) {
					list.add(r.getInt(1));
					list.add(r.getString(2));
				}
				sb.append(C.char_31).append(list.size()/2);
				for (String str : list) {
					sb.append(C.char_31).append(str);
				}
			} finally {
				r.close();
			}
		} finally {
			stmt.close();
		}
		return sb.toString();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		ItemModel itemModel = new ItemModel();
		itemModel.item_name = Helper.getString(params, C.item_name, true, l.item_name());
		itemModel.item_desc = Helper.getString(params, C.item_desc, false, l.item_desc());
		itemModel.item_filename = Helper.getString(params, C.item_filename, false, l.item_filename());
		itemModel.category_id = Helper.getInt(params, C.category_id, true, l.category_name());
		itemModel.color_id = Helper.getIntNullable(params, C.color_id, false, l.color_name());
		itemModel.size_id = Helper.getIntNullable(params, C.size_id, false, l.size_name());
		itemModel.item_disabled = Helper.getInt(params, C.item_disabled, false, l.item_disabled()) == 1;
		itemModel.price_sale = Helper.getIntNullable(params, C.price_sale, false, l.price_sale());
		itemModel.location = Helper.getString(params, C.location, false, l.location());
		itemModel.expired_date = Helper.getTimestamp(params, C.expired_date, false);
		itemModel.item_point = Helper.getIntNullable(params, C.item_point, false);
		itemModel.is_composite = Helper.getInt(params, C.is_composite, false) == 1;
		
		if (itemModel.is_composite) {
			String[] item_composites = Helper.getStringArray(params, C.item_composites, true);
			ItemCompositeModel[] ary = new ItemCompositeModel[item_composites.length];
			for (int i = 0; i < item_composites.length; i++) {
				String[] tokens = StringUtils.splitPreserveAllTokens(item_composites[i], C.char_30);
				ItemCompositeModel a = new ItemCompositeModel();
				a.item_id = Integer.parseInt(tokens[0]);
				a.item_qty = Integer.parseInt(tokens[1]);
				ary[i] = a;
			}
			itemModel.item_composites = ary;
		}
		
		try {
			insert(fi, itemModel);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.item_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(itemModel.item_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			fi.getConnection().getConnection().commit();
			
			return row;
		} catch (Exception e) {
			fi.getConnection().getConnection().rollback();
			throw e;
		}
	}

	@Override
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		ItemModel itemModel = new ItemModel();
		itemModel.item_id = Helper.getInt(params, C.item_id, true, l.item_id());
		itemModel.item_name = Helper.getString(params, C.item_name, true, l.item_name());
		itemModel.item_desc = Helper.getString(params, C.item_desc, false, l.item_desc());
		itemModel.item_filename = Helper.getString(params, C.item_filename, false, l.item_filename());
		itemModel.category_id = Helper.getInt(params, C.category_id, true, l.category_name());
		itemModel.color_id = Helper.getIntNullable(params, C.color_id, false, l.color_name());
		itemModel.size_id = Helper.getIntNullable(params, C.size_id, false, l.size_name());
		itemModel.item_disabled = Helper.getInt(params, C.item_disabled, false, l.item_disabled()) == 1;
		itemModel.price_sale = Helper.getIntNullable(params, C.price_sale, false, l.price_sale());
		itemModel.lm_time = Helper.getTimestamp(params, C.lm_time, true, l.lm_time());
		itemModel.location = Helper.getString(params, C.location, false);
		itemModel.expired_date = Helper.getTimestamp(params, C.expired_date, false);
		itemModel.item_point = Helper.getIntNullable(params, C.item_point, false);
		
		try {
			update(fi, itemModel);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.item_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(itemModel.item_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			fi.getConnection().getConnection().commit();
			
			return row;
		} catch (Exception e) {
			fi.getConnection().getConnection().rollback();
			throw e;
		}
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int item_id = Helper.getInt(params, C.item_id, true, l.item_id());
		Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true, l.lm_time());
		
		try {
			delete(fi, item_id, lm_time);
			fi.getConnection().getConnection().commit();
		} catch (Exception e) {
			fi.getConnection().getConnection().rollback();
			throw e;
		}
		
		return null;
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int item_id = Helper.getInt(params, C.item_id, true);
		ItemModel m = get(fi, item_id);
		
		StringBuilder sb = new StringBuilder();
		sb.append(m.item_name)
		.append(C.char_31).append(Stringify.getString(m.item_desc))
		.append(C.char_31).append(Stringify.getString(m.item_filename))
		.append(C.char_31).append(Stringify.getString(m.category_id))
		.append(C.char_31).append(Stringify.getString(m.color_id))
		.append(C.char_31).append(Stringify.getString(m.size_id))
		.append(C.char_31).append(Stringify.getString(m.item_disabled ? 1 : 0))
		.append(C.char_31).append(Stringify.getString(m.price_sale))
		.append(C.char_31).append(Stringify.getTimestamp(m.lm_time))
		.append(C.char_31).append(Stringify.getString(m.lm_user))
		.append(C.char_31).append(Stringify.getString(m.location))
		.append(C.char_31).append(Stringify.getDate(m.expired_date))
		.append(C.char_31).append(Stringify.getString(m.item_point));
		
		int level = item_id % 5000;
		String url = fi.getConnection().getGlobalConfig(C.item, C.image_base_url);
		sb.append(C.char_31).append(url).append(C.slash);
		if (m.item_filename == null)
			sb.append(s4);
		else
			sb.append(C.slash).append(level).append(C.slash).append(m.item_filename).append(C.dot).append(C.png);
		
		sb.append(C.char_31).append(m.is_composite ? 1 : 0);
		
		if (m.is_composite) {
			sb.append(C.char_31).append(m.item_composites.length);
			for (ItemCompositeModel mm : m.item_composites) {
				sb.append(C.char_31).append(mm.item_name)
				.append(C.char_31).append(mm.item_desc)
				.append(C.char_31).append(mm.category_name)
				.append(C.char_31).append(mm.color_name)
				.append(C.char_31).append(mm.size_name)
				.append(C.char_31).append(mm.item_qty)
				.append(C.char_31).append(mm.item_id)
				.append(C.char_31).append(url).append(C.slash);
				
				if (mm.item_filename == null) {
					sb.append(s4);
				} else {
					sb.append(C.slash).append(level).append(C.slash).append(mm.item_filename).append(C.dot).append(C.png);
				}
			}
		}
		
		return sb.toString();
	}
	
	public ItemModel get(FunctionItem fi, int item_id) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		S sql = fi.getSql();
		
		ItemModel m = new ItemModel();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.item_get()))) {
			pstmt.setInt(1, item_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					m.item_id = item_id;
					m.item_name = r.getString(1);
					m.item_desc = r.getString(2);
					m.item_filename = r.getString(3);
					m.category_id = r.getInt(4);
					m.color_id = r.getInt(5);
					m.size_id = r.getInt(6);
					m.item_disabled = r.getInt(7) == 1;
					m.price_sale = r.getInt(8);
					m.lm_time = r.getTimestamp(9);
					m.lm_user = r.getString(10);
					m.location = r.getString(11);
					m.expired_date = r.getTimestamp(12);
					m.item_point = r.getInt(13);
					m.is_composite = r.getInt(14) == 1;
				} else {
					throw new Exception(fi.getLanguage().data_not_exist(C.item_id, Stringify.getString(item_id)));
				}
			}
		}
		
		if (m.is_composite) {
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.get_item_composites()))) {
				pstmt.setInt(1, m.item_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					List<ItemCompositeModel> list = new ArrayList<>();
					while (r.next()) {
						ItemCompositeModel mm = new ItemCompositeModel();
						mm.composite_id = m.item_id;
						mm.item_name = r.getString(1);
						mm.item_desc = r.getString(2);
						mm.category_name = r.getString(3);
						mm.color_name = r.getString(4);
						mm.size_name = r.getString(5);
						mm.item_qty = r.getInt(6);
						mm.item_id = r.getInt(7);
						mm.item_filename = r.getString(8);
						list.add(mm);
					}
					m.item_composites = list.toArray(new ItemCompositeModel[0]);
				}
			}
		}
		
		return m;	}
	
	public void insert(FunctionItem fi, ItemModel itemModel) throws Exception {
		itemModel.item_id = (int) fi.getConnection().getSeq(C.item, true);
		itemModel.lm_time = fi.getConnection().getCurrentTime();
		itemModel.lm_user = fi.getSessionInfo().getUserName();
		
		if (itemModel.item_filename != null && itemModel.item_filename.length() > 0) {
			int groupId = itemModel.item_id % 5000;
			moveImageToProduction(fi, itemModel.item_filename, groupId);
		}
		
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(fi.getSql().item_insert()))) {
			pstmt.setInt(1, itemModel.item_id);
			pstmt.setString(2, itemModel.item_name);
			pstmt.setString(3, itemModel.item_desc);
			pstmt.setString(4, itemModel.item_filename);
			pstmt.setInt(5, itemModel.category_id);
			pstmt.setInt(6, itemModel.color_id);
			pstmt.setInt(7, itemModel.size_id);
			pstmt.setInt(8, itemModel.item_disabled ? 1 : 0);
			pstmt.setInt(9, itemModel.price_sale);
			pstmt.setTimestamp(10, itemModel.lm_time);
			pstmt.setString(11, itemModel.lm_user);
			pstmt.setString(12, itemModel.location);
			pstmt.setTimestamp(13, itemModel.expired_date);
			pstmt.setInt(14, itemModel.item_point);
			pstmt.setInt(15, itemModel.is_composite ? 1 : 0);
			pstmt.executeUpdate();
		}
		
		if (itemModel.is_composite) {
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into item_composite (composite_id, item_id, item_qty) values (?,?,?)"))) {
				for (ItemCompositeModel m : itemModel.item_composites) {
					m.composite_id = itemModel.item_id;
					pstmt.setInt(1, m.composite_id);
					pstmt.setInt(2, m.item_id);
					pstmt.setInt(3, m.item_qty);
					pstmt.executeUpdate();
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(fi.getSql().inventory_insert()))) {
			Set<Integer> shop_ids = Shop.getShopIds(fi);
			for (int shop_id : shop_ids) {
				pstmt.setInt(1, shop_id);
				pstmt.setInt(2, itemModel.item_id);
				pstmt.setInt(3, 0);
				pstmt.setTimestamp(4, itemModel.lm_time);
				pstmt.setString(5, itemModel.lm_user);
				pstmt.executeUpdate();
			}
		}
	}
	
	public void update(FunctionItem fi, ItemModel itemModel) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		Timestamp old_lm_time;
		String old_filename;
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.item_select_for_update()))) {
			pstmt.setInt(1, itemModel.item_id);
			try (ResultSet r = pstmt.executeQuery()) {
				if (r.next()) {
					old_lm_time = r.getTimestamp(1);
					old_filename = r.getString(2);
				} else
					throw new Exception(fi.getLanguage().data_not_exist(C.item_id, Stringify.getString(itemModel.item_id)));
			}
		}
		
		if (old_lm_time.getTime() != itemModel.lm_time.getTime())
			throw new Exception(fi.getLanguage().data_already_updated_by_another_user(C.item_id, Stringify.getString(itemModel.item_id)));

		if (!Helper.isEquals(old_filename, itemModel.item_filename)) {
			int groupId = itemModel.item_id % 5000;
			
			if (!Helper.isNullOrEmpty(old_filename)) {
				deleteImageFromProduction(fi, old_filename, groupId);
			}
			
			if (!Helper.isNullOrEmpty(itemModel.item_filename)) {
				moveImageToProduction(fi, itemModel.item_filename, groupId);
			}
		}
		
		try (PreparedStatementWrapper pstmtUpdate = new PreparedStatementWrapper(conn.prepareStatement(sql.item_update()))) {
			itemModel.lm_time = fi.getConnection().getCurrentTime();
			itemModel.lm_user = fi.getSessionInfo().getUserName();
			
			pstmtUpdate.setString(1, itemModel.item_name);
			pstmtUpdate.setString(2, itemModel.item_desc);
			pstmtUpdate.setString(3, itemModel.item_filename);
			pstmtUpdate.setInt(4, itemModel.category_id);
			pstmtUpdate.setInt(5, itemModel.color_id);
			pstmtUpdate.setInt(6, itemModel.size_id);
			pstmtUpdate.setInt(7, itemModel.item_disabled ? 1 : 0);
			pstmtUpdate.setInt(8, itemModel.price_sale);
			pstmtUpdate.setTimestamp(9, itemModel.lm_time);
			pstmtUpdate.setString(10, itemModel.lm_user);
			pstmtUpdate.setString(11, itemModel.location);
			pstmtUpdate.setTimestamp(12, itemModel.expired_date);
			pstmtUpdate.setInt(13, itemModel.item_point);
			pstmtUpdate.setInt(14, itemModel.item_id);
			pstmtUpdate.executeUpdate();
		}
	}
	
	public void delete(FunctionItem fi, int item_id, Timestamp lm_time) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();

		Timestamp old_lm_time;
		String old_filename;
		boolean is_composite;
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.item_select_for_update()))) {
			pstmt.setInt(1, item_id);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					old_lm_time = r.getTimestamp(1);
					old_filename = r.getString(2);
					is_composite = r.getInt(3) == 1;
				} else
					throw new Exception(fi.getLanguage().data_not_exist(C.item_id, Stringify.getString(item_id)));
			} finally {
				r.close();
			}
		}
		
		if (old_lm_time.getTime() != lm_time.getTime())
			throw new Exception(fi.getLanguage().data_already_updated_by_another_user(C.item_id, Stringify.getString(item_id)));
		
		// delete inventory with qty = 0
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from inventory where item_id = ? and item_qty = 0"))) {
			pstmt.setInt(1, item_id);
			pstmt.executeUpdate();
		}
		
		if (is_composite) {
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from item_composite where composite_id = ?"))) {
				pstmt.setInt(1, item_id);
				pstmt.executeUpdate();
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.item_delete()))) {
			pstmt.setInt(1, item_id);
			pstmt.executeUpdate();
		}
		
		if (!Helper.isNullOrEmpty(old_filename)) {
			int groupId = item_id % 5000;
			deleteImageFromProduction(fi, old_filename, groupId);
		}
	}
	
	public String saveImageToTemp(FunctionItem fi, InputStream imageSource, String oldFilename) throws Exception {
		String tempFolder = fi.getConnection().getGlobalConfig(C.item, C.image_temp_directory);
		String filename = UUID.randomUUID().toString().replaceAll("-", "");
		
		BufferedImage bufferedImage = ImageIO.read(imageSource);
		File file = new File(fi.getTempFolder(), filename);
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
			FileObject remoteTempFolder = manager.resolveFile(tempFolder, opts);
			FileObject remoteFile = manager.resolveFile(remoteTempFolder, Stringify.concat(filename, ".png"), opts);
			localFile.moveTo(remoteFile);
			
			if (!Helper.isNullOrEmpty(oldFilename)) {
				remoteFile = manager.resolveFile(remoteTempFolder, Stringify.concat(oldFilename, ".png"), opts);
				try {
					remoteFile.delete();
				} catch (Exception ignore) {}
			}
			
			return filename;
		} finally {
			manager.close();
		}
	}
	
	public void moveImageToProduction(FunctionItem fi, String filename, int groupId) throws Exception {
		String tempFolderPath = fi.getConnection().getGlobalConfig(C.item, C.image_temp_directory);
		String productionFolderPath = Stringify.concat(fi.getConnection().getGlobalConfig(C.item, C.image_base_directory), C.slash, String.valueOf(groupId));
		
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			manager.init();
			
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setStrictHostKeyChecking(opts, "no");
			builder.setUserDirIsRoot(opts, true);
			builder.setTimeout(opts, 10000);
			FileObject tempFile = manager.resolveFile(manager.resolveFile(tempFolderPath, opts), Stringify.concat(filename, ".png"), opts);
			
			// generate thumbnail
			File originalFile = new File(fi.getTempFolder(), filename);
			File thumbnailFile = new File(fi.getTempFolder(), Stringify.concat(filename, C.underscore, C.thumbnail, C.dot, C.png));
			FileObject localFile = manager.resolveFile(originalFile.getAbsolutePath());
			localFile.copyFrom(tempFile, Selectors.SELECT_SELF);
			BufferedImage bufferedImage = ImageIO.read(originalFile);
			ImageIO.write(Scalr.resize(bufferedImage, 100, 100), C.png, thumbnailFile);
			originalFile.delete();
			localFile = manager.resolveFile(thumbnailFile.getAbsolutePath());
			
			FileObject productionFolder = manager.resolveFile(productionFolderPath, opts);
			if (!productionFolder.exists()) {
				productionFolder.createFolder();
			}
			FileObject productionFile = manager.resolveFile(productionFolder, Stringify.concat(filename, C.dot, C.png), opts);
			tempFile.moveTo(productionFile);
			FileObject productionThumbnail = manager.resolveFile(productionFolder, Stringify.concat(filename, C.underscore, C.thumbnail, C.dot, C.png), opts);
			localFile.moveTo(productionThumbnail);
		} finally {
			manager.close();
		}
	}
	
	public void deleteImageFromProduction(FunctionItem fi, String filename, int groupId) throws Exception {
		String productionFolderPath = Stringify.concat(fi.getConnection().getGlobalConfig(C.item, C.image_base_directory), C.slash, String.valueOf(groupId));
		
		StandardFileSystemManager manager = new StandardFileSystemManager();
		try {
			manager.init();
			
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
			builder.setStrictHostKeyChecking(opts, "no");
			builder.setUserDirIsRoot(opts, true);
			builder.setTimeout(opts, 10000);
			
			FileObject productionFolder = manager.resolveFile(productionFolderPath, opts);
			if (productionFolder.exists()) {
				FileObject productionFile = manager.resolveFile(productionFolder, Stringify.concat(filename, C.dot, C.png), opts);
				if (productionFile.exists()) {
					productionFile.delete();
				}
				FileObject productionThumbnail = manager.resolveFile(productionFolder, Stringify.concat(filename, C.underscore, C.thumbnail, C.dot, C.png), opts);
				if (productionThumbnail.exists()) {
					productionThumbnail.delete();
				}
			}
		} finally {
			manager.close();
		}
	}
	
	public int sqlTempTable(FunctionItem fi, List<String> item_names) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.item);
			pstmt.executeUpdate();
			
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.item_item_names);
			pstmt.executeUpdate();
		}
			
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.insert_temp_table_item1()))) {
			for (String item_name : item_names) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.item_item_names);
				pstmt.setString(3, item_name);
				pstmt.executeUpdate();
			}
		}
		
		List<Integer> item_ids = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select distinct a.item_id from item a join sql_temp_table b on a.item_name = b.item1 and b.session_id = ? and b.page_id = ?"))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.item_item_names);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					item_ids.add(r.getInt(1));
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select distinct a.item_id from barcode a join sql_temp_table b on a.barcode_id = b.item1 and b.session_id = ? and b.page_id = ?"))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.item_item_names);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					item_ids.add(r.getInt(1));
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.insert_temp_table_long1()))) {
			for (Integer item_id : item_ids) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.item);
				pstmt.setInt(3, item_id);
				pstmt.executeUpdate();
			}
		}
		
		return item_ids.size();
	}

}
