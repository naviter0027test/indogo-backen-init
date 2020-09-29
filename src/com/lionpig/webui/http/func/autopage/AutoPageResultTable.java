package com.lionpig.webui.http.func.autopage;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;

import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.struct.SqlOutputInfo;
import com.lionpig.webui.http.util.DateFormat;

public class AutoPageResultTable implements IAutoPageResult {
	private static final String LOCK_ID = "AutoPageResultTableLock";
	private FunctionItem fi;
	private String pageId;
	private List<SqlOutputInfo> listSqlOutputInfo;
	
	public AutoPageResultTable(FunctionItem fi, String pageId, List<SqlOutputInfo> listSqlOutputInfo) {
		this.fi = fi;
		this.pageId = pageId;
		this.listSqlOutputInfo = listSqlOutputInfo;
	}
	
	public String execute(ResultSet r) throws Exception {
		fi.getConnection().autoPageClearSqlTempTable(pageId, fi.getSID());
		int rowCount = fi.getConnection().autoPageSetSqlTempTable(pageId, fi.getSID(), r, listSqlOutputInfo);
		fi.getConnection().setParamValue(fi.getSID(), "row-count", rowCount + "");
		return rowCount + "";
	}
	
	public void lock() throws Exception {
		String lock = fi.getConnection().getParamValue(fi.getSID(), LOCK_ID);
		if (lock != null)
			throw new Exception("There is an unfinished transaction at [" + lock + "], please wait for it to finished, or you can logout and use a new Session Id");
		
		lock = DateFormat.getInstance().format(Calendar.getInstance().getTime());
		fi.getConnection().setParamValue(fi.getSID(), LOCK_ID, lock);
	}
	
	public void unlock() {
		try {
			fi.getConnection().removeParam(fi.getSID(), LOCK_ID);
		}
		catch (Exception ignore) {}
	}
}
