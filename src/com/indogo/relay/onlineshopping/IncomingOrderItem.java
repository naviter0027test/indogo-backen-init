package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.indogo.model.onlineshopping.ItemModel;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;

public class IncomingOrderItem implements ITablePage {
	
	private PreparedStatementWrapper pstmtItem;
	private Map<Integer, ItemModel> items;

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.incoming_order_item;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.qty, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.qty()));
		cols.add(new TablePageColumn(C.price_buy, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.price_buy()));
		cols.add(new TablePageColumn(C.discount, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.discount()));
		cols.add(new TablePageColumn(C.total, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.total()));
		cols.add(new TablePageColumn(C.item_image, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString, true));
		cols.add(new TablePageColumn(C.item_name, C.columnTypeString, C.columnDirectionNone, false, true, l.item_name()));
		cols.add(new TablePageColumn(C.item_desc, C.columnTypeString, C.columnDirectionNone, false, true, l.item_desc()));
		cols.add(new TablePageColumn(C.category_name, C.columnTypeString, C.columnDirectionNone, false, true, l.category_name()));
		cols.add(new TablePageColumn(C.color_name, C.columnTypeString, C.columnDirectionNone, false, true, l.color_name()));
		cols.add(new TablePageColumn(C.size_name, C.columnTypeString, C.columnDirectionNone, false, true, l.size_name()));
		cols.add(new TablePageColumn(C.item_filename, C.columnTypeString, C.columnDirectionNone, false, true, l.item_filename(), true, true));
		cols.add(new TablePageColumn(C.order_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.order_id(), true, true));
		cols.add(new TablePageColumn(C.item_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_id(), true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		int item_id = r.unwrap().getInt(C.item_id);
		
		ItemModel item = checkItem(item_id);
		
		cols.get(C.qty).setValue(r.getIntCurrency(C.qty));
		cols.get(C.price_buy).setValue(r.getIntCurrency(C.price_buy));
		cols.get(C.discount).setValue(r.getIntCurrency(C.discount));
		cols.get(C.total).setValue(r.getIntCurrency(C.total));
		cols.get(C.item_name).setValue(item.item_name);
		cols.get(C.item_desc).setValue(item.item_desc);
		cols.get(C.category_name).setValue(item.category_name);
		cols.get(C.color_name).setValue(item.color_name);
		cols.get(C.size_name).setValue(item.size_name);
		cols.get(C.item_filename).setValue(item.item_filename);
		cols.get(C.order_id).setValue(r.getLong(C.order_id));
		cols.get(C.item_id).setValue(String.valueOf(item_id));
	}
	
	private ItemModel checkItem(int item_id) throws Exception {
		ItemModel item = items.get(item_id);
		if (item == null) {
			item = new ItemModel();
			
			pstmtItem.setInt(1, item_id);
			try (ResultSetWrapper r = pstmtItem.executeQueryWrapper()) {
				if (r.next()) {
					item.item_name = r.getString(1);
					item.item_filename = r.getString(3);
					item.category_name = r.getString(4);
					item.color_name = r.getString(5);
					item.size_name = r.getString(6);
					item.item_desc = r.getString(7);
				} else {
					item.item_name = C.emptyString;
					item.item_filename = C.emptyString;
					item.category_name = C.emptyString;
					item.color_name = C.emptyString;
					item.size_name = C.emptyString;
					item.item_desc = C.emptyString;
				}
			}
			
			items.put(item_id, item);
		}
		return item;
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		S sql = fi.getSql();
		
		items = new HashMap<>();
		pstmtItem = new PreparedStatementWrapper(conn.prepareStatement(sql.item_get_for_inventory()));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
