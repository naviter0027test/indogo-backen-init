package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.indogo.InventoryHistoryReason;
import com.indogo.model.onlineshopping.Freight;
import com.indogo.model.onlineshopping.SalesItemModel;
import com.indogo.model.onlineshopping.SalesModel;
import com.indogo.model.onlineshopping.SalesStatus;
import com.indogo.relay.member.MemberConfiguration;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class SalesCreate extends AbstractFunction {

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		if (action.equals(C.member_data)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.member_get_for_sales_create()))) {
				pstmt.setLong(1, member_id);
				
				StringBuilder sb = new StringBuilder();
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQueryWrapper())) {
					if (r.next()) {
						sb.append(r.getString(1))
						.append(C.char_31).append(r.getString(2))
						.append(C.char_31).append(r.getString(3))
						.append(C.char_31).append(r.getInt(4))
						.append(C.char_31).append(r.getString(5))
						.append(C.char_31).append(r.getInt(6))
						.append(C.char_31).append(r.getInt(7));
					} else {
						throw new Exception(l.data_not_exist("member_id = " + member_id));
					}
				}
				return sb.toString();
			}
		} else if (action.equals(C.member_sales_history)) {
			long member_id = Helper.getLong(params, C.member_id, true);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select lm_time_created, status_id, (select shop_name from shop where shop_id = a.shop_id) as shop_name, sales_id, total_amount from sales a where member_id = ? order by lm_time_created desc limit 5"))) {
				pstmt.setLong(1, member_id);
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
					StringBuilder sb = new StringBuilder();
					while (r.next()) {
						sb.append(r.getTimestamp(1))
						.append(C.char_30).append(SalesStatus.get(r.unwrap().unwrap().getInt(2)).getName(l))
						.append(C.char_30).append(r.getString(3))
						.append(C.char_30).append(r.getLong(4))
						.append(C.char_30).append(r.getIntCurrency(5))
						.append(C.char_31);
					}
					if (sb.length() > 0) {
						sb.delete(sb.length() - 1, sb.length());
					}
					return sb.toString();
				}
			}
		} else if (action.equals(C.sales_item)) {
			long sales_id = Helper.getLong(params, C.sales_id, true);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select b.item_name, a.sales_qty, a.sales_discount, a.comment, b.item_id from sales_item a, item b where a.item_id = b.item_id and a.sales_id = ?"))) {
				pstmt.setLong(1, sales_id);
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
					StringBuilder sb = new StringBuilder();
					while (r.next()) {
						sb.append(r.getString(1))
						.append(C.char_30).append(r.getInt(2))
						.append(C.char_30).append(r.getInt(3))
						.append(C.char_30).append(r.getString(4))
						.append(C.char_30).append(r.getInt(5))
						.append(C.char_31);
					}
					if (sb.length() > 0) {
						sb.delete(sb.length() - 1, sb.length());
					}
					return sb.toString();
				}
			}
		} else {
			throw new Exception(l.unknown_action(action));
		}
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
		int shop_id = Helper.getInt(params, C.shop_id, true, l.shop_name());
		long member_id = Helper.getLong(params, C.member_id, true);
		String ship_address = Helper.getString(params, C.ship_address, true);
		String[] items = Helper.getStringArray(params, C.items, true);
		int freight_id = Helper.getInt(params, C.freight_id, true);
		int ship_fee = Helper.getInt(params, C.ship_fee, true);
		String comment = Helper.getString(params, C.comment, false);
		Integer point_used = Helper.getIntNullable(params, C.point_used, false);
		String phone_no = Helper.getString(params, C.phone_no, true);
		String member_login_id = Helper.getString(params, C.member_login_id, false);
		String member_name = Helper.getString(params, C.member_name, false);
		Integer wallet_used = Helper.getIntNullable(params, C.wallet_used, false);
		
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
		
		if (wallet_used != null) {
			if (wallet_used.intValue() > 0) {
				total_amount -= wallet_used.intValue();
			} else {
				wallet_used = null;
			}
		}
		
		SalesModel salesModel = new SalesModel();
		salesModel.shop_id = shop_id;
		salesModel.member_id = member_id;
		salesModel.ship_address = ship_address;
		salesModel.total_amount = total_amount;
		salesModel.items = salesItems;
		salesModel.freight = Freight.get(freight_id);
		salesModel.ship_fee = ship_fee;
		salesModel.comment = comment;
		salesModel.point_used = point_used;
		salesModel.member_phone_no = phone_no;
		salesModel.member_login_id = member_login_id;
		salesModel.member_name = member_name;
		salesModel.wallet_used = wallet_used;
		
		Connection conn = fi.getConnection().getConnection();
		try {
			insert(fi, salesModel);
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
		
		return String.valueOf(salesModel.sales_id);
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
	
	public void insert(FunctionItem fi, SalesModel salesModel) throws Exception {
		L l = fi.getLanguage();
		S sql = fi.getSql();
		
		if (salesModel.freight == null) {
			throw new Exception("freight_id must be selected");
		}
		
		if (salesModel.point_used != null && salesModel.point_used < 0) {
			throw new Exception("Point Used must be a positive value (>= 0)");
		}
		
		salesModel.lm_time = fi.getConnection().getCurrentTime();
		salesModel.lm_user = fi.getSessionInfo().getUserName();
		
		switch (salesModel.freight) {
			case cash:
				salesModel.status = SalesStatus.paid;
				break;
			case post:
				salesModel.status = SalesStatus.checkout;
				break;
			default:
				salesModel.status = SalesStatus.created;
				break;
		}
		
		Calendar c = Calendar.getInstance();
		c.setTime(salesModel.lm_time);
		StringBuilder sb = new StringBuilder(15);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DAY_OF_MONTH);
		sb.append(year).delete(0, 2).append(StringUtils.leftPad(String.valueOf(month), 2, '0')).append(StringUtils.leftPad(String.valueOf(date), 2, '0'));
		long seq = fi.getConnection().getSeq("sales_id_" + sb.toString(), true);
		sb.append(StringUtils.leftPad(String.valueOf(seq), 5, '0'));
		salesModel.sales_id = Long.parseLong(sb.toString());
		
		Connection conn = fi.getConnection().getConnection();
		
		List<Integer> item_ids = new ArrayList<>();
		Map<Integer, SalesItemModel> cache = new HashMap<>();
		for (SalesItemModel item : salesModel.items) {
			item_ids.add(item.item_id);
			cache.put(item.item_id, item);
		}
		Collections.sort(item_ids);
		
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
		
		int member_remit_point = 0, member_wallet = 0;
		// if member_id = 0, then its a new member
		if (salesModel.member_id == 0) {
			if (Helper.isNullOrEmpty(salesModel.member_name)) {
				throw new Exception("member name cannot empty");
			}
			if (Helper.isNullOrEmpty(salesModel.member_login_id)) {
				throw new Exception("app login id cannot empty");
			}
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into member (member_id, member_name, member_login_id, phone_no, address, lm_time, lm_user, is_wait_confirm) values (?,?,?,?,?,?,?,?)"))) {
				salesModel.member_id = fi.getConnection().getSeq(C.member_id, true);
				pstmt.setLong(1, salesModel.member_id);
				pstmt.setString(2, salesModel.member_name.toUpperCase());
				pstmt.setString(3, salesModel.member_login_id.toUpperCase());
				pstmt.setString(4, salesModel.member_phone_no);
				pstmt.setString(5, salesModel.ship_address);
				pstmt.setTimestamp(6, salesModel.lm_time);
				pstmt.setString(7, salesModel.lm_user);
				pstmt.setInt(8, -1);
				pstmt.executeUpdate();
			}
		} else {
			// if is_wait_confirm == -1 then we should update the member data, because this member is created from sales_create
			int is_wait_confirm;
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select is_wait_confirm, remit_point, wallet from member where member_id = ? for update"))) {
				pstmt.setLong(1, salesModel.member_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						is_wait_confirm = r.getInt(1);
						member_remit_point = r.getInt(2);
						member_wallet = r.getInt(3);
					} else {
						throw new Exception(l.data_not_exist(C.member_id, String.valueOf(salesModel.member_id)));
					}
				}
			}
			
			if (is_wait_confirm == -1) {
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member set member_name = ?, member_login_id = ? where member_id = ?"))) {
					pstmt.setString(1, salesModel.member_name.toUpperCase());
					pstmt.setString(2, salesModel.member_login_id.toUpperCase());
					pstmt.setLong(3, salesModel.member_id);
					pstmt.executeUpdate();
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sales (sales_id, shop_id, status_id, member_id, freight_id, ship_address, total_amount, lm_time, lm_user, lm_time_created, ship_fee, comment, point_used, lm_user_created, wallet_used) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"))) {
			pstmt.setLong(1, salesModel.sales_id);
			pstmt.setInt(2, salesModel.shop_id);
			pstmt.setInt(3, salesModel.status.getId());
			pstmt.setLong(4, salesModel.member_id);
			pstmt.setInt(5, salesModel.freight.getId());
			pstmt.setString(6, salesModel.ship_address);
			pstmt.setInt(7, salesModel.total_amount);
			pstmt.setTimestamp(8, salesModel.lm_time);
			pstmt.setString(9, salesModel.lm_user);
			pstmt.setTimestamp(10, salesModel.lm_time);
			pstmt.setInt(11, salesModel.ship_fee);
			pstmt.setString(12, salesModel.comment);
			pstmt.setInt(13, salesModel.point_used);
			pstmt.setString(14, salesModel.lm_user);
			pstmt.setInt(15, salesModel.wallet_used);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into sales_item (sales_id, item_id, sales_qty, sales_price, sales_discount, sales_total, comment) values (?,?,?,?,?,?,?)"))) {
			for (SalesItemModel item : salesModel.items) {
				pstmt.setLong(1, salesModel.sales_id);
				pstmt.setInt(2, item.item_id);
				pstmt.setInt(3, item.sales_qty);
				pstmt.setInt(4, item.sales_price);
				pstmt.setInt(5, item.sales_discount);
				pstmt.setInt(6, item.sales_total);
				pstmt.setString(7, item.comment);
				pstmt.executeUpdate();
			}
		}
		
		// update inventories
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_select_for_update()))) {
			try (PreparedStatementWrapper pstmtUpd = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_update()))) {
				try (PreparedStatementWrapper pstmtHistory = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_history_insert()))) {
					for (int item_id : item_ids) {
						pstmt.setInt(1, salesModel.shop_id);
						pstmt.setInt(2, item_id);
						int item_old_qty;
						try (ResultSet r = pstmt.executeQuery()) {
							if (r.next()) {
								item_old_qty = r.getInt(1);
							} else {
								throw new Exception("inventory for item id [" + item_id + "] not exist");
							}
						}
						
						int item_diff_qty = -cache.get(item_id).sales_qty;
						int item_new_qty = item_old_qty + item_diff_qty;
						
						pstmtUpd.setInt(1, item_new_qty);
						pstmtUpd.setTimestamp(2, salesModel.lm_time);
						pstmtUpd.setString(3, salesModel.lm_user);
						pstmtUpd.setInt(4, salesModel.shop_id);
						pstmtUpd.setInt(5, item_id);
						pstmtUpd.executeUpdate();
						
						pstmtHistory.setLong(1, Helper.randomSeq());
						pstmtHistory.setInt(2, InventoryHistoryReason.sales_create.getId());
						pstmtHistory.setInt(3, salesModel.shop_id);
						pstmtHistory.setInt(4, item_id);
						pstmtHistory.setInt(5, item_old_qty);
						pstmtHistory.setInt(6, item_diff_qty);
						pstmtHistory.setInt(7, item_new_qty);
						pstmtHistory.setTimestamp(8, salesModel.lm_time);
						pstmtHistory.setString(9, salesModel.lm_user);
						pstmtHistory.setLong(10, salesModel.sales_id);
						pstmtHistory.setLong(11, null);
						pstmtHistory.setLong(12, null);
						pstmtHistory.setLong(13, null);
						pstmtHistory.executeUpdate();
					}
				}
			}
		}
		
		if (salesModel.point_used != null && salesModel.point_used > 0) {
			member_remit_point = member_remit_point - salesModel.point_used;
			if (member_remit_point < 0) {
				throw new Exception("Not enough point to use");
			}
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member set remit_point = ? where member_id = ?"))) {
				pstmt.setInt(1, member_remit_point);
				pstmt.setLong(2, salesModel.member_id);
				pstmt.executeUpdate();
			}
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into member_point (member_id, lm_time, remit_point, sales_id, reason_id) values (?,?,?,?,10)"))) {
				pstmt.setLong(1, salesModel.member_id);
				pstmt.setTimestamp(2, salesModel.lm_time);
				pstmt.setInt(3, -salesModel.point_used);
				pstmt.setLong(4, salesModel.sales_id);
				pstmt.executeUpdate();
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member set address = ?, phone_no = ? where member_id = ?"))) {
			pstmt.setString(1, salesModel.ship_address);
			pstmt.setString(2, salesModel.member_phone_no);
			pstmt.setLong(3, salesModel.member_id);
			pstmt.executeUpdate();
		}
		
		if (salesModel.wallet_used != null && salesModel.wallet_used > 0) {
			member_wallet = member_wallet - salesModel.wallet_used;
			if (member_wallet < 0) {
				throw new Exception("Not enough money in the wallet to use");
			}
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member set wallet = ? where member_id = ?"))) {
				pstmt.setInt(1, member_wallet);
				pstmt.setLong(2, salesModel.member_id);
				pstmt.executeUpdate();
			}
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into member_wallet_hst (member_id, lm_time, wallet, sales_id) values (?,?,?,?)"))) {
				pstmt.setLong(1, salesModel.member_id);
				pstmt.setTimestamp(2, salesModel.lm_time);
				pstmt.setInt(3, -salesModel.wallet_used);
				pstmt.setLong(4, salesModel.sales_id);
				pstmt.executeUpdate();
			}
		}
	}

}
