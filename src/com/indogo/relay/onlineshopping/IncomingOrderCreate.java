package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.indogo.InventoryHistoryReason;
import com.indogo.model.onlineshopping.IncomingOrderItemModel;
import com.indogo.model.onlineshopping.IncomingOrderModel;
import com.indogo.model.onlineshopping.InventoryModel;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class IncomingOrderCreate extends AbstractFunction {

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(l.shop_name())
		.append(C.char_31).append(l.item_name())
		.append(C.char_31).append(l.item_qty())
		.append(C.char_31).append(l.price_sale())
		.append(C.char_31).append(l.invoice_no())
		.append(C.char_31).append(l.vendor_name())
		.append(C.char_31).append(l.item_list())
		.append(C.char_31).append(l.qty())
		.append(C.char_31).append(l.price_buy())
		.append(C.char_31).append(l.total())
		.append(C.char_31).append(l.item_image())
		.append(C.char_31).append(l.item_desc())
		.append(C.char_31).append(l.category_name())
		.append(C.char_31).append(l.color_name())
		.append(C.char_31).append(l.size_name())
		.append(C.char_31).append(l.vendor_maintenance_title())
		.append(C.char_31).append(l.contact_person())
		.append(C.char_31).append(l.phone_no())
		.append(C.char_31).append(l.fax_no())
		.append(C.char_31).append(l.address())
		.append(C.char_31).append(l.email())
		.append(C.char_31).append(l.incoming_order_title())
		.append(C.char_31).append(l.button_search_vendor())
		.append(C.char_31).append(l.button_browse_vendor())
		.append(C.char_31).append(l.button_add_item())
		.append(C.char_31).append(l.button_browse_item())
		.append(C.char_31).append(l.button_create())
		.append(C.char_31).append(l.button_confirm())
		.append(C.char_31).append(l.order_id())
		.append(C.char_31).append(l.discount());
		
		sb.append(C.char_31).append(Shop.getShopListForUser(fi));
		
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.item_category_get_all()))) {
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				StringBuilder cc = new StringBuilder();
				int size = 0;
				while (r.next()) {
					cc.append(C.char_31).append(r.getInt(1))
					.append(C.char_31).append(r.getString(2));
					size++;
				}
				sb.append(C.char_31).append(size)
				.append(cc);
			}
		}
		
		return sb.toString();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int shop_id = Helper.getInt(params, C.shop_id, true, l.shop_name());
		int vendor_id = Helper.getInt(params, C.vendor_id, true, l.vendor_name());
		String invoice_no = Helper.getString(params, C.invoice_no, false);
		String[] items = Helper.getStringArray(params, C.items, true);
		String comment = Helper.getString(params, C.comment, false);
		
		int total = 0;
		StringBuilder sb = new StringBuilder();
		IncomingOrderItemModel[] incomingOrderItemModels = new IncomingOrderItemModel[items.length];
		for (int i = 0; i < items.length; i++) {
			String[] cells = StringUtils.splitPreserveAllTokens(items[i], C.char_30);
			IncomingOrderItemModel m = new IncomingOrderItemModel();
			m.item_id = Integer.parseInt(cells[0]);
			m.qty = Integer.parseInt(cells[1]);
			m.price_buy = Integer.parseInt(cells[2]);
			m.discount = Integer.parseInt(cells[3]);
			m.total = Integer.parseInt(cells[4]);
			incomingOrderItemModels[i] = m;
			total += m.total;
			sb.append(m.item_id).append(",");
		}
		sb.delete(sb.length() - 1, sb.length());
		
		IncomingOrderModel incomingOrderModel = new IncomingOrderModel();
		incomingOrderModel.shop_id = shop_id;
		incomingOrderModel.invoice_no = invoice_no;
		incomingOrderModel.vendor_id = vendor_id;
		incomingOrderModel.items = incomingOrderItemModels;
		incomingOrderModel.total = total;
		incomingOrderModel.comment = comment;
		
		Connection conn = fi.getConnection().getConnection();
		try {
			insert(fi, incomingOrderModel);
			
			conn.commit();
			
			return String.valueOf(incomingOrderModel.order_id);
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
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
	
	public void insert(FunctionItem fi, IncomingOrderModel incomingOrderModel) throws Exception {
		L l = fi.getLanguage();
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper p = new PreparedStatementWrapper(conn.prepareStatement(sql.shop_user_check()))) {
			p.setInt(1, incomingOrderModel.shop_id);
			p.setInt(2, fi.getSessionInfo().getUserRowId());
			try (ResultSetWrapper r = p.executeQueryWrapper()) {
				if (r.next()) {
					int count = r.getInt(1);
					if (count == 0) {
						throw new Exception(l.exception_not_shop_owner());
					}
				} else {
					throw new Exception(l.exception_not_shop_owner());
				}
			}
		}
		
		incomingOrderModel.order_id = fi.getConnection().getSeq(C.order_id, true);
		incomingOrderModel.lm_time = fi.getConnection().getCurrentTime();
		incomingOrderModel.lm_user = fi.getSessionInfo().getUserName();
		incomingOrderModel.create_time = incomingOrderModel.lm_time;
		
		// sort item
		List<Integer> itemIds = new ArrayList<>();
		for (IncomingOrderItemModel item : incomingOrderModel.items) {
			itemIds.add(item.item_id);
		}
		Collections.sort(itemIds);
		
		// check if hidden or discontinued
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select item_hide, item_disabled, is_composite from item where item_id = ?"))) {
			for (int item_id : itemIds) {
				pstmt.setInt(1, item_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						int item_hide = r.getInt(1);
						if (item_hide == 1) {
							throw new Exception("item_id = " + item_id + " is hidden, cannot restock");
						}
						
						int item_disabled = r.getInt(2);
						if (item_disabled == 1) {
							throw new Exception("item_id = " + item_id + " is discontinued, cannot restock");
						}
						
						int is_composite = r.getInt(3);
						if (is_composite == 1) {
							throw new Exception("item_id = " + item_id + " is composite items, cannot restock");
						}
					} else {
						throw new Exception(l.data_not_exist("item_id = " + item_id));
					}
				}
			}
		}
		
		HashMap<Integer, InventoryModel> inventories = new HashMap<>();
		try (PreparedStatementWrapper pstmtInventoryLock = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_select_for_update()))) {
			for (int itemId : itemIds) {
				pstmtInventoryLock.setInt(1, incomingOrderModel.shop_id);
				pstmtInventoryLock.setInt(2, itemId);
				ResultSet r = pstmtInventoryLock.executeQuery();
				try {
					if (r.next()) {
						int itemQty = r.getInt(1);
						InventoryModel m = new InventoryModel();
						m.item_id = itemId;
						m.shop_id = incomingOrderModel.shop_id;
						m.item_qty = itemQty;
						inventories.put(itemId, m);
					}
				} finally {
					r.close();
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.incoming_order_insert()))) {
			pstmt.setLong(1, incomingOrderModel.order_id);
			pstmt.setTimestamp(2, incomingOrderModel.create_time);
			pstmt.setInt(3, incomingOrderModel.vendor_id);
			pstmt.setString(4, incomingOrderModel.invoice_no);
			pstmt.setInt(5, incomingOrderModel.total);
			pstmt.setString(6, incomingOrderModel.comment);
			pstmt.setInt(7, incomingOrderModel.shop_id);
			pstmt.setTimestamp(8, incomingOrderModel.lm_time);
			pstmt.setString(9, incomingOrderModel.lm_user);
			pstmt.setString(10, incomingOrderModel.lm_user);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmtItem = new PreparedStatementWrapper(conn.prepareStatement(sql.incoming_order_item_insert()))) {
			try (PreparedStatementWrapper pstmtInventoryUpdate = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_update()))) {
				try (PreparedStatementWrapper pstmtInventoryInsert = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_insert()))) {
					try (PreparedStatementWrapper pstmtHistory = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_history_insert()))) {
						for (IncomingOrderItemModel item : incomingOrderModel.items) {
							pstmtItem.setLong(1, incomingOrderModel.order_id);
							pstmtItem.setInt(2, item.item_id);
							pstmtItem.setInt(3, item.qty);
							pstmtItem.setInt(4, item.price_buy);
							pstmtItem.setInt(5, item.discount);
							pstmtItem.setInt(6, item.total);
							pstmtItem.executeUpdate();
							
							int item_old_qty, item_diff_qty, item_new_qty;
							InventoryModel m = inventories.get(item.item_id);
							if (m == null) {
								item_old_qty = 0;
								item_diff_qty = item.qty;
								item_new_qty = item.qty;
								
								pstmtInventoryInsert.setInt(1, incomingOrderModel.shop_id);
								pstmtInventoryInsert.setInt(2, item.item_id);
								pstmtInventoryInsert.setInt(3, item.qty);
								pstmtInventoryInsert.setTimestamp(4, incomingOrderModel.lm_time);
								pstmtInventoryInsert.setString(5, incomingOrderModel.lm_user);
								pstmtInventoryInsert.executeUpdate();
							} else {
								item_old_qty = m.item_qty;
								item_diff_qty = item.qty;
								item_new_qty = m.item_qty + item.qty;
								
								m.item_qty = item_new_qty;
								
								pstmtInventoryUpdate.setInt(1, m.item_qty);
								pstmtInventoryUpdate.setTimestamp(2, incomingOrderModel.lm_time);
								pstmtInventoryUpdate.setString(3, incomingOrderModel.lm_user);
								pstmtInventoryUpdate.setInt(4, m.shop_id);
								pstmtInventoryUpdate.setInt(5, m.item_id);
								pstmtInventoryUpdate.executeUpdate();
							}

							pstmtHistory.setLong(1, Helper.randomSeq());
							pstmtHistory.setInt(2, InventoryHistoryReason.incoming_order_create.getId());
							pstmtHistory.setInt(3, incomingOrderModel.shop_id);
							pstmtHistory.setInt(4, item.item_id);
							pstmtHistory.setInt(5, item_old_qty);
							pstmtHistory.setInt(6, item_diff_qty);
							pstmtHistory.setInt(7, item_new_qty);
							pstmtHistory.setTimestamp(8, incomingOrderModel.lm_time);
							pstmtHistory.setString(9, incomingOrderModel.lm_user);
							pstmtHistory.setLong(10, null);
							pstmtHistory.setLong(11, incomingOrderModel.order_id);
							pstmtHistory.setLong(12, null);
							pstmtHistory.setLong(13, null);
							pstmtHistory.executeUpdate();
						}
					}
				}
			}
		}
	}

}
