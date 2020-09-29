package com.indogo.relay.onlineshopping;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.lionpig.language.L;
import com.lionpig.webui.database.PreparedStatementWrapper;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;

public class SalesItem implements ITablePage {
	
	private PreparedStatementWrapper pstmtItem = null;

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.sales_item;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.item_image, C.columnTypeString, C.columnDirectionNone, false, true, C.emptyString, true));
		cols.add(new TablePageColumn(C.item_name, C.columnTypeString, C.columnDirectionDefault, false, true, l.item_name()));
		cols.add(new TablePageColumn(C.item_desc, C.columnTypeString, C.columnDirectionDefault, false, true, l.item_desc()));
		cols.add(new TablePageColumn(C.category_name, C.columnTypeString, C.columnDirectionNone, false, true, l.category_name()));
		cols.add(new TablePageColumn(C.color_name, C.columnTypeString, C.columnDirectionNone, false, true, l.color_name()));
		cols.add(new TablePageColumn(C.size_name, C.columnTypeString, C.columnDirectionNone, false, true, l.size_name()));
		cols.add(new TablePageColumn(C.sales_qty, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Qty"));
		cols.add(new TablePageColumn(C.sales_price, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Price"));
		cols.add(new TablePageColumn(C.sales_discount, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Discount"));
		cols.add(new TablePageColumn(C.sales_total, C.columnTypeNumber, C.columnDirectionDefault, true, false, "Total"));
		cols.add(new TablePageColumn(C.comment, C.columnTypeString, C.columnDirectionDefault, true, false, "Comment"));
		cols.add(new TablePageColumn(C.item_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.item_id, true, true));
		cols.add(new TablePageColumn(C.sales_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.sales_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		int item_id = r.unwrap().unwrap().getInt(C.item_id);
		
		pstmtItem.setInt(1, item_id);
		try (ResultSetWrapperStringify rr = new ResultSetWrapperStringify(pstmtItem.executeQuery())) {
			if (rr.next()) {
				String item_image = Item.resolveItemImageUrl(fi, item_id, rr.getString(C.item_filename));
				
				cols.get(C.item_image).setValue(item_image);
				cols.get(C.item_name).setValue(rr.getString(C.item_name));
				cols.get(C.item_desc).setValue(rr.getString(C.item_desc));
				cols.get(C.category_name).setValue(rr.getString(C.category_name));
				cols.get(C.color_name).setValue(rr.getString(C.color_name));
				cols.get(C.size_name).setValue(rr.getString(C.size_name));
			} else {
				cols.get(C.item_image).setValue(C.emptyString);
				cols.get(C.item_name).setValue(C.emptyString);
				cols.get(C.item_desc).setValue(C.emptyString);
				cols.get(C.category_name).setValue(C.emptyString);
				cols.get(C.color_name).setValue(C.emptyString);
				cols.get(C.size_name).setValue(C.emptyString);
			}
		}
		
		cols.get(C.sales_qty).setValue(r.getIntCurrency(C.sales_qty));
		cols.get(C.sales_price).setValue(r.getIntCurrency(C.sales_price));
		cols.get(C.sales_discount).setValue(r.getIntCurrency(C.sales_discount));
		cols.get(C.sales_total).setValue(r.getIntCurrency(C.sales_total));
		cols.get(C.item_id).setValue(String.valueOf(item_id));
		cols.get(C.sales_id).setValue(r.getLong(C.sales_id));
		cols.get(C.comment).setValue(r.getString(C.comment));
	}

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
			List<TablePageFilter> filter) throws Exception {
		pstmtItem = new PreparedStatementWrapper(fi.getConnection().getConnection().prepareStatement("select a.item_filename, a.item_name, a.item_desc, (select category_name from item_category where category_id = a.category_id) as category_name, (select color_name from item_color where color_id = a.color_id) as color_name, (select size_name from item_size where size_id = a.size_id) as size_name from item a where a.item_id = ?"));
		return false;
	}

	@Override
	public void populateRowDataAfter(FunctionItem fi) throws Exception {
		pstmtItem.close();
	}

}
