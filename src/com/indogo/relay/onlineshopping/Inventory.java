package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.InventoryHistoryReason;
import com.indogo.model.onlineshopping.InventoryAdjustItemModel;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.database.RoleNameListModel;
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

public class Inventory extends AbstractFunction implements ITablePage {
	
	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.inventory_v;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.item_image, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString, true));
		cols.add(new TablePageColumn(C.item_desc, C.columnTypeString, C.columnDirectionDefault, true, false, l.item_desc()));
		cols.add(new TablePageColumn(C.color_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.color_name()));
		cols.add(new TablePageColumn(C.size_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.size_name()));
		cols.add(new TablePageColumn(C.item_qty, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_qty()));
		cols.add(new TablePageColumn(C.pending_qty, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Pending Qty"));
		cols.add(new TablePageColumn(C.price_sale, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.price_sale()));
		cols.add(new TablePageColumn(C.item_point, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_point()));
		cols.add(new TablePageColumn(C.category_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.category_name()));
		cols.add(new TablePageColumn(C.item_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.item_name()));
		cols.add(new TablePageColumn(C.shop_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.shop_name()));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, l.lm_time()));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, l.lm_user()));
		cols.add(new TablePageColumn(C.shop_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.shop_id(), true, true));
		cols.add(new TablePageColumn(C.item_id, C.columnTypeNumber, C.columnDirectionAsc, true, false, l.item_id(), true, true));
		cols.add(new TablePageColumn(C.item_disabled, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_disabled(), false, true));
		cols.add(new TablePageColumn(C.item_hide, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_hide(), false, true));
		cols.add(new TablePageColumn(C.is_composite, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_composite, false, true));
		cols.add(new TablePageColumn(C.item_filename, C.columnTypeString, C.columnDirectionDefault, true, false, C.item_filename, false, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.item_image).setValue(Item.resolveItemImageUrl(fi, r.unwrap().getInt(C.item_id), r.unwrap().getString(C.item_filename)));
		cols.get(C.item_desc).setValue(r.getString(C.item_desc));
		cols.get(C.color_name).setValue(r.getString(C.color_name));
		cols.get(C.size_name).setValue(r.getString(C.size_name));
		cols.get(C.item_qty).setValue(r.getIntCurrency(C.item_qty));
		cols.get(C.pending_qty).setValue(r.getIntCurrency(C.pending_qty));
		cols.get(C.price_sale).setValue(r.getIntCurrency(C.price_sale));
		cols.get(C.item_point).setValue(r.getIntCurrency(C.item_point));
		cols.get(C.category_name).setValue(r.getString(C.category_name));
		cols.get(C.item_name).setValue(r.getString(C.item_name));
		cols.get(C.shop_name).setValue(r.getString(C.shop_name));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.shop_id).setValue(r.getInt(C.shop_id));
		cols.get(C.item_id).setValue(r.getInt(C.item_id));
		cols.get(C.item_disabled).setValue(r.getInt(C.item_disabled));
		cols.get(C.item_hide).setValue(r.getInt(C.item_hide));
		cols.get(C.is_composite).setValue(r.getInt(C.is_composite));
		cols.get(C.item_filename).setValue(r.getString(C.item_filename));
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
		if (action.equals(C.sql_temp_table)) {
			List<String> item_names = Helper.getStringArrayByReadLine(params, C.item_name);
			Integer item_disabled = Helper.getIntNullable(params, C.item_disabled, false);
			Integer item_hide = Helper.getIntNullable(params, C.item_hide, false);
			String item_desc = Helper.getString(params, C.item_desc, false);
			Integer category_id = Helper.getIntNullable(params, C.category_id, false);
			
			Connection conn = fi.getConnection().getConnection();
			try {
				int count = sqlTempTable(fi, item_names, item_disabled, item_hide, item_desc, category_id);
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
		.append(C.char_31).append(l.item_name())
		.append(C.char_31).append(Shop.getShopList(fi))
		.append(C.char_31).append(ItemCategory.getAll(fi))
		.append(C.char_31).append(isAllowedToEditQty(fi) ? 1 : 0);
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
		
		try {
			int shop_id = Helper.getInt(params, C.shop_id, true);
			int item_id = Helper.getInt(params, C.item_id, true);
			int item_qty = Helper.getInt(params, C.item_qty, true);
			int old_qty = Helper.getInt(params, C.old_qty, true);
			
			Set<Integer> userShopIds = Shop.getShopIdsForUser(fi);
			if (!userShopIds.contains(shop_id)) {
				throw new Exception(l.exception_not_shop_owner());
			}
			
			if (!isAllowedToEditQty(fi)) {
				throw new Exception(l.update_not_allowed());
			}
			
			boolean is_composite;
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select is_composite from item where item_id = ?"))) {
				pstmt.setInt(1, item_id);
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					if (r.next()) {
						is_composite = r.getInt(1) == 1;
					} else {
						throw new Exception(l.data_not_exist(C.item_id, String.valueOf(item_id)));
					}
				}
			}
			
			List<Integer> item_ids = new ArrayList<>();
			Map<Integer, InventoryAdjustItemModel> items = new HashMap<>();
			
			InventoryAdjustItemModel m = new InventoryAdjustItemModel();
			m.item_id = item_id;
			m.is_composite = is_composite;
			m.new_qty = item_qty;
			
			item_ids.add(m.item_id);
			items.put(m.item_id, m);
			
			if (is_composite) {
				try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("select item_id, item_qty from item_composite where composite_id = ?"))) {
					pstmt.setInt(1, item_id);
					try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
						while (r.next()) {
							m = new InventoryAdjustItemModel();
							m.composite_id = item_id;
							m.item_id = r.getInt(1);
							m.is_composite = false;
							m.qty_taken_for_composite = r.getInt(2);
							
							item_ids.add(m.item_id);
							items.put(m.item_id, m);
						}
					}
				}
			}
			
			Collections.sort(item_ids);
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(s.inventory_select_for_update()))) {
				for (int i : item_ids) {
					m = items.get(i);
					pstmt.setInt(1, shop_id);
					pstmt.setInt(2, i);
					try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
						if (r.next()) {
							m.is_new_item = false;
							m.old_qty = r.getInt(1);
						} else {
							m.is_new_item = true;
							m.old_qty = 0;
						}
					}
				}
			}
			
			if (items.get(item_id).old_qty != old_qty) {
				throw new Exception("product quantity mismatch, please refresh to again to get the correct quantity");
			}
			
			if (is_composite) {
				// only for composite item
				// if parent composite item qty changed
				// the child composite items qty need to recalculate difference
				for (int i : item_ids) {
					m = items.get(i);
					if (m.composite_id != null) {
						InventoryAdjustItemModel composite = items.get(m.composite_id);
						int composite_qty_diff = composite.new_qty - composite.old_qty;
						m.new_qty = m.old_qty - (m.qty_taken_for_composite * composite_qty_diff);
					}
				}
			}
			
			Timestamp lm_time = fi.getConnection().getCurrentTime();
			String lm_user = fi.getSessionInfo().getUserName();

			try (PreparedStatementWrapper pstmtIns = new PreparedStatementWrapper(conn.prepareStatement(s.inventory_insert()))) {
				try (PreparedStatementWrapper pstmtUpd = new PreparedStatementWrapper(conn.prepareStatement(s.inventory_update()))) {
					for (int i : item_ids) {
						m = items.get(i);
						if (m.is_new_item) {
							pstmtIns.setInt(1, shop_id);
							pstmtIns.setInt(2, m.item_id);
							pstmtIns.setInt(3, m.new_qty);
							pstmtIns.setTimestamp(4, lm_time);
							pstmtIns.setString(5, lm_user);
							pstmtIns.executeUpdate();
						} else {
							pstmtUpd.setInt(1, m.new_qty);
							pstmtUpd.setTimestamp(2, lm_time);
							pstmtUpd.setString(3, lm_user);
							pstmtUpd.setInt(4, shop_id);
							pstmtUpd.setInt(5, m.item_id);
							pstmtUpd.executeUpdate();
						}
					}
				}
			}

			try (PreparedStatementWrapper pstmtHistory = new PreparedStatementWrapper(conn.prepareStatement(s.inventory_history_insert()))) {
				for (int i : item_ids) {
					m = items.get(i);
					pstmtHistory.setLong(1, Helper.randomSeq());
					pstmtHistory.setInt(2, InventoryHistoryReason.manual_edit.getId());
					pstmtHistory.setInt(3, shop_id);
					pstmtHistory.setInt(4, m.item_id);
					pstmtHistory.setInt(5, m.old_qty);
					pstmtHistory.setInt(6, m.new_qty - m.old_qty);
					pstmtHistory.setInt(7, m.new_qty);
					pstmtHistory.setTimestamp(8, lm_time);
					pstmtHistory.setString(9, lm_user);
					pstmtHistory.setLong(10, null);
					pstmtHistory.setLong(11, null);
					pstmtHistory.setLong(12, null);
					pstmtHistory.setLong(13, null);
					pstmtHistory.executeUpdate();
				}
			}
			
			conn.commit();
			return Stringify.getTimestamp(lm_time) + C.char_31 + lm_user + C.char_31 + Stringify.getCurrency(item_qty);
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
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
		
		return null;
	}
	
	private int sqlTempTable(FunctionItem fi, List<String> item_names, Integer item_disabled, Integer item_hide, String item_desc, Integer category_id) throws Exception {
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.clear_temp_table()))) {
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.inventory);
			pstmt.executeUpdate();
			
			pstmt.setString(1, fi.getSID());
			pstmt.setString(2, C.inventory_item_names);
			pstmt.executeUpdate();
		}
			
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.insert_temp_table_item1()))) {
			for (String item_name : item_names) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.inventory_item_names);
				pstmt.setString(3, item_name);
				pstmt.executeUpdate();
			}
		}
		
		List<Integer> item_ids = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		if (item_names.size() > 0) {
			sb.append("select distinct a.item_id from item a, sql_temp_table b where a.item_name = b.item1 and b.session_id = ? and b.page_id = ?");
		} else {
			sb.append("select a.item_id from item a where 1=1");
		}
		
		if (item_disabled != null) {
			sb.append(" and a.item_disabled = ?");
		}
		if (item_hide != null) {
			sb.append(" and a.item_hide = ?");
		}
		if (item_desc != null) {
			sb.append(" and a.item_desc like ?");
		}
		if (category_id != null) {
			sb.append(" and a.category_id = ?");
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sb.toString()))) {
			int i = 1;
			if (item_names.size() > 0) {
				pstmt.setString(i++, fi.getSID());
				pstmt.setString(i++, C.inventory_item_names);
			}
			if (item_disabled != null) {
				pstmt.setInt(i++, item_disabled);
			}
			if (item_hide != null) {
				pstmt.setInt(i++, item_hide);
			}
			if (item_desc != null) {
				pstmt.setString(i++, "%" + item_desc + "%");
			}
			if (category_id != null) {
				pstmt.setInt(i++, category_id);
			}
			
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				while (r.next()) {
					item_ids.add(r.getInt(1));
				}
			}
		}
		
		if (item_names.size() > 0) {
			sb = new StringBuilder();
			if (item_disabled != null || item_hide != null) {
				sb.append("select distinct a.item_id from barcode a, sql_temp_table b, item c where a.barcode_id = b.item1 and a.item_id = c.item_id and b.session_id = ? and b.page_id = ?");
			} else {
				sb.append("select distinct a.item_id from barcode a, sql_temp_table b where a.barcode_id = b.item1 and b.session_id = ? and b.page_id = ?");
			}
			
			if (item_disabled != null) {
				sb.append(" and c.item_disabled = ?");
			}
			if (item_hide != null) {
				sb.append(" and c.item_hide = ?");
			}
			if (item_desc != null) {
				sb.append(" and a.item_desc like ?");
			}
			if (category_id != null) {
				sb.append(" and a.category_id = ?");
			}
			
			try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sb.toString()))) {
				int i = 1;
				pstmt.setString(i++, fi.getSID());
				pstmt.setString(i++, C.inventory_item_names);
				if (item_disabled != null) {
					pstmt.setInt(i++, item_disabled);
				}
				if (item_hide != null) {
					pstmt.setInt(i++, item_hide);
				}
				if (item_desc != null) {
					pstmt.setString(i++, "%" + item_desc + "%");
				}
				if (category_id != null) {
					pstmt.setInt(i++, category_id);
				}
				
				try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
					while (r.next()) {
						item_ids.add(r.getInt(1));
					}
				}
			}
		}
		
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.insert_temp_table_long1()))) {
			for (Integer item_id : item_ids) {
				pstmt.setString(1, fi.getSID());
				pstmt.setString(2, C.inventory);
				pstmt.setInt(3, item_id);
				pstmt.executeUpdate();
			}
		}
		
		return item_ids.size();
	}
	
	public boolean isAllowedToEditQty(FunctionItem fi) throws Exception {
		List<RoleNameListModel> roles = fi.getConnection().adminUserGetRoles(fi.getSessionInfo().getUserName());
		String[] tokens = fi.getConnection().getGlobalConfig(C.inventory, C.allow_edit_qty_role_ids).split(",");
		Set<Integer> userRoleIds = new HashSet<>();
		for (RoleNameListModel role : roles) {
			userRoleIds.add(role.ROLE_ID);
		}
		boolean isAllowedToEditQty = false;
		for (String token : tokens) {
			int allowedRoleId = Integer.parseInt(token);
			if (userRoleIds.contains(allowedRoleId)) {
				isAllowedToEditQty = true;
				break;
			}
		}
		return isAllowedToEditQty;
	}

}
