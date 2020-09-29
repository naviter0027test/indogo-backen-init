package com.indogo.relay.onlineshopping;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.AbstractFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Stringify;

public class ItemViewForIncomingOrder extends AbstractFunction implements ITablePage {

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
		cols.add(new TablePageColumn(C.item_image, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString, true));
		cols.add(new TablePageColumn(C.item_desc, C.columnTypeString, C.columnDirectionDefault, true, false, l.item_desc()));
		cols.add(new TablePageColumn(C.category_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.category_name()));
		cols.add(new TablePageColumn(C.color_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.color_name()));
		cols.add(new TablePageColumn(C.size_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.size_name()));
		cols.add(new TablePageColumn(C.price_sale, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.price_sale()));
		cols.add(new TablePageColumn(C.barcode_id, C.columnTypeString, C.columnDirectionNone, false, true, "Vendor Barcode"));
		cols.add(new TablePageColumn(C.item_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.item_name()));
		cols.add(new TablePageColumn(C.item_filename, C.columnTypeString, C.columnDirectionDefault, true, false, l.item_filename(), true, true));
		cols.add(new TablePageColumn(C.item_disabled, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_disabled(), true, true));
		cols.add(new TablePageColumn(C.item_hide, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_hide(), true, true));
		cols.add(new TablePageColumn(C.item_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.item_id(), true, true));
		cols.add(new TablePageColumn(C.category_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.category_id(), true, true));
		cols.add(new TablePageColumn(C.color_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.color_id(), true, true));
		cols.add(new TablePageColumn(C.size_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.size_id(), true, true));
		cols.add(new TablePageColumn(C.is_composite, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.is_composite, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		int item_id = r.unwrap().unwrap().getInt(C.item_id);
		String item_filename = r.unwrap().getString(C.item_filename);
		int category_id = r.unwrap().getInt(C.category_id).intValue();
		Integer color_id = r.unwrap().getInt(C.color_id);
		Integer size_id = r.unwrap().getInt(C.size_id);
		
		cols.get(C.item_image).setValue(Item.resolveItemImageUrl(fi, item_id, item_filename));
		cols.get(C.item_desc).setValue(r.getString(C.item_desc));
		cols.get(C.category_name).setValue(r.getString(C.category_name));
		cols.get(C.color_name).setValue(r.getString(C.color_name));
		cols.get(C.size_name).setValue(r.getString(C.size_name));
		cols.get(C.price_sale).setValue(r.getIntCurrency(C.price_sale));
		cols.get(C.barcode_id).setValue(C.emptyString);
		cols.get(C.item_name).setValue(r.getString(C.item_name));
		cols.get(C.item_filename).setValue(Stringify.getString(item_filename));
		cols.get(C.item_disabled).setValue(r.getInt(C.item_disabled));
		cols.get(C.item_hide).setValue(r.getInt(C.item_hide));
		cols.get(C.item_id).setValue(Stringify.getString(item_id));
		cols.get(C.category_id).setValue(Stringify.getString(category_id));
		cols.get(C.color_id).setValue(Stringify.getString(color_id));
		cols.get(C.size_id).setValue(Stringify.getString(size_id));
		cols.get(C.is_composite).setValue(r.getInt(C.is_composite));
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
		sb.append(l.item_name())
		.append(C.char_31).append(l.item_desc())
		.append(C.char_31).append(l.category_name())
		.append(C.char_31).append(ItemCategory.getAll(fi));
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
