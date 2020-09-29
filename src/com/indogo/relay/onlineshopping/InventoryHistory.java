package com.indogo.relay.onlineshopping;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.indogo.InventoryHistoryReason;
import com.lionpig.language.L;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;

public class InventoryHistory implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.inventory_history;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDesc, true, false, l.lm_time()));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, l.lm_user()));
		cols.add(new TablePageColumn(C.reason_name, C.columnTypeString, C.columnDirectionNone, false, true, "Reason"));
		cols.add(new TablePageColumn(C.item_old_qty, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Before"));
		cols.add(new TablePageColumn(C.item_new_qty, C.columnTypeNumber, C.columnDirectionDefault, true, false, "After"));
		cols.add(new TablePageColumn(C.sales_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Sales Id"));
		cols.add(new TablePageColumn(C.reason_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.reason_id, true, true));
		cols.add(new TablePageColumn(C.shop_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.shop_id, true, true));
		cols.add(new TablePageColumn(C.item_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.item_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		InventoryHistoryReason reason = InventoryHistoryReason.get(r.unwrap().unwrap().getInt(C.reason_id));
		
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.reason_name).setValue(reason.name());
		cols.get(C.item_old_qty).setValue(r.getIntCurrency(C.item_old_qty));
		cols.get(C.item_new_qty).setValue(r.getIntCurrency(C.item_new_qty));
		cols.get(C.sales_id).setValue(r.getLong(C.sales_id));
		cols.get(C.reason_id).setValue(String.valueOf(reason.getId()));
		cols.get(C.shop_id).setValue(r.getInt(C.shop_id));
		cols.get(C.item_id).setValue(r.getInt(C.item_id));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
