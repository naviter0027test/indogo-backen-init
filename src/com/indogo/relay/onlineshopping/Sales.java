package com.indogo.relay.onlineshopping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.indogo.InventoryHistoryReason;
import com.indogo.model.member.MemberModel;
import com.indogo.model.onlineshopping.Freight;
import com.indogo.model.onlineshopping.SalesItemModel;
import com.indogo.model.onlineshopping.SalesModel;
import com.indogo.model.onlineshopping.SalesStatus;
import com.indogo.relay.member.MemberConfiguration;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lionpig.language.L;
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

public class Sales extends AbstractFunction implements ITablePage {
	private PreparedStatementWrapper pstmtMember = null;

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.sales;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString));
		cols.add(new TablePageColumn(C.print, C.columnTypeString, C.columnDirectionNone, false, true, "Printed"));
		cols.add(new TablePageColumn(C.print_by, C.columnTypeString, C.columnDirectionDefault, true, false, "Print By"));
		cols.add(new TablePageColumn(C.status_name, C.columnTypeString, C.columnDirectionNone, false, true, "Status"));
		cols.add(new TablePageColumn(C.sales_id, C.columnTypeNumber, C.columnDirectionDesc, true, false, C.sales_id));
		cols.add(new TablePageColumn(C.member_name, C.columnTypeString, C.columnDirectionNone, false, true, "Customer Name"));
		cols.add(new TablePageColumn(C.phone_no_1, C.columnTypeString, C.columnDirectionNone, false, true, "Telp 1"));
		cols.add(new TablePageColumn(C.phone_no_2, C.columnTypeString, C.columnDirectionNone, false, true, "Telp 2"));
		cols.add(new TablePageColumn(C.ship_address, C.columnTypeString, C.columnDirectionDefault, true, false, "Shipping Address"));
		cols.add(new TablePageColumn(C.total_amount, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total"));
		cols.add(new TablePageColumn(C.ship_no, C.columnTypeString, C.columnDirectionDefault, true, false, "Shipping Number"));
		cols.add(new TablePageColumn(C.freight_name, C.columnTypeString, C.columnDirectionNone, false, true, "Shipping Type"));
		cols.add(new TablePageColumn(C.comment, C.columnTypeString, C.columnDirectionDefault, true, false, "Comment"));
		cols.add(new TablePageColumn(C.lm_user_created, C.columnTypeString, C.columnDirectionDefault, true, false, "Create By"));
		cols.add(new TablePageColumn(C.lm_time_created, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Create Time"));
		cols.add(new TablePageColumn(C.lm_user_checkout, C.columnTypeString, C.columnDirectionDefault, true, false, "Checkout By"));
		cols.add(new TablePageColumn(C.lm_time_checkout, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Checkout Time"));
		cols.add(new TablePageColumn(C.lm_user_shipped, C.columnTypeString, C.columnDirectionDefault, true, false, "Shipped By"));
		cols.add(new TablePageColumn(C.lm_time_shipped, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Shipped Time"));
		cols.add(new TablePageColumn(C.lm_user_paid, C.columnTypeString, C.columnDirectionDefault, true, false, "Paid By"));
		cols.add(new TablePageColumn(C.lm_time_paid, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Paid Time"));
		cols.add(new TablePageColumn(C.lm_user_returned, C.columnTypeString, C.columnDirectionDefault, true, false, "Returned By"));
		cols.add(new TablePageColumn(C.lm_time_returned, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Returned Time"));
		cols.add(new TablePageColumn(C.lm_user_scrapped, C.columnTypeString, C.columnDirectionDefault, true, false, "Scrapped By"));
		cols.add(new TablePageColumn(C.lm_time_scrapped, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Scrapped Time"));
		cols.add(new TablePageColumn(C.invoice_no, C.columnTypeString, C.columnDirectionDefault, true, false, "發票號碼"));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, l.lm_time(), true, true));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, l.lm_user(), true, true));
		cols.add(new TablePageColumn(C.shop_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.shop_id, true, true));
		cols.add(new TablePageColumn(C.status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.status_id, true, true));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, true));
		cols.add(new TablePageColumn(C.freight_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.freight_id, true, true));
		cols.add(new TablePageColumn(C.is_print, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_print, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		L l = fi.getLanguage();
		SalesStatus status = SalesStatus.get(r.unwrap().unwrap().getInt(C.status_id));
		Freight freight = Freight.get(r.unwrap().unwrap().getInt(C.freight_id));
		long member_id = r.unwrap().unwrap().getInt(C.member_id);
		
		String member_name, phone_no_1, phone_no_2;
		pstmtMember.setLong(1, member_id);
		try (ResultSetWrapperStringify rr = new ResultSetWrapperStringify(pstmtMember.executeQueryWrapper())) {
			if (rr.next()) {
				member_name = rr.getString(C.member_name);
				
				String[] phone_nos = StringUtils.split(rr.getString(C.phone_no), '.');
				String app_phone_no = rr.getString(C.app_phone_no);
				
				Set<String> phones = new HashSet<>();
				if (app_phone_no.length() > 0) {
					phones.add(app_phone_no);
				}
				for (String phone_no : phone_nos) {
					if (phone_no.length() > 0) {
						phones.add(phone_no);
					}
				}
				
				phone_nos = phones.toArray(new String[0]);
				if (phone_nos.length > 0) {
					phone_no_1 = phone_nos[0];
				} else {
					phone_no_1 = C.emptyString;
				}
				
				if (phone_nos.length > 1) {
					phone_no_2 = phone_nos[1];
				} else {
					phone_no_2 = C.emptyString;
				}
			} else {
				member_name = C.emptyString;
				phone_no_1 = C.emptyString;
				phone_no_2 = C.emptyString;
			}
		}
		
		cols.get(C.action).setValue(C.emptyString);
		cols.get(C.print).setValue(C.emptyString);
		cols.get(C.status_name).setValue(status.getName(l));
		cols.get(C.sales_id).setValue(r.getLong(C.sales_id));
		cols.get(C.member_name).setValue(member_name);
		cols.get(C.phone_no_1).setValue(phone_no_1);
		cols.get(C.phone_no_2).setValue(phone_no_2);
		cols.get(C.ship_address).setValue(r.getString(C.ship_address));
		cols.get(C.total_amount).setValue(r.getIntCurrency(C.total_amount));
		cols.get(C.invoice_no).setValue(r.getString(C.invoice_no));
		cols.get(C.ship_no).setValue(r.getString(C.ship_no));
		cols.get(C.freight_name).setValue(freight.getName(l));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.shop_id).setValue(r.getInt(C.shop_id));
		cols.get(C.status_id).setValue(String.valueOf(status.getId()));
		cols.get(C.member_id).setValue(String.valueOf(member_id));
		cols.get(C.freight_id).setValue(String.valueOf(freight.getId()));
		cols.get(C.is_print).setValue(r.getInt(C.is_print));
		cols.get(C.comment).setValue(r.getString(C.comment));
		cols.get(C.print_by).setValue(r.getString(C.print_by));
		
		cols.get(C.lm_time_created).setValue(r.getTimestamp(C.lm_time_created));
		cols.get(C.lm_time_checkout).setValue(r.getTimestamp(C.lm_time_checkout));
		cols.get(C.lm_time_shipped).setValue(r.getTimestamp(C.lm_time_shipped));
		cols.get(C.lm_time_paid).setValue(r.getTimestamp(C.lm_time_paid));
		cols.get(C.lm_time_returned).setValue(r.getTimestamp(C.lm_time_returned));
		cols.get(C.lm_time_scrapped).setValue(r.getTimestamp(C.lm_time_scrapped));

		cols.get(C.lm_user_created).setValue(r.getString(C.lm_user_created));
		cols.get(C.lm_user_checkout).setValue(r.getString(C.lm_user_checkout));
		cols.get(C.lm_user_shipped).setValue(r.getString(C.lm_user_shipped));
		cols.get(C.lm_user_paid).setValue(r.getString(C.lm_user_paid));
		cols.get(C.lm_user_returned).setValue(r.getString(C.lm_user_returned));
		cols.get(C.lm_user_scrapped).setValue(r.getString(C.lm_user_scrapped));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		S sql = fi.getSql();
		pstmtMember = new PreparedStatementWrapper(fi.getConnection().getConnection().prepareStatement(sql.member_get_for_sales_export_heimao()));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		pstmtMember.close();
	}

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		
		if (action.equals(C.print)) {
			long[] sales_ids = Helper.getLongArray(params, C.sales_ids, true);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String filename = "sales_" + dateFormat.format(new Date()) + ".pdf";
			FileOutputStream output = new FileOutputStream(new File(fi.getTempFolder(), filename), false);
			try {
				print(fi, sales_ids, output);
			} finally {
				output.close();
			}
			
			List<Long> sales_ids_sorted = new ArrayList<>();
			for (long sales_id : sales_ids) {
				sales_ids_sorted.add(sales_id);
			}
			Collections.sort(sales_ids_sorted);
			
			String lm_user = fi.getSessionInfo().getUserName();
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update sales set is_print = 1, print_by = ? where sales_id = ?"))) {
				for (long sales_id : sales_ids_sorted) {
					pstmt.setString(1, lm_user);
					pstmt.setLong(2, sales_id);
					pstmt.executeUpdate();
				}
			}
			conn.commit();
			
			return filename;
		} else if (action.equals(C.sql_temp_table)) {
			Timestamp lm_time_created_start = Helper.getTimestamp(params, C.lm_time_created_start, true);
			Timestamp lm_time_created_end = Helper.getTimestamp(params, C.lm_time_created_end, true);
			String member_name = Helper.getString(params, C.member_name, false);
			String phone_no = Helper.getString(params, C.phone_no, false);
			String item_desc = Helper.getString(params, C.item_desc, false);
			try {
				int count = prepareTempTable(fi, lm_time_created_start, lm_time_created_end, member_name, phone_no, item_desc);
				conn.commit();
				return String.valueOf(count);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.update_comment)) {
			long sales_id = Helper.getLong(params, C.sales_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			String comment = Helper.getString(params, C.comment, true);
			try {
				Timestamp old_lm_time;
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select lm_time from sales where sales_id = ? for update"))) {
					pstmt.setLong(1, sales_id);
					try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
						if (r.next()) {
							old_lm_time = r.getTimestamp(1);
						} else {
							throw new Exception(l.data_not_exist(C.sales_id, sales_id));
						}
					}
				}
				
				if (old_lm_time.getTime() != lm_time.getTime()) {
					throw new Exception(l.data_already_updated_by_another_user(C.sales_id, sales_id));
				}
				
				lm_time = fi.getConnection().getCurrentTime();
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update sales set lm_time = ?, comment = ? where sales_id = ?"))) {
					pstmt.setTimestamp(1, lm_time);
					pstmt.setString(2, comment);
					pstmt.setLong(3, sales_id);
					pstmt.executeUpdate();
				}
				
				conn.commit();
				
				return Stringify.getTimestamp(lm_time);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.paid)) {
			long sales_id = Helper.getLong(params, C.sales_id, true);
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			try {
				Timestamp old_lm_time;
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select lm_time from sales where sales_id = ? for update"))) {
					pstmt.setLong(1, sales_id);
					try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
						if (r.next()) {
							old_lm_time = r.getTimestamp(1);
						} else {
							throw new Exception(l.data_not_exist(C.sales_id, sales_id));
						}
					}
				}
				
				if (old_lm_time.getTime() != lm_time.getTime()) {
					throw new Exception(l.data_already_updated_by_another_user(C.sales_id, sales_id));
				}
				
				lm_time = fi.getConnection().getCurrentTime();
				String lm_user = fi.getSessionInfo().getUserName();
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update sales set status_id = ?, lm_time = ?, lm_user = ?, lm_time_paid = ?, lm_user_paid = ? where sales_id = ?"))) {
					pstmt.setInt(1, SalesStatus.paid.getId());
					pstmt.setTimestamp(2, lm_time);
					pstmt.setString(3, lm_user);
					pstmt.setTimestamp(4, lm_time);
					pstmt.setString(5, lm_user);
					pstmt.setLong(6, sales_id);
					pstmt.executeUpdate();
				}
				
				conn.commit();
				
				StringBuilder sb = new StringBuilder();
				sb.append(SalesStatus.paid.getName(l))
				.append(C.char_31).append(SalesStatus.paid.getId())
				.append(C.char_31).append(Stringify.getTimestamp(lm_time))
				.append(C.char_31).append(lm_user);
				return sb.toString();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		}
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(Shop.getShopListForUser(fi))
		.append(C.char_31).append(fi.getConnection().getGlobalConfig(C.sales, C.ship_fee_heimao))
		.append(C.char_31).append(fi.getConnection().getGlobalConfig(C.sales, C.ship_fee_post))
		.append(C.char_31).append(MemberConfiguration.getAddressInHtmlOptionFormat(fi));
		return sb.toString();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return null;
	}

	@Override
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		long sales_id = Helper.getLong(params, C.sales_id, true);
		String ship_address = Helper.getString(params, C.ship_address, true);
		String[] items = Helper.getStringArray(params, C.items, true);
		int freight_id = Helper.getInt(params, C.freight_id, true);
		int ship_fee = Helper.getInt(params, C.ship_fee, true);
		String comment = Helper.getString(params, C.comment, false);
		String phone_no = Helper.getString(params, C.phone_no, true);
		String member_login_id = Helper.getString(params, C.member_login_id, false);
		String member_name = Helper.getString(params, C.member_name, false);
		Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
		
		int total_amount = ship_fee;
		SalesItemModel[] salesItems = new SalesItemModel[items.length];
		for (int i = 0; i < items.length; i++) {
			String[] cells = StringUtils.splitPreserveAllTokens(items[i], C.char_30);
			SalesItemModel salesItem = new SalesItemModel();
			salesItem.item_id = Integer.parseInt(cells[0]);
			salesItem.sales_qty = Integer.parseInt(cells[1]);
			salesItem.sales_price = Integer.parseInt(cells[2]);
			salesItem.sales_discount = Integer.parseInt(cells[3]);
			salesItem.sales_total = (salesItem.sales_qty * salesItem.sales_price) - salesItem.sales_discount;
			salesItem.comment = cells[4];
			salesItems[i] = salesItem;
			
			total_amount += salesItem.sales_total;
		}
		
		SalesModel salesModel = new SalesModel();
		salesModel.sales_id = sales_id;
		salesModel.ship_address = ship_address;
		salesModel.total_amount = total_amount;
		salesModel.items = salesItems;
		salesModel.freight = Freight.get(freight_id);
		salesModel.ship_fee = ship_fee;
		salesModel.comment = comment;
		salesModel.member_phone_no = phone_no;
		salesModel.member_login_id = member_login_id;
		salesModel.member_name = member_name;
		salesModel.lm_time = lm_time;
		
		Connection conn = fi.getConnection().getConnection();
		try {
			update(fi, salesModel);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.sales_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(salesModel.sales_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			conn.commit();
			
			return row;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		return null;
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		long sales_id = Helper.getLong(params, C.sales_id, true);
		
		StringBuilder sb = new StringBuilder();
		
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.shop_id, b.shop_name, a.ship_address, a.freight_id, a.ship_fee, a.point_used, a.comment,\r\n" + 
				"c.member_login_id, c.member_name, c.phone_no, c.remit_point, c.is_wait_confirm, a.lm_time\r\n" + 
				"from sales a inner join shop b on b.shop_id = a.shop_id\r\n" + 
				"inner join member c on c.member_id = a.member_id\r\n" + 
				"where a.sales_id = ?"))) {
			pstmt.setLong(1, sales_id);
			try (ResultSetWrapperStringify r = pstmt.executeQueryWrapperStringify()) {
				if (r.next()) {
					sb.append(r.getInt(1))
					.append(C.char_31).append(r.getString(2))
					.append(C.char_31).append(r.getString(3))
					.append(C.char_31).append(r.getInt(4))
					.append(C.char_31).append(r.getInt(5))
					.append(C.char_31).append(r.getInt(6))
					.append(C.char_31).append(r.getString(7))
					.append(C.char_31).append(r.getString(8))
					.append(C.char_31).append(r.getString(9))
					.append(C.char_31).append(r.getString(10))
					.append(C.char_31).append(r.getInt(11))
					.append(C.char_31).append(r.getInt(12))
					.append(C.char_31).append(r.getTimestamp(13));
				} else {
					throw new Exception(l.data_not_exist(C.sales_id, String.valueOf(sales_id)));
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.sales_qty, a.item_id, b.item_qty, IFNULL(b.price_sale, c.price_sale) AS price_sale, a.`comment`,\r\n" + 
				"c.item_filename, c.item_name, c.item_desc, d.category_name, e.color_name, f.size_name\r\n" + 
				"from sales_item a inner join inventory b on b.shop_id = 1 and b.item_id = a.item_id\r\n" + 
				"inner join item c on c.item_id = a.item_id\r\n" + 
				"inner join item_category d on d.category_id = c.category_id\r\n" + 
				"left join item_color e on e.color_id = c.color_id\r\n" + 
				"left join item_size f on f.size_id = c.size_id\r\n" + 
				"where a.sales_id = ?"))) {
			pstmt.setLong(1, sales_id);
			try (ResultSetWrapperStringify r = pstmt.executeQueryWrapperStringify()) {
				int size = 0;
				StringBuilder ss = new StringBuilder();
				while (r.next()) {
					ss.append(C.char_31).append(r.getInt(1))
					.append(C.char_31).append(r.getInt(2))
					.append(C.char_31).append(r.getInt(3))
					.append(C.char_31).append(r.getInt(4))
					.append(C.char_31).append(r.getString(5));
					
					String item_filename = r.getString(6);
					String item_image = Item.resolveItemImageUrl(fi, r.unwrap().getInt(2), item_filename);
					ss.append(C.char_31).append(item_image)
					.append(C.char_31).append(r.getString(7))
					.append(C.char_31).append(r.getString(8))
					.append(C.char_31).append(r.getString(9))
					.append(C.char_31).append(r.getString(10))
					.append(C.char_31).append(r.getString(11));
					size++;
				}
				sb.append(C.char_31).append(size).append(ss);
			}
		}
		
		return sb.toString();
	}
	
	private SalesModel getForPrint(FunctionItem fi, long sales_id) throws Exception {
		L l = fi.getLanguage();
		
		SalesModel salesModel = new SalesModel();
		salesModel.sales_id = sales_id;
		
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select shop_id, status_id, member_id, freight_id, ship_address, total_amount, lm_time, lm_user, invoice_no, ship_no, lm_time_created, comment from sales where sales_id = ?"))) {
			pstmt.setLong(1, sales_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					salesModel.shop_id = r.getInt(1);
					salesModel.status = SalesStatus.get(r.getInt(2));
					salesModel.member_id = r.getLong(3);
					salesModel.freight = Freight.get(r.getInt(4));
					salesModel.ship_address = r.getString(5);
					salesModel.total_amount = r.getInt(6);
					salesModel.lm_time = r.getTimestamp(7);
					salesModel.lm_user = r.getString(8);
					salesModel.invoice_no = r.getString(9);
					salesModel.ship_no = r.getString(10);
					salesModel.lm_time_created = r.getTimestamp(11);
					salesModel.comment = r.getString(12);
				} else {
					throw new Exception(l.data_not_exist("sales_id = " + sales_id));
				}
			}
		}
		
		/**
		 * select a.item_id, a.sales_qty, a.sales_price, a.sales_discount, a.sales_total, a.comment,
b.item_desc, c.category_name, d.color_name, e.size_name
from sales_item a
inner join item b on b.item_id = a.item_id
inner join item_category c on c.category_id = b.category_id
left join item_color d on d.color_id = b.color_id
left join item_size e on e.size_id = b.size_id
where a.sales_id = ?
order by c.category_name
		 */
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.item_id, a.sales_qty, a.sales_price, a.sales_discount, a.sales_total, a.comment,\r\n" + 
				"b.item_desc, c.category_name, d.color_name, e.size_name\r\n" + 
				"from sales_item a\r\n" + 
				"inner join item b on b.item_id = a.item_id\r\n" + 
				"inner join item_category c on c.category_id = b.category_id\r\n" + 
				"left join item_color d on d.color_id = b.color_id\r\n" + 
				"left join item_size e on e.size_id = b.size_id\r\n" + 
				"where a.sales_id = ?\r\n" + 
				"order by c.category_name"))) {
			pstmt.setLong(1, sales_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				List<SalesItemModel> items = new ArrayList<>();
				while (r.next()) {
					SalesItemModel item = new SalesItemModel();
					item.item_id = r.getInt(1);
					item.sales_qty = r.getInt(2);
					item.sales_price = r.getInt(3);
					item.sales_discount = r.getInt(4);
					item.sales_total = r.getInt(5);
					item.comment = r.getString(6);
					item.item_desc = r.getString(7);
					item.category_name = r.getString(8);
					item.color_name = r.getString(9);
					item.size_name = r.getString(10);
					items.add(item);
				}
				salesModel.items = items.toArray(new SalesItemModel[0]);
			}
		}
		
		return salesModel;
	}
	
	public void print(FunctionItem fi, long[] sales_ids, OutputStream output) throws Exception {
		if (!FontFactory.isRegistered(C.arial)) {
			String path = fi.getConnection().getGlobalConfig(C.money_transfer, C.font_arial);
			FontFactory.register(path, C.arial);
		}
		if (!FontFactory.isRegistered(C.alger)) {
			String path = fi.getConnection().getGlobalConfig(C.money_transfer, C.font_alger);
			FontFactory.register(path, C.alger);
		}
		
		Font FontArialNormal = FontFactory.getFont(C.arial, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.NORMAL);
		Font FontArialSmall = FontFactory.getFont(C.arial, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 8, Font.NORMAL);
		
		Image logoImage = null;
		String logoFilename = fi.getServletContext().getRealPath("ui/logo.png");
		File logoFile = new File(logoFilename);
		if (logoFile.exists()) {
			byte[] logoBytes = FileUtils.readFileToByteArray(logoFile);
			logoImage = Image.getInstance(logoBytes);
			logoImage.scaleToFit(140, 45);
		}
		
		Document doc = new Document(PageSize.A4);
		PdfWriter writer = PdfWriter.getInstance(doc, output);
		doc.setMargins(15, 15, 15, doc.getPageSize().getHeight()/2f);
		doc.setMarginMirroring(true);
		doc.open();
		try {
			PdfContentByte cb = writer.getDirectContent();
			Paragraph p;
			PdfPTable table;
			PdfPTable page;
			PdfPCell pageCell;
			
			MemberConfiguration member = new MemberConfiguration();
			for (int i = 0; i < sales_ids.length; i++) {
				long sales_id = sales_ids[i];
				SalesModel salesModel = getForPrint(fi, sales_id);
				MemberModel memberModel = member.getData(fi, salesModel.member_id);
				
				page = new PdfPTable(1);
				page.setWidthPercentage(100);
				
				// sales id
				p = new Paragraph("\nTerima Kasih telah berbelanja di Indogo\n\nPrint by: " + fi.getSessionInfo().getUserName() + ", " + Stringify.getTimestamp(new Date()), FontArialSmall);
				pageCell = new PdfPCell(p);
				pageCell.setBorder(Rectangle.NO_BORDER);
				pageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				page.addCell(pageCell);
				
				page.setHeaderRows(1);
				page.setFooterRows(1);
				
				// Basic Information 
				table = pdfHeader(cb, FontArialNormal, salesModel, memberModel, logoImage);
				pageCell = new PdfPCell(table);
				pageCell.setPaddingTop(10);
				pageCell.setPaddingBottom(10);
				pageCell.setBorder(Rectangle.NO_BORDER);
				page.addCell(pageCell);
				
				// Item List Title
				table = new PdfPTable(8);
				table.setWidthPercentage(100);
				table.setWidths(new float[] { 0.5f, 3.1f, 1, 1, 0.5f, 1, 1, 1 });
				this.pdfAddCellForBody(table, FontArialNormal, "NO", "NAME", "COLOR", "SIZE", "QTY", "PRICE", "DISCOUNT", "SUBTOTAL");
				pageCell = new PdfPCell(table);
				pageCell.setBorder(Rectangle.NO_BORDER);
				page.addCell(pageCell);
				for (int j = 0; j < salesModel.items.length; j++) {
					SalesItemModel salesItem = salesModel.items[j];
					
					String item_desc = salesItem.item_desc;
					if (!Helper.isNullOrEmpty(salesItem.comment)) {
						item_desc += "\n" + salesItem.comment;
					}
					
					table = new PdfPTable(8);
					table.setWidthPercentage(100);
					table.setWidths(new float[] { 0.5f, 3.1f, 1, 1, 0.5f, 1, 1, 1 });
					this.pdfAddCellForBody(table, FontArialNormal, String.valueOf(j+1), item_desc, salesItem.color_name, salesItem.size_name, String.valueOf(salesItem.sales_qty), Stringify.getCurrency(salesItem.sales_price), Stringify.getCurrency(salesItem.sales_discount), Stringify.getCurrency(salesItem.sales_total));
					pageCell = new PdfPCell(table);
					pageCell.setBorder(Rectangle.NO_BORDER);
					page.addCell(pageCell);
				}
				
				// Total Information
				table = new PdfPTable(4);
				table.setWidthPercentage(100);
				table.setWidths(new int[] { 1, 4, 1, 1 });
				this.pdfAddCellForHeader(table, FontArialNormal, "NOTE", salesModel.comment, "TOTAL", Stringify.getCurrency(salesModel.total_amount));
				pageCell = new PdfPCell(table);
				pageCell.setPaddingTop(5);
				pageCell.setBorder(Rectangle.NO_BORDER);
				page.addCell(pageCell);
				
				// Footer Signature
				table = new PdfPTable(5);
				table.setWidthPercentage(100);
				table.setWidths(new int[] {2, 1, 2, 1, 2});
				this.pdfAddCellForHeader(table, FontArialNormal, "CREATE TRANSACTION", "", "結帳", "", "COLLECT ITEMS");
				this.pdfAddCellForFooter(table, FontArialNormal, salesModel.lm_user, "", "");
				pageCell = new PdfPCell(table);
				pageCell.setPaddingTop(5);
				pageCell.setBorder(Rectangle.NO_BORDER);
				page.addCell(pageCell);
				
				doc.add(page);
				
				if (i < sales_ids.length - 1)
					doc.newPage();
			}
		} finally {
			doc.close();
		}
	}
	
	private PdfPTable pdfHeader(PdfContentByte cb, Font arialNormal, SalesModel sales, MemberModel member, Image logo) throws DocumentException {
		Barcode128 code128 = new Barcode128();
		code128.setCode(String.valueOf(sales.sales_id));
		Image img128 = code128.createImageWithBarcode(cb, null, null);
		
		int topDate = 10;
		Calendar dueDate = Calendar.getInstance();
		dueDate.setTime(sales.lm_time_created);
		dueDate.add(Calendar.DAY_OF_MONTH, topDate);
		
		String phone_no = member.app_phone_no;
		if (Helper.isNullOrEmpty(phone_no)) {
			String[] tokens = StringUtils.split(member.phone_no, '.');
			for (int i = 0; i < tokens.length; i++) {
				tokens[i] = tokens[i].trim();
				if (tokens[i].length() > 0) {
					phone_no = tokens[i];
					break;
				}
			}
		}
		
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		table.setWidths(new float[] { 1.5f, 1 });
		
		PdfPCell cell;
		
		cell = new PdfPCell(logo);
		cell.setColspan(2);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		
		cell = new PdfPCell(new Phrase("臺北市內湖區安美街181號5樓\n5 F, No. 181, Anmei St., Neihu Dist., Taipei City 114, Taiwan (R.O.C.)\nTelp: (02)8792-8773 HP: 0988-034-210", arialNormal));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		cell = new PdfPCell(img128);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("", arialNormal));
		cell.setColspan(2);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		
		cell = new PdfPCell(new Phrase("To: " + member.member_name + "\n    " + phone_no + "\n    " + sales.ship_address, arialNormal));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase("INVOICE# " + sales.sales_id + "\nDate: " + Stringify.getDate(sales.lm_time_created) + "\nDue: " + Stringify.getDate(dueDate.getTime()) + "\nToP: " + topDate, arialNormal));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);
		
		return table;
	}
	
	private void pdfAddCellForFooter(PdfPTable table, Font font, String c1, String c2, String c3) throws Exception {
		PdfPCell cell = new PdfPCell(new Phrase(c1, font));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(30);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase("", font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c2, font));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase("", font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c3, font));
		table.addCell(cell);
	}
	
	private void pdfAddCellForHeader(PdfPTable table, Font font, String c1, String c2, String c3, String c4, String c5) throws Exception {
		PdfPCell cell = new PdfPCell(new Phrase(c1, font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c2, font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c3, font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c4, font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c5, font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
	}
	
	private void pdfAddCellForHeader(PdfPTable table, Font font, String c1, String c2, String c3, String c4) throws Exception {
		PdfPCell cell = new PdfPCell(new Phrase(c1, font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c2, font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c3, font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c4, font));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
	}
	
	private void pdfAddCellForBody(PdfPTable table, Font font, String c1, String c2, String c3, String c4, String c5, String c6, String c7, String c8) throws Exception {
		PdfPCell cell = new PdfPCell(new Phrase(c1, font));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c2, font));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c3, font));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c4, font));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c5, font));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c6, font));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c7, font));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(c8, font));
		table.addCell(cell);
	}
	
	public int prepareTempTable(FunctionItem fi, Timestamp lm_time_created_start, Timestamp lm_time_created_end, String member_name, String phone_no, String item_desc) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.sales);
			pstmt.executeUpdate();
		}
		
		StringBuilder sb = new StringBuilder();
		
		if (item_desc == null) {
			sb.append("select sales_id from sales where lm_time_created between ? and ?");
			if (member_name != null && phone_no != null) {
				sb.append(" and member_id in (select member_id from member where member_name like ? and (phone_no like ? or app_phone_no like ?))");
			} else if (member_name != null) {
				sb.append(" and member_id in (select member_id from member where member_name like ?)");
			} else if (phone_no != null) {
				sb.append(" and member_id in (select member_id from member where phone_no like ? or app_phone_no like ?)");
			}
		} else {
			sb.append("select a.sales_id from sales a inner join sales_item b on a.sales_id = b.sales_id where a.lm_time_created between ? and ? and b.item_id in (select item_id from item where item_desc like ?)");
			if (member_name != null && phone_no != null) {
				sb.append(" and a.member_id in (select member_id from member where member_name like ? and (phone_no like ? or app_phone_no like ?))");
			} else if (member_name != null) {
				sb.append(" and a.member_id in (select member_id from member where member_name like ?)");
			} else if (phone_no != null) {
				sb.append(" and a.member_id in (select member_id from member where phone_no like ? or app_phone_no like ?)");
			}
		}
		
		List<Long> sales_ids = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sb.toString()))) {
			int pos = 1;
			pstmt.setTimestamp(pos++, lm_time_created_start);
			pstmt.setTimestamp(pos++, lm_time_created_end);
			if (item_desc == null) {
				if (member_name != null && phone_no != null) {
					pstmt.setString(pos++, "%" + member_name + "%");
					pstmt.setString(pos++, "%." + phone_no + "%");
					pstmt.setString(pos++, phone_no + "%");
				} else if (member_name != null) {
					pstmt.setString(pos++, "%" + member_name + "%");
				} else if (phone_no != null) {
					pstmt.setString(pos++, "%." + phone_no + "%");
					pstmt.setString(pos++, phone_no + "%");
				}
			} else {
				pstmt.setString(pos++, "%" + item_desc + "%");
				if (member_name != null && phone_no != null) {
					pstmt.setString(pos++, "%" + member_name + "%");
					pstmt.setString(pos++, "%." + phone_no + "%");
					pstmt.setString(pos++, phone_no + "%");
				} else if (member_name != null) {
					pstmt.setString(pos++, "%" + member_name + "%");
				} else if (phone_no != null) {
					pstmt.setString(pos++, "%." + phone_no + "%");
					pstmt.setString(pos++, phone_no + "%");
				}
			}
			
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					sales_ids.add(r.getLong(1));
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.insert_temp_table_long1()))) {
			for (Long sales_id : sales_ids) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.sales);
				pstmt.setLong(3, sales_id);
				pstmt.executeUpdate();
			}
		}
		
		return sales_ids.size();
	}
	
	public void update(FunctionItem fi, SalesModel m) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		S sql = fi.getSql();
		L l = fi.getLanguage();
		
		Timestamp old_lm_time;
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select lm_time, status_id, shop_id, member_id from sales where sales_id = ? for update"))) {
			pstmt.setLong(1, m.sales_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					old_lm_time = r.getTimestamp(1);
					m.status = SalesStatus.get(r.getInt(2));
					m.shop_id = r.getInt(3);
					m.member_id = r.getLong(4);
				} else {
					throw new Exception(l.data_not_exist(C.sales_id, m.sales_id));
				}
			}
		}
		
		if (old_lm_time.getTime() != m.lm_time.getTime()) {
			throw new Exception(l.data_already_updated_by_another_user(C.sales_id, m.sales_id));
		}
		
		if (m.status != SalesStatus.created) {
			throw new Exception(l.status_not_allowed());
		}
		
		SortedSet<Integer> item_ids = new TreeSet<>();
		for (SalesItemModel item : m.items) {
			item_ids.add(item.item_id);
		}
		
		// check if hidden
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select item_hide from item where item_id = ?"))) {
			for (int item_id : item_ids) {
				pstmt.setInt(1, item_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						int item_hide = r.getInt(1);
						if (item_hide == 1) {
							throw new Exception("item_id = " + item_id + " is hidden, cannot sell");
						}
					} else {
						throw new Exception(l.data_not_exist("item_id = " + item_id));
					}
				}
			}
		}
		
		m.lm_time_created = m.lm_time = fi.getConnection().getCurrentTime();
		m.lm_user = fi.getSessionInfo().getUserName();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update sales set freight_id = ?, ship_address = ?, total_amount = ?, lm_time = ?, lm_user = ?, ship_fee = ?, comment = ?, point_used = ?, lm_time_created = ?, lm_user_created = ? where sales_id = ?"))) {
			pstmt.setInt(1, m.freight.getId());
			pstmt.setString(2, m.ship_address);
			pstmt.setInt(3, m.total_amount);
			pstmt.setTimestamp(4, m.lm_time);
			pstmt.setString(5, m.lm_user);
			pstmt.setInt(6, m.ship_fee);
			pstmt.setString(7, m.comment);
			pstmt.setInt(8, m.point_used);
			pstmt.setTimestamp(9, m.lm_time_created);
			pstmt.setString(10, m.lm_user);
			pstmt.setLong(11, m.sales_id);
			pstmt.executeUpdate();
		}
		
		List<SalesItemModel> listOfReturnItem = new ArrayList<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select item_id, sales_qty from sales_item where sales_id = ?"))) {
			pstmt.setLong(1, m.sales_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					SalesItemModel s = new SalesItemModel();
					s.item_id = r.getInt(1);
					s.sales_qty = r.getInt(2);
					listOfReturnItem.add(s);
				}
			}
		}
		
		// combine with new items
		for (SalesItemModel s : listOfReturnItem) {
			item_ids.add(s.item_id);
		}
		
		// lock and get item quantity from inventory for all items
		Map<Integer, Integer> mapOfInventoryQty = new HashMap<>();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select item_qty from inventory where shop_id = ? and item_id = ? for update"))) {
			for (int item_id : item_ids) {
				pstmt.setInt(1, m.shop_id);
				pstmt.setInt(2, item_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						int item_qty = r.getInt(1);
						mapOfInventoryQty.put(item_id, item_qty);
					} else {
						throw new Exception(l.data_not_exist("shop_id=" + m.shop_id + ", item_id=" + item_id));
					}
				}
			}
		}

		// give back old item quantity to inventory
		for (SalesItemModel s : listOfReturnItem) {
			int item_qty = mapOfInventoryQty.get(s.item_id);
			item_qty += s.sales_qty;
			mapOfInventoryQty.put(s.item_id, item_qty);
		}
		
		// subtract with new item
		for (SalesItemModel s : m.items) {
			int item_qty = mapOfInventoryQty.get(s.item_id);
			s.inventory_old_qty = item_qty;
			s.inventory_diff_qty = -s.sales_qty;
			item_qty -= s.sales_qty;
			s.inventory_new_qty = item_qty;
			mapOfInventoryQty.put(s.item_id, item_qty);
		}
		
		// update inventory
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update inventory set item_qty = ? where shop_id = ? and item_id = ?"))) {
			for (int item_id : item_ids) {
				pstmt.setInt(1, mapOfInventoryQty.get(item_id));
				pstmt.setInt(2, m.shop_id);
				pstmt.setInt(3, item_id);
				pstmt.executeUpdate();
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from sales_item where sales_id = ?"))) {
			pstmt.setLong(1, m.sales_id);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sales_item (sales_id, item_id, sales_qty, sales_price, sales_discount, sales_total, comment) values (?,?,?,?,?,?,?)"))) {
			for (SalesItemModel item : m.items) {
				pstmt.setLong(1, m.sales_id);
				pstmt.setInt(2, item.item_id);
				pstmt.setInt(3, item.sales_qty);
				pstmt.setInt(4, item.sales_price);
				pstmt.setInt(5, item.sales_discount);
				pstmt.setInt(6, item.sales_total);
				pstmt.setString(7, item.comment);
				pstmt.executeUpdate();
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from inventory_history where sales_id = ?"))) {
			pstmt.setLong(1, m.sales_id);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmtHistory = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_history_insert()))) {
			for (SalesItemModel item : m.items) {
				pstmtHistory.setLong(1, Helper.randomSeq());
				pstmtHistory.setInt(2, InventoryHistoryReason.sales_create.getId());
				pstmtHistory.setInt(3, m.shop_id);
				pstmtHistory.setInt(4, item.item_id);
				pstmtHistory.setInt(5, item.inventory_old_qty);
				pstmtHistory.setInt(6, item.inventory_diff_qty);
				pstmtHistory.setInt(7, item.inventory_new_qty);
				pstmtHistory.setTimestamp(8, m.lm_time);
				pstmtHistory.setString(9, m.lm_user);
				pstmtHistory.setLong(10, m.sales_id);
				pstmtHistory.setLong(11, null);
				pstmtHistory.setLong(12, null);
				pstmtHistory.setLong(13, null);
				pstmtHistory.executeUpdate();
			}
		}
		
		int is_wait_confirm;
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select is_wait_confirm from member where member_id = ?"))) {
			pstmt.setLong(1, m.member_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					is_wait_confirm = r.getInt(1);
				} else {
					throw new Exception(l.data_not_exist(C.member_id, String.valueOf(m.member_id)));
				}
			}
		}
		
		if (is_wait_confirm == -1) {
			if (Helper.isNullOrEmpty(m.member_name)) {
				throw new Exception("member name cannot be null");
			}
			if (Helper.isNullOrEmpty(m.member_login_id)) {
				throw new Exception("member login id cannot be null");
			}
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member set member_name = ?, member_login_id = ?, address = ?, phone_no = ? where member_id = ?"))) {
				pstmt.setString(1, m.member_name.toUpperCase());
				pstmt.setString(2, m.member_login_id.toUpperCase());
				pstmt.setString(3, m.ship_address);
				pstmt.setString(4, m.member_phone_no);
				pstmt.setLong(5, m.member_id);
				pstmt.executeUpdate();
			}
		} else {
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member set address = ?, phone_no = ? where member_id = ?"))) {
				pstmt.setString(1, m.ship_address);
				pstmt.setString(2, m.member_phone_no);
				pstmt.setLong(3, m.member_id);
				pstmt.executeUpdate();
			}
		}
	}

}
