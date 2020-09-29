package com.lionpig.webui.http.tablepage;

import java.util.Hashtable;
import java.util.List;

import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.struct.FunctionItem;

public interface ITablePage {
	String getTableOwner();
	String getTableName();
	List<TablePageColumn> getColumns(FunctionItem fi);
	void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r, boolean isHtml, TablePageRowAttribute rowAttr) throws Exception;
	boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort, List<TablePageFilter> filter) throws Exception;
	void populateRowDataAfter(FunctionItem fi) throws Exception;
}
