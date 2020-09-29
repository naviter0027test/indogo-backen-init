package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

public class ItemGift extends AbstractFunction implements ITablePage {
	
	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.item_v;
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
		cols.add(new TablePageColumn(C.category_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.category_name()));
		cols.add(new TablePageColumn(C.color_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.color_name()));
		cols.add(new TablePageColumn(C.size_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.size_name()));
		cols.add(new TablePageColumn(C.location, C.columnTypeString, C.columnDirectionDefault, true, false, l.location()));
		cols.add(new TablePageColumn(C.expired_date, C.columnTypeDate, C.columnDirectionDefault, true, false, l.expired_date()));
		cols.add(new TablePageColumn(C.item_point, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_point()));
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
		cols.add(new TablePageColumn(C.has_gift, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.has_gift, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		int item_id = r.unwrap().unwrap().getInt(C.item_id);
		String item_filename = r.unwrap().getString(C.item_filename);
		
		cols.get(C.action).setValue(C.emptyString);
		cols.get(C.item_image).setValue(Item.resolveItemImageUrl(fi, item_id, item_filename));
		cols.get(C.item_name).setValue(r.getString(C.item_name));
		cols.get(C.item_desc).setValue(r.getString(C.item_desc));
		cols.get(C.price_sale).setValue(r.getIntCurrency(C.price_sale));
		cols.get(C.barcode_id).setValue(C.emptyString);
		cols.get(C.category_name).setValue(r.getString(C.category_name));
		cols.get(C.color_name).setValue(r.getString(C.color_name));
		cols.get(C.size_name).setValue(r.getString(C.size_name));
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

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		filter.add(new TablePageFilter(C.has_gift, C.columnTypeNumber, C.operationEqual, "1", null));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		if (action.equals(C.get_gifts)) {
			int shop_id = Helper.getInt(params, C.shop_id, true);
			int item_id = Helper.getInt(params, C.item_id, true);
			
			List<GiftInfo> list = getGifts(fi, shop_id, item_id);
			
			StringBuilder sb = new StringBuilder();
			sb.append(list.size());
			for (GiftInfo item : list) {
				sb.append(C.char_31).append(item.gift_id)
				.append(C.char_31).append(item.gift_qty)
				.append(C.char_31).append(item.current_qty)
				.append(C.char_31).append(item.item_image)
				.append(C.char_31).append(item.item_name)
				.append(C.char_31).append(item.item_desc)
				.append(C.char_31).append(item.category_name)
				.append(C.char_31).append(item.color_name)
				.append(C.char_31).append(item.size_name);
			}
			return sb.toString();
		}
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int item_id = Helper.getInt(params, C.item_id, true);
		int[] gift_ids = Helper.getIntArray(params, C.gift_ids, true);
		int[] gift_qtys = Helper.getIntArray(params, C.gift_qtys, true);
		
		Gift[] gifts = new Gift[gift_ids.length];
		for (int i = 0; i < gifts.length; i++) {
			Gift g = new Gift();
			g.id = gift_ids[i];
			g.qty = gift_qtys[i];
			gifts[i] = g;
		}
		
		Connection conn = fi.getConnection().getConnection();
		try {
			insert(fi, item_id, gifts);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.item_id, C.columnTypeNumber, C.operationEqual, String.valueOf(item_id), null));
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
	protected String onUpdate(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int item_id = Helper.getInt(params, C.item_id, true);
		int[] gift_ids = Helper.getIntArray(params, C.gift_ids, true);
		int[] gift_qtys = Helper.getIntArray(params, C.gift_qtys, true);
		
		Gift[] gifts = new Gift[gift_ids.length];
		for (int i = 0; i < gifts.length; i++) {
			Gift g = new Gift();
			g.id = gift_ids[i];
			g.qty = gift_qtys[i];
			gifts[i] = g;
		}
		
		Connection conn = fi.getConnection().getConnection();
		try {
			update(fi, item_id, gifts);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.item_id, C.columnTypeNumber, C.operationEqual, String.valueOf(item_id), null));
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
		int item_id = Helper.getInt(params, C.item_id, true);
		Connection conn = fi.getConnection().getConnection();
		try {
			delete(fi, item_id);
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
		return null;
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int item_id = Helper.getInt(params, C.item_id, true);
		ItemInfo item = get(fi, item_id);
		StringBuilder sb = new StringBuilder();
		sb.append("<tr>")
		.append("<td class=\"item_id\">").append(item_id).append("</td>")
		.append("<td class=\"item_image\">").append(item.item_image).append("</td>")
		.append("<td class=\"item_name\">").append(item.item_name).append("</td>")
		.append("<td class=\"item_desc\">").append(item.item_desc).append("</td>")
		.append("<td class=\"price_sale\">").append(item.price_sale).append("</td>")
		.append("<td class=\"category_name\">").append(item.category_name).append("</td>")
		.append("<td class=\"color_name\">").append(item.color_name).append("</td>")
		.append("<td class=\"size_name\">").append(item.size_name).append("</td>")
		.append("</tr>")
		.append(C.char_31).append(item.gifts.size());
		for (ItemDetailInfo d : item.gifts) {
			sb.append(C.char_31).append("<tr>")
			.append("<td class=\"item_id\">").append(d.gift_id).append("</td>")
			.append("<td class=\"qty\">").append(d.gift_qty).append("</td>")
			.append("<td class=\"item_image\">").append(d.item_image).append("</td>")
			.append("<td class=\"item_name\">").append(d.item_name).append("</td>")
			.append("<td class=\"item_desc\">").append(d.item_desc).append("</td>")
			.append("<td class=\"price_sale\">").append(d.price_sale).append("</td>")
			.append("<td class=\"category_name\">").append(d.category_name).append("</td>")
			.append("<td class=\"color_name\">").append(d.color_name).append("</td>")
			.append("<td class=\"size_name\">").append(d.size_name).append("</td>")
			.append("</tr>");
		}
		return sb.toString();
	}
	
	private class Gift {
		public int id;
		public int qty;
	}
	
	public void insert(FunctionItem fi, int item_id, Gift[] gifts) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		L l = fi.getLanguage();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select has_gift from item where item_id = ? for update"))) {
			pstmt.setInt(1, item_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					int has_gift = r.getInt(1);
					if (has_gift == 1) {
						throw new Exception("gift configuration already exist");
					}
				} else {
					throw new Exception(l.data_not_exist(C.item_id, item_id));
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into item_gift (item_id, gift_id, gift_qty) values (?,?,?)"))) {
			for (Gift g : gifts) {
				pstmt.setInt(1, item_id);
				pstmt.setInt(2, g.id);
				pstmt.setInt(3, g.qty);
				pstmt.executeUpdate();
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update item set has_gift = 1 where item_id = ?"))) {
			pstmt.setInt(1, item_id);
			pstmt.executeUpdate();
		}
	}
	
	public void update(FunctionItem fi, int item_id, Gift[] gifts) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		L l = fi.getLanguage();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select has_gift from item where item_id = ? for update"))) {
			pstmt.setInt(1, item_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					int has_gift = r.getInt(1);
					if (has_gift != 1) {
						throw new Exception("gift configuration not exist");
					}
				} else {
					throw new Exception(l.data_not_exist(C.item_id, item_id));
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from item_gift where item_id = ?"))) {
			pstmt.setInt(1, item_id);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into item_gift (item_id, gift_id, gift_qty) values (?,?,?)"))) {
			for (Gift g : gifts) {
				pstmt.setInt(1, item_id);
				pstmt.setInt(2, g.id);
				pstmt.setInt(3, g.qty);
				pstmt.executeUpdate();
			}
		}
	}
	
