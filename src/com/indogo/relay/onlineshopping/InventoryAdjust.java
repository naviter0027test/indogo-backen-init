package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.indogo.InventoryHistoryReason;
import com.indogo.model.onlineshopping.InventoryAdjustItemModel;
import com.indogo.model.onlineshopping.InventoryAdjustModel;
import com.indogo.model.onlineshopping.ItemCompositeModel;
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

public class InventoryAdjust extends AbstractFunction implements ITablePage {
	
	private HashMap<Integer, String> shops = null;
	private PreparedStatementWrapper pstmtShop = null;

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.inventory_adjust;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.adjust_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, "No."));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, "Create Time"));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, "User"));
		cols.add(new TablePageColumn(C.shop_name, C.columnTypeString, C.columnDirectionNone, false, true, "Shop"));
		cols.add(new TablePageColumn(C.shop_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.shop_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		Integer shop_id = r.unwrap().getInt(C.shop_id);
		
		String shop_name = shops.get(shop_id);
		if (shop_name == null) {
			pstmtShop.setInt(1, shop_id);
			ResultSetWrapper rr = pstmtShop.executeQueryWrapper();
			try {
				rr.next();
				shop_name = rr.getString(1);
				if (shop_name == null)
					shop_name = C.emptyString;
			} finally {
				rr.close();
			}
			shops.put(shop_id, shop_name);
		}
		
		cols.get(C.adjust_id).setValue(r.getLong(C.adjust_id));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.shop_name).setValue(shop_name);
		cols.get(C.shop_id).setValue(String.valueOf(shop_id));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		S sql = fi.getSql();
		pstmtShop = new PreparedStatementWrapper(conn.prepareStatement(sql.shop_get_for_inventory()));
		shops = new HashMap<>();
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		pstmtShop.close();
	}

	@Override
	protected String onExecute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		if (action.equals(C.get_current_qty)) {
			int shop_id = Helper.getInt(params, C.shop_id, true);
			int item_id = Helper.getInt(params, C.item_id, true);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.inventory_select()))) {
				pstmt.setInt(1, shop_id);
				pstmt.setInt(2, item_id);
				try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
					if (r.next()) {
						return r.getInt(1);
					} else {
						return "0";
					}
				}
			}
		}
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(Shop.getShopListForUser(fi))
		.append(C.char_31).append(l.shop_name())
		.append(C.char_31).append(l.item_list())
		.append(C.char_31).append(l.item_desc())
		.append(C.char_31).append(l.item_name())
		.append(C.char_31).append(l.button_add_item())
		.append(C.char_31).append(l.button_browse_item())
		.append(C.char_31).append(l.current_qty())
		.append(C.char_31).append(l.adjust_qty())
		.append(C.char_31).append(l.item_image())
		.append(C.char_31).append(l.category_name())
		.append(C.char_31).append(l.color_name())
		.append(C.char_31).append(l.size_name());
		return sb.toString();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int shop_id = Helper.getInt(params, C.shop_id, true);
		String[] items = Helper.getStringArray(params, C.items, true);
		
		InventoryAdjustModel m = new InventoryAdjustModel();
		m.shop_id = shop_id;
		m.items = new InventoryAdjustItemModel[items.length];
		for (int i = 0; i < items.length; i++) {
			String[] cells = StringUtils.splitPreserveAllTokens(items[i], C.char_30);
			m.items[i] = new InventoryAdjustItemModel();
			m.items[i].item_id = Integer.parseInt(cells[0]);
			m.items[i].new_qty = Integer.parseInt(cells[1]);
			m.items[i].comment = cells[2];
		}
		
		Connection conn = fi.getConnection().getConnection();
		try {
			insert(fi, m);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.adjust_id, C.columnTypeNumber, C.operationEqual, String.valueOf(m.adjust_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, getColumns(fi), null, filter, null, null, null);
			
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
	
	public void insert(FunctionItem fi, InventoryAdjustModel m) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		S sql = fi.getSql();
		L l = fi.getLanguage();
		
		m.adjust_id = fi.getConnection().getSeq(C.adjust_id, true);
		m.lm_time = fi.getConnection().getCurrentTime();
		m.lm_user = fi.getSessionInfo().getUserName();
		
		List<Integer> item_ids = new ArrayList<>();
		Map<Integer, InventoryAdjustItemModel> items = new HashMap<>();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select is_composite from item where item_id = ?"))) {
			for (InventoryAdjustItemModel item : m.items) {
				pstmt.setInt(1, item.item_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						item_ids.add(item.item_id);
						items.put(item.item_id, item);
						
						item.is_composite = r.getInt(1) == 1;
					} else {
						throw new Exception(l.data_not_exist(C.item_id, String.valueOf(item.item_id)));
					}
				}
			}
		}
		
		// if child composite items already exist in inventory adjust input parameters
		// then it should be considered as user knowingly set the child composite item qty based on real inventory
		try (PreparedStatementWrapper pstmtComposite = new PreparedStatementWrapper(conn.prepareStatement("select item_id, item_qty from item_composite where composite_id = ?"))) {
			for (InventoryAdjustItemModel item : m.items) {
				if (item.is_composite) {
					pstmtComposite.setInt(1, item.item_id);
					try (ResultSetWrapper r = pstmtComposite.executeQueryWrapper()) {
						while (r.next()) {
							ItemCompositeModel icm = new ItemCompositeModel();
							icm.item_id = r.getInt(1);
							icm.item_qty = r.getInt(2);
							
							if (!items.containsKey(icm.item_id)) {
								InventoryAdjustItemModel newItem = new InventoryAdjustItemModel();
								newItem.composite_id = item.item_id;
								newItem.item_id = icm.item_id;
								newItem.qty_taken_for_composite = icm.item_qty;
								
								item_ids.add(icm.item_id);
								items.put(icm.item_id, newItem);
							}
						}
					}
				}
			}
		}
		
		Collections.sort(item_ids);
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_select_for_update()))) {
			for (int item_id : item_ids) {
				pstmt.setInt(1, m.shop_id);
				pstmt.setInt(2, item_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					InventoryAdjustItemModel item = items.get(item_id);
					if (r.next()) {
						item.old_qty = r.getInt(1);
						item.is_new_item = false;
					} else {
						item.old_qty = 0;
						item.is_new_item = true;
					}
				}
			}
		}
		
		// only for composite items
		// if parent composite item qty changed
		// the child composite items qty need to recalculate difference
		for (int item_id : item_ids) {
			InventoryAdjustItemModel item = items.get(item_id);
			if (item.composite_id != null) {
				InventoryAdjustItemModel composite = items.get(item.composite_id);
				int composite_qty_diff = composite.new_qty - composite.old_qty;
				item.new_qty = item.old_qty - (item.qty_taken_for_composite * composite_qty_diff);
			}
		}
		
		try (PreparedStatementWrapper pstmtInventoryInsert = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_insert()))) {
			try (PreparedStatementWrapper pstmtInventoryUpdate = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_update()))) {
				for (int item_id : item_ids) {
					InventoryAdjustItemModel item = items.get(item_id);
					if (item.is_new_item) {
						pstmtInventoryInsert.setInt(1, m.shop_id);
						pstmtInventoryInsert.setInt(2, item.item_id);
						pstmtInventoryInsert.setInt(3, item.new_qty);
						pstmtInventoryInsert.setTimestamp(4, m.lm_time);
						pstmtInventoryInsert.setString(5, m.lm_user);
						pstmtInventoryInsert.executeUpdate();
					} else {
						pstmtInventoryUpdate.setInt(1, item.new_qty);
						pstmtInventoryUpdate.setTimestamp(2, m.lm_time);
						pstmtInventoryUpdate.setString(3, m.lm_user);
						pstmtInventoryUpdate.setInt(4, m.shop_id);
						pstmtInventoryUpdate.setInt(5, item.item_id);
						pstmtInventoryUpdate.executeUpdate();
					}
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into inventory_adjust (adjust_id, shop_id, lm_time, lm_user) values (?,?,?,?)"))) {
			pstmt.setLong(1, m.adjust_id);
			pstmt.setInt(2, m.shop_id);
			pstmt.setTimestamp(3, m.lm_time);
			pstmt.setString(4, m.lm_user);
			pstmt.executeUpdate();
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into inventory_adjust_item (adjust_id, item_id, old_qty, new_qty, comment) values (?,?,?,?,?)"))) {
			for (int item_id : item_ids) {
				InventoryAdjustItemModel item = items.get(item_id);
				pstmt.setLong(1, m.adjust_id);
				pstmt.setInt(2, item.item_id);
				pstmt.setInt(3, item.old_qty);
				pstmt.setInt(4, item.new_qty);
				pstmt.setString(5, item.comment);
				pstmt.executeUpdate();
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.inventory_history_insert()))) {
			for (int item_id : item_ids) {
				InventoryAdjustItemModel item = items.get(item_id);
				pstmt.setLong(1, Helper.randomSeq());
				pstmt.setInt(2, InventoryHistoryReason.mass_adjust.getId());
				pstmt.setInt(3, m.shop_id);
				pstmt.setInt(4, item.item_id);
				pstmt.setInt(5, item.old_qty);
				pstmt.setInt(6, item.new_qty - item.old_qty);
				pstmt.setInt(7, item.new_qty);
				pstmt.setTimestamp(8, m.lm_time);
				pstmt.setString(9, m.lm_user);
				pstmt.setLong(10, null);
				pstmt.setLong(11, null);
				pstmt.setLong(12, m.adjust_id);
				pstmt.setLong(13, null);
				pstmt.executeUpdate();
			}
		}
	}

}
