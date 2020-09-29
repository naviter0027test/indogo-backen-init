package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.InventoryHistoryReason;
import com.indogo.model.onlineshopping.IncomingOrderItemModel;
import com.indogo.model.onlineshopping.IncomingOrderModel;
import com.indogo.model.onlineshopping.IncomingOrderStatus;
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

public class IncomingOrder extends AbstractFunction implements ITablePage {
	
	private PreparedStatementWrapper pstmtShop, pstmtVendor;
	private Map<Integer, String> shopNames, vendorNames;

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.incoming_order;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.action, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString));
		cols.add(new TablePageColumn(C.shop_name, C.columnTypeString, C.columnDirectionNone, false, true, l.shop_name()));
		cols.add(new TablePageColumn(C.create_time, C.columnTypeDateTime, C.columnDirectionDesc, true, false, l.create_time()));
		cols.add(new TablePageColumn(C.vendor_name, C.columnTypeString, C.columnDirectionNone, false, true, l.vendor_name()));
		cols.add(new TablePageColumn(C.invoice_no, C.columnTypeString, C.columnDirectionDefault, true, false, l.invoice_no()));
		cols.add(new TablePageColumn(C.total, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.total()));
		cols.add(new TablePageColumn(C.comment, C.columnTypeString, C.columnDirectionDefault, true, false, l.comment()));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, l.lm_time()));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, l.lm_user()));
		cols.add(new TablePageColumn(C.order_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.order_id()));
		cols.add(new TablePageColumn(C.status_name, C.columnTypeString, C.columnDirectionNone, false, true, l.status_name()));
		cols.add(new TablePageColumn(C.vendor_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.vendor_id(), true, true));
		cols.add(new TablePageColumn(C.shop_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.shop_id(), true, true));
		cols.add(new TablePageColumn(C.status_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.status_id(), true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		L l = fi.getLanguage();
		int shop_id = r.unwrap().getInt(C.shop_id);
		int vendor_id = r.unwrap().getInt(C.vendor_id);
		IncomingOrderStatus status = IncomingOrderStatus.get(r.unwrap().getInt(C.status_id));
		
		String shop_name = checkShopName(shop_id);
		String vendor_name = checkVendorName(vendor_id);
		
		cols.get(C.shop_name).setValue(shop_name);
		cols.get(C.create_time).setValue(r.getTimestamp(C.create_time));
		cols.get(C.vendor_name).setValue(vendor_name);
		cols.get(C.invoice_no).setValue(r.getString(C.invoice_no));
		cols.get(C.total).setValue(r.getIntCurrency(C.total));
		cols.get(C.comment).setValue(r.getString(C.comment));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.order_id).setValue(r.getInt(C.order_id));
		cols.get(C.status_name).setValue(status.getName(l));
		cols.get(C.vendor_id).setValue(String.valueOf(vendor_id));
		cols.get(C.shop_id).setValue(String.valueOf(shop_id));
		cols.get(C.status_id).setValue(String.valueOf(status.getId()));
	}
	
	private String checkShopName(int shop_id) throws Exception {
		String shop_name = shopNames.get(shop_id);
		if (shop_name == null) {
			pstmtShop.setInt(1, shop_id);
			try (ResultSetWrapper rs = pstmtShop.executeQueryWrapper()) {
				if (rs.next()) {
					shop_name = rs.getString(1);
				} else {
					shop_name = C.emptyString;
				}
			}
			shopNames.put(shop_id, shop_name);
		}
		return shop_name;
	}
	
	private String checkVendorName(int vendor_id) throws Exception {
		String vendor_name = vendorNames.get(vendor_id);
		if (vendor_name == null) {
			pstmtVendor.setInt(1, vendor_id);
			try (ResultSetWrapper r = pstmtVendor.executeQueryWrapper()) {
				if (r.next()) {
					vendor_name = r.getString(1);
				} else {
					vendor_name = C.emptyString;
				}
			}
			vendorNames.put(vendor_id, vendor_name);
		}
		return vendor_name;
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		shopNames = new HashMap<>();
		vendorNames = new HashMap<>();
		
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		pstmtShop = new PreparedStatementWrapper(conn.prepareStatement(sql.shop_get_for_inventory()));
		pstmtVendor = new PreparedStatementWrapper(conn.prepareStatement(sql.vendor_get_for_inventory()));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		if (action.equals(C.sql_temp_table)) {
			Timestamp create_time_start = Helper.getTimestamp(params, C.start_time, true);
			Timestamp create_time_end = Helper.getTimestamp(params, C.end_time, true);
			String vendor_name = Helper.getString(params, C.vendor_name, false);
			String item_desc = Helper.getString(params, C.item_desc, false);
			
			Connection conn = fi.getConnection().getConnection();
			try {
				int count = sqlTempTable(fi, create_time_start, create_time_end, vendor_name, item_desc);
				conn.commit();
				return String.valueOf(count);
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
		
		sb.append(l.shop_name())
		.append(C.char_31).append(l.create_time())
		.append(C.char_31).append(l.status_name())
		.append(C.char_31).append(IncomingOrderStatus.created.getName(l))
		.append(C.char_31).append(IncomingOrderStatus.deleted.getName(l))
		.append(C.char_31).append(l.button_search())
		.append(C.char_31).append(l.button_cancel())
		.append(C.char_31).append(l.button_show_incoming_order_items())
		.append(C.char_31).append(l.button_close())
		.append(C.char_31).append(l.invoice_no())
		.append(C.char_31).append(l.vendor_name());
		
		sb.append(C.char_31).append(Shop.getShopListForUser(fi));
		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		long order_id = Helper.getLong(params, C.order_id, true);
		Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
		String lm_user = Helper.getString(params, C.lm_user, true);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			IncomingOrderModel incomingOrderModel = delete(fi, order_id, lm_time, lm_user);
			
			conn.commit();
			
			StringBuilder sb = new StringBuilder();
			sb.append(Stringify.getTimestamp(incomingOrderModel.lm_time))
			.append(C.char_31).append(incomingOrderModel.lm_user)
			.append(C.char_31).append(incomingOrderModel.status.getId())
			.append(C.char_31).append(incomingOrderModel.status.getName(l));
			return sb.toString();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public IncomingOrderModel delete(FunctionItem fi, long order_id, Timestamp lm_time, String lm_user) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		L l = fi.getLanguage();
		S sql = fi.getSql();
		
		int shop_id;
		Timestamp old_lm_time;
		String old_lm_user;
		IncomingOrderStatus status;
		try (PreparedStatementWrapper p = new PreparedStatementWrapper(conn.prepareStatement(sql.incoming_order_get_for_update()))) {
			p.setLong(1, order_id);
			try (ResultSetWrapper r = p.executeQueryWrapper()) {
				if (r.next()) {
					shop_id = r.getInt(1);
					old_lm_time = r.getTimestamp(2);
					old_lm_user = r.getString(3);
					status = IncomingOrderStatus.get(r.getInt(4));
				} else {
					throw new Exception(l.data_not_exist(C.order_id, String.valueOf(order_id)));
				}
			}
		}
		
		if (lm_time.getTime() != old_lm_time.getTime() || !lm_user.equals(old_lm_user)) {
			throw new Exception(l.data_already_updated_by_another_user(C.order_id, String.valueOf(order_id)));
		}
		
		if (status != IncomingOrderStatus.created) {
			throw new Exception(l.status_not_allowed());
		}
		
		Timestamp new_lm_time = fi.getConnection().getCurrentTime();
		String new_lm_user = fi.getSessionInfo().getUserName();
		
		try (PreparedStatementWrapper p = new PreparedStatementWrapper(conn.prepareStatement(sql.shop_user_check()))) {
			p.setInt(1, shop_id);
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
		
		int[] item_ids;
		Map<Integer, IncomingOrderItemModel> incomingOrderItems = new HashMap<>();
		try (PreparedStatementWrapper p = new PreparedStatementWrapper(conn.prepareStatement(sql.incoming_order_get_all_items()))) {
			p.setLong(1, order_id);
			try (ResultSetWrapper r = p.executeQueryWrapper()) {
				List<IncomingOrderItemModel> list = new ArrayList<>();
				while (r.next()) {
					IncomingOrderItemModel m = new IncomingOrderItemModel();
					m.item_id = r.getInt(1);
					m.qty = r.getInt(2);
					list.add(m);
				}
				
				item_ids = new int[list.size()];
				for (int i = 0; i < item_ids.length; i++) {
					IncomingOrderItemModel m = list.get(i);
					item_ids[i] = m.item_id;
					incomingOrderItems.put(m.item_id, m);
				}
			}
		}
		
		Arrays.sort(item_ids);
		
		Map<Integer, Integer> inventoryQtys = new HashMap<>();
		try (PreparedStatementWrapper p = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_select_for_update()))) {
			for (int item_id : item_ids) {
				p.setInt(1, shop_id);
				p.setInt(2, item_id);
				try (ResultSetWrapper r = p.executeQueryWrapper()) {
					if (r.next()) {
						inventoryQtys.put(item_id, r.getInt(1));
					} else {
						throw new Exception(l.data_not_exist("shop_id = " + shop_id + ", item_id = " + item_id));
					}
				}
			}
		}
		
		// remove item qty from inventory
		try (PreparedStatementWrapper p = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_update()))) {
			try (PreparedStatementWrapper pstmtHistory = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_history_insert()))) {
				for (int item_id : item_ids) {
					int item_old_qty = inventoryQtys.get(item_id);
					int item_diff_qty = -incomingOrderItems.get(item_id).qty;
					int item_new_qty = item_old_qty + item_diff_qty;
					
					p.setInt(1, item_new_qty);
					p.setTimestamp(2, new_lm_time);
					p.setString(3, new_lm_user);
					p.setInt(4, shop_id);
					p.setInt(5, item_id);
					p.executeUpdate();
					
					pstmtHistory.setLong(1, Helper.randomSeq());
					pstmtHistory.setInt(2, InventoryHistoryReason.incoming_order_cancel.getId());
					pstmtHistory.setInt(3, shop_id);
					pstmtHistory.setInt(4, item_id);
					pstmtHistory.setInt(5, item_old_qty);
					pstmtHistory.setInt(6, item_diff_qty);
					pstmtHistory.setInt(7, item_new_qty);
					pstmtHistory.setTimestamp(8, new_lm_time);
					pstmtHistory.setString(9, new_lm_user);
					pstmtHistory.setLong(10, null);
					pstmtHistory.setLong(11, order_id);
					pstmtHistory.setLong(12, null);
					pstmtHistory.setLong(13, null);
					pstmtHistory.executeUpdate();
				}
			}
		}
		
		// change status to deleted
		try (PreparedStatementWrapper p = new PreparedStatementWrapper(conn.prepareStatement(sql.incoming_order_update()))) {
			p.setInt(1, IncomingOrderStatus.deleted.getId());
			p.setTimestamp(2, new_lm_time);
			p.setString(3, new_lm_user);
			p.setLong(4, order_id);
			p.executeUpdate();
		}
		
		IncomingOrderModel incomingOrderModel = new IncomingOrderModel();
		incomingOrderModel.lm_time = new_lm_time;
		incomingOrderModel.lm_user = new_lm_user;
		incomingOrderModel.status = IncomingOrderStatus.deleted;
		return incomingOrderModel;
	}
	
	private int sqlTempTable(FunctionItem fi, Timestamp create_time_start, Timestamp create_time_end, String vendor_name, String item_desc) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.incoming_order);
			pstmt.executeUpdate();
		}
		
		Set<Long> order_ids = new HashSet<>();
		
		if (!Helper.isNullOrEmpty(vendor_name)) {
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select a.order_id from incoming_order a, vendor b where a.vendor_id = b.vendor_id and a.create_time >= ? and a.create_time <= ? and b.vendor_name like ?"))) {
				pstmt.setTimestamp(1, create_time_start);
				pstmt.setTimestamp(2, create_time_end);
				pstmt.setString(3, "%" + vendor_name + "%");
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					while (r.next()) {
						order_ids.add(r.getLong(1));
					}
				}
			}
		}
		
		if (!Helper.isNullOrEmpty(item_desc)) {
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select distinct a.order_id from incoming_order a, incoming_order_item b, item c where a.order_id = b.order_id and b.item_id = c.item_id and a.create_time >= ? and a.create_time <= ? and c.item_desc like ?"))) {
				pstmt.setTimestamp(1, create_time_start);
				pstmt.setTimestamp(2, create_time_end);
				pstmt.setString(3, "%" + item_desc + "%");
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					while (r.next()) {
						order_ids.add(r.getLong(1));
					}
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.insert_temp_table_long1()))) {
			for (Long order_id : order_ids) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.incoming_order);
				pstmt.setLong(3, order_id);
				pstmt.executeUpdate();
			}
		}
		
		return order_ids.size();
	}

}