	public void delete(FunctionItem fi, int item_id) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		L l = fi.getLanguage();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select has_gift from item where item_id = ? for update"))) {
			pstmt.setInt(1, item_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					int has_gift = r.getInt(1);
					if (has_gift != 1) {
						throw new Exception("gift configuration not exist");
					}
				} else {
					throw new Exception(l.data_not_exist(C.item_id, item_id));
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from item_gift where item_id = ?"))) {
			pstmt.setInt(1, item_id);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update item set has_gift = 0 where item_id = ?"))) {
			pstmt.setInt(1, item_id);
			pstmt.executeUpdate();
		}
	}
	
	private class ItemDetailInfo {
		public int gift_id;
		public int gift_qty;
		public String item_image;
		public String item_name;
		public String item_desc;
		public int price_sale;
		public String category_name;
		public String color_name;
		public String size_name;
	}
	
	private class ItemInfo {
		public String item_image;
		public String item_name;
		public String item_desc;
		public int price_sale;
		public String category_name;
		public String color_name;
		public String size_name;
		public List<ItemDetailInfo> gifts;
	}
	
	private class GiftInfo {
		public int gift_id;
		public int gift_qty;
		public int current_qty;
		public String item_image;
		public String item_name;
		public String item_desc;
		public String category_name;
		public String color_name;
		public String size_name;
	}
	
	public ItemInfo get(FunctionItem fi, int item_id) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		L l = fi.getLanguage();
		
		ItemInfo item = new ItemInfo();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select item_filename, item_name, item_desc, price_sale, category_name, color_name, size_name from item_v where item_id = ?"))) {
			pstmt.setInt(1, item_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					item.item_image = Item.resolveItemImageUrl(fi, item_id, r.getString(1));
					item.item_name = r.getString(2);
					item.item_desc = r.getString(3);
					item.price_sale = r.getInt(4);
					item.category_name = r.getString(5);
					item.color_name = r.getString(6);
					item.size_name = r.getString(7);
					item.gifts = new ArrayList<>();
				} else {
					throw new Exception(l.data_not_exist(C.item_id, item_id));
				}
			}
		}
		
		/*
		 * select a.gift_id, a.gift_qty, b.item_filename, b.item_name, b.item_desc, b.category_name, b.color_name, b.size_name
 from item_gift a
inner join item_v b on b.item_id = a.gift_id
where a.item_id = ?
		 */
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.gift_id, a.gift_qty, b.item_filename, b.item_name, b.item_desc, b.category_name, b.color_name, b.size_name\r\n" + 
				" from item_gift a\r\n" + 
				"inner join item_v b on b.item_id = a.gift_id\r\n" + 
				"where a.item_id = ?"))) {
			pstmt.setInt(1, item_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					ItemDetailInfo d = new ItemDetailInfo();
					d.gift_id = r.getInt(1);
					d.gift_qty = r.getInt(2);
					d.item_image = Item.resolveItemImageUrl(fi, d.gift_id, r.getString(3));
					d.item_name = r.getString(4);
					d.item_desc = r.getString(5);
					d.category_name = r.getString(6);
					d.color_name = r.getString(7);
					d.size_name = r.getString(8);
					item.gifts.add(d);
				}
			}
		}
		
		return item;
	}
	
	public List<GiftInfo> getGifts(FunctionItem fi, int shop_id, int item_id) throws Exception {
		List<GiftInfo> list = new ArrayList<>();
		
		Connection conn = fi.getConnection().getConnection();
		/*
		 * select a.gift_qty, b.item_filename, b.item_name, b.item_desc, b.category_name, b.color_name, b.size_name, c.item_qty, c.shop_id
 from item_gift a
inner join item_v b on b.item_id = a.gift_id
 left join inventory c on c.item_id = b.item_id
where a.item_id = ?
  and c.shop_id = ?
		 */
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.gift_id, a.gift_qty, b.item_filename, b.item_name, b.item_desc, b.category_name, b.color_name, b.size_name, c.item_qty\r\n" + 
				" from item_gift a\r\n" + 
				"inner join item_v b on b.item_id = a.gift_id\r\n" + 
				" left join inventory c on c.item_id = b.item_id\r\n" + 
				"where a.item_id = ?\r\n" + 
				"  and c.shop_id = ?"))) {
			pstmt.setInt(1, item_id);
			pstmt.setInt(2, shop_id);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					GiftInfo item = new GiftInfo();
					item.gift_id = r.getInt(1);
					item.gift_qty = r.getInt(2);
					item.item_image = Item.resolveItemImageUrl(fi, item.gift_id, r.getString(3));
					item.item_name = r.getString(4);
					item.item_desc = r.getString(5);
					item.category_name = r.getString(6);
					item.color_name = r.getString(7);
					item.size_name = r.getString(8);
					item.current_qty = r.getInt(9);
					list.add(item);
				}
			}
		}
		
		return list;
	}
}
