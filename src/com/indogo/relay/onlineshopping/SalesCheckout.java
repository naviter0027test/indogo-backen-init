package com.indogo.relay.onlineshopping;

import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.indogo.model.onlineshopping.Freight;
import com.indogo.model.onlineshopping.SalesStatus;
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

public class SalesCheckout extends AbstractFunction implements ITablePage {

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
		cols.add(new TablePageColumn(C.sales_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.sales_id));
		cols.add(new TablePageColumn(C.ship_address, C.columnTypeString, C.columnDirectionDefault, true, false, "Shipping Address"));
		cols.add(new TablePageColumn(C.total_amount, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total"));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, l.lm_time()));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, l.lm_user()));
		cols.add(new TablePageColumn(C.shop_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.shop_id, true, true));
		cols.add(new TablePageColumn(C.status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.status_id, true, true));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, true));
		cols.add(new TablePageColumn(C.freight_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.freight_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.sales_id).setValue(r.getLong(C.sales_id));
		cols.get(C.ship_address).setValue(r.getString(C.ship_address));
		cols.get(C.total_amount).setValue(r.getIntCurrency(C.total_amount));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.shop_id).setValue(r.getInt(C.shop_id));
		cols.get(C.status_id).setValue(r.getInt(C.status_id));
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
		cols.get(C.freight_id).setValue(r.getInt(C.freight_id));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		filter.add(new TablePageFilter(C.status_id, C.columnTypeNumber, C.operationEqual, "2", null));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
	}

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		if (action.equals(C.barcode_id)) {
			List<String> item_names = Helper.getStringArrayByReadLine(params, C.item_name);
			
			Map<Integer, Integer> items = new HashMap<>();
			List<String> listOfItemNamesNoMatch = new ArrayList<>();
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select item_id from item where item_name = ?"))) {
				try (PreparedStatementWrapper pstmtBarcode = new PreparedStatementWrapper(conn.prepareStatement("select item_id from barcode where barcode_id = ?"))) {
					for (String item_name : item_names) {
						Integer item_id = null;
						
						pstmt.setString(1, item_name);
						try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
							if (r.next()) {
								item_id = r.getInt(1);
							}
						}
						
						if (item_id == null) {
							pstmtBarcode.setString(1, item_name);
							try (ResultSetWrapper r = pstmtBarcode.executeQueryWrapper()) {
								if (r.next()) {
									item_id = r.getInt(1);
								}
							}
						}
						
						if (item_id == null) {
							listOfItemNamesNoMatch.add(item_name);
							continue;
						}
						
						Integer qty = items.get(item_id);
						if (qty == null) {
							items.put(item_id, 1);
						} else {
							items.put(item_id, qty + 1);
						}
					}
				}
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(items.size());
			for (Entry<Integer, Integer> item : items.entrySet()) {
				sb.append(C.char_31).append(item.getKey()).append(C.char_31).append(item.getValue());
			}
			sb.append(C.char_31).append(listOfItemNamesNoMatch.size());
			for (String item_name : listOfItemNamesNoMatch) {
				sb.append(C.char_31).append(item_name);
			}
			return sb.toString();
		} else if (action.equals(C.checkout)) {
			long sales_id = Helper.getLong(params, C.sales_id, true);
			String imeis = Helper.getString(params, C.imeis, false);
			String ship_no = Helper.getString(params, C.ship_no, false);
			String invoice_no = Helper.getString(params, C.invoice_no, false);
			
			try {
				if (imeis != null) {
					Map<Integer, List<String>> imeiItems = new HashMap<>();
					String[] tokens = StringUtils.splitPreserveAllTokens(imeis, C.char_31);
					for (String token : tokens) {
						String[] cells = StringUtils.splitPreserveAllTokens(token, C.char_30);
						int item_id = Integer.parseInt(cells[0]);
						List<String> input_imeis = new ArrayList<>();
						try (BufferedReader br = new BufferedReader(new StringReader(cells[1]))) {
							String line = null;
							while ((line = br.readLine()) != null) {
								line = line.trim();
								if (line.length() > 0) {
									input_imeis.add(line);
								}
							}
						}
						imeiItems.put(item_id, input_imeis);
					}
					
					Map<Integer, Integer> imeiQty = new HashMap<>();
					try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.item_id, a.sales_qty from sales_item a, item b, item_category c where a.sales_id = ? and a.item_id = b.item_id and b.category_id = c.category_id and c.imei_flag = 1"))) {
						pstmt.setLong(1, sales_id);
						try (ResultSet r = pstmt.executeQuery()) {
							while (r.next()) {
								imeiQty.put(r.getInt(1), r.getInt(2));
							}
						}
					}
					
					try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sales_imei (imei_id, sales_id, item_id) values (?, ?, ?)"))) {
						for (Entry<Integer, Integer> entry : imeiQty.entrySet()) {
							List<String> input_imeis = imeiItems.get(entry.getKey());
							if (input_imeis == null) {
								throw new Exception("IMEI for item_id = " + entry.getKey() + " not found");
							}
							
							if (entry.getValue() != input_imeis.size()) {
								throw new Exception("expected IMEI quantity is " + entry.getValue() + ", but only " + input_imeis.size() + " given");
							}
							
							for (String imei_id : input_imeis) {
								pstmt.setString(1, imei_id);
								pstmt.setLong(2, sales_id);
								pstmt.setInt(3, entry.getKey());
								pstmt.executeUpdate();
							}
						}
					}
				}
				
				Integer shop_id;
				Freight freight;
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select status_id, shop_id, freight_id from sales where sales_id = ? for update"))) {
					pstmt.setLong(1, sales_id);
					try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
						if (r.next()) {
							SalesStatus status = SalesStatus.get(r.unwrap().getInt(1));
							if (status != SalesStatus.checkout) {
								throw new Exception("status must be Checkout");
							}
							
							shop_id = r.getInt(2);
							freight = Freight.get(r.getInt(3));
						} else {
							throw new Exception(l.data_not_exist("sales_id = " + sales_id));
						}
					}
				}
				
				Set<Integer> userShopIds = Shop.getShopIdsForUser(fi);
				if (!userShopIds.contains(shop_id)) {
					throw new Exception(l.exception_not_shop_owner());
				}
				
				if (freight == Freight.heimao) {
					if (Helper.isNullOrEmpty(ship_no)) {
						throw new Exception("Shipping Number must be provided");
					}
				}
				
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update sales set status_id = ?, lm_time_shipped = sysdate(), lm_time = sysdate(), lm_user = ?, ship_no = ?, invoice_no = ?, lm_user_shipped = ? where sales_id = ?"))) {
					pstmt.setInt(1, SalesStatus.shipped.getId());
					pstmt.setString(2, fi.getSessionInfo().getUserName());
					pstmt.setString(3, ship_no);
					pstmt.setString(4, invoice_no);
					pstmt.setString(5, fi.getSessionInfo().getUserName());
					pstmt.setLong(6, sales_id);
					pstmt.executeUpdate();
				}
				
				conn.commit();
				
				return C.emptyString;
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
		return Shop.getShopListForUser(fi);
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
		long sales_id = Helper.getLong(params, C.sales_id, true);
		
		Connection conn = fi.getConnection().getConnection();
		String ship_address, total_amount, ship_fee, comment;
		long member_id;
		Freight freight;
		Integer shop_id;
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select status_id, ship_address, total_amount, member_id, freight_id, shop_id, ship_fee, comment from sales where sales_id = ?"))) {
			pstmt.setLong(1, sales_id);
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				if (r.next()) {
					int status_id = r.unwrap().unwrap().getInt(1);
					if (status_id != SalesStatus.checkout.getId()) {
						throw new Exception("sales has not exported to heimao");
					}
					
					ship_address = r.getString(2);
					total_amount = r.getIntCurrency(3);
					member_id = r.unwrap().unwrap().getLong(4);
					freight = Freight.get(r.unwrap().unwrap().getInt(5));
					shop_id = r.unwrap().getInt(6);
					ship_fee = r.getIntCurrency(7);
					comment = r.getString(8);
				} else {
					throw new Exception(l.data_not_exist("sales_id = " + sales_id));
				}
			}
		}
		
		Set<Integer> userShopIds = Shop.getShopIdsForUser(fi);
		if (!userShopIds.contains(shop_id)) {
			throw new Exception(l.exception_not_shop_owner());
		}
		
		String member_name, phone_no;
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select member_name, phone_no from member where member_id = ?"))) {
			pstmt.setLong(1, member_id);
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				if (r.next()) {
					member_name = r.getString(1);
					phone_no = r.getString(2);
				} else {
					throw new Exception(l.data_not_exist("member_id = " + member_id));
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(ship_address)
		.append(C.char_31).append(ship_fee)
		.append(C.char_31).append(total_amount)
		.append(C.char_31).append(freight.getName(l))
		.append(C.char_31).append(member_name)
		.append(C.char_31).append(phone_no)
		.append(C.char_31).append(comment);
		
		/**
		 * SELECT a.sales_qty, a.sales_price, a.sales_discount, a.sales_total, b.item_name, b.item_desc, b.item_filename, c.category_name, c.imei_flag, (
SELECT color_name
FROM item_color
WHERE color_id = b.color_id) AS color_name, (
SELECT size_name
FROM item_size
WHERE size_id = b.size_id) AS size_name, b.item_id, a.comment
FROM sales_item a, item b, item_category c
WHERE a.item_id = b.item_id
  AND b.category_id = c.category_id
  AND a.sales_id = ?
		 */
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("SELECT a.sales_qty, a.sales_price, a.sales_discount, a.sales_total, b.item_name, b.item_desc, b.item_filename, c.category_name, c.imei_flag, (\r\n" + 
				"SELECT color_name\r\n" + 
				"FROM item_color\r\n" + 
				"WHERE color_id = b.color_id) AS color_name, (\r\n" + 
				"SELECT size_name\r\n" + 
				"FROM item_size\r\n" + 
				"WHERE size_id = b.size_id) AS size_name, b.item_id, a.comment\r\n" + 
				"FROM sales_item a, item b, item_category c\r\n" + 
				"WHERE a.item_id = b.item_id\r\n" + 
				"  AND b.category_id = c.category_id\r\n" + 
				"  AND a.sales_id = ?"))) {
			pstmt.setLong(1, sales_id);
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				int count = 0;
				StringBuilder items = new StringBuilder();
				while (r.next()) {
					String sales_qty = r.getInt(1);
					String sales_price = r.getIntCurrency(2);
					String sales_discount = r.getIntCurrency(3);
					String sales_total = r.getIntCurrency(4);
					String item_name = r.getString(5);
					String item_desc = r.getString(6);
					String item_filename = r.getString(7);
					String category_name = r.getString(8);
					String imei_flag = r.getInt(9);
					String color_name = r.getString(10);
					String size_name = r.getString(11);
					int item_id = r.unwrap().getInt(12);
					String item_comment = r.getString(13);
					
					String item_image = Item.resolveItemImageUrl(fi, item_id, item_filename);
					
					items.append(C.char_31).append(sales_qty)
					.append(C.char_30).append(sales_price)
					.append(C.char_30).append(sales_discount)
					.append(C.char_30).append(sales_total)
					.append(C.char_30).append(item_name)
					.append(C.char_30).append(item_desc)
					.append(C.char_30).append(item_image)
					.append(C.char_30).append(category_name)
					.append(C.char_30).append(imei_flag)
					.append(C.char_30).append(color_name)
					.append(C.char_30).append(size_name)
					.append(C.char_30).append(item_id)
					.append(C.char_30).append(item_comment);
					
					count++;
				}
				
				sb.append(C.char_31).append(count)
				.append(items);
			}
		}
		
		return sb.toString();
	}

}
