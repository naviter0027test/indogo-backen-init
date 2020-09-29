package com.indogo.relay.bookkeeping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class IdrBookkeeping implements IFunction, ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.idr_history;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDesc, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, C.lm_user));
		cols.add(new TablePageColumn(C.amount, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.amount));
		cols.add(new TablePageColumn(C.total, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.total));
		cols.add(new TablePageColumn(C.comment, C.columnTypeString, C.columnDirectionDefault, true, false, C.comment));
		cols.add(new TablePageColumn(C.txn_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.txn_id));
		cols.add(new TablePageColumn(C.kurs_value, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.kurs_value));
		cols.add(new TablePageColumn(C.amount_ntd, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.amount_ntd));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.amount).setValue(r.getLongCurrency(C.amount));
		cols.get(C.total).setValue(r.getLongCurrency(C.total));
		cols.get(C.comment).setValue(r.getString(C.comment));
		cols.get(C.txn_id).setValue(r.getLong(C.txn_id));
		cols.get(C.kurs_value).setValue(r.getDoubleCurrency(C.kurs_value));
		cols.get(C.amount_ntd).setValue(r.getIntCurrency(C.amount_ntd));
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
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		Connection conn = fi.getConnection().getConnection();
		
		if (action.equals(C.init)) {
			long current = getCurrent(fi, false);
			return String.format("%,d", current);
		} else if (action.equals(C.insert)) {
			int amount = Helper.getInt(params, C.amount, true);
			String comment = Helper.getString(params, C.comment, true);
			try {
				long current = add(fi, amount, comment);
				conn.commit();
				return String.valueOf(current);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else if (action.equals(C.getCurrent)) {
			long current = getCurrent(fi, false);
			return String.format("%,d", current);
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}
	
	public long getCurrent(FunctionItem fi, boolean isLock) throws Exception {
		String s = fi.getConnection().getGlobalConfig(C.idr_bookkeeping, C.total, isLock);
		if (Helper.isNullOrEmpty(s))
			return 0;
		else
			return Long.parseLong(s);
	}
	
	public long add(FunctionItem fi, long amount, String comment) throws Exception {
		return add(fi, amount, comment, null, null, null, null);
	}
	
	public long add(FunctionItem fi, long amount, String comment, Timestamp lmTime) throws Exception {
		return add(fi, amount, comment, lmTime, null, null, null);
	}
	
	public long add(FunctionItem fi, long amount, String comment, Long txnId, Double kursValue, Integer amountNtd) throws Exception {
		return add(fi, amount, comment, null, txnId, kursValue, amountNtd);
	}
	
	private long add(FunctionItem fi, long amount, String comment, Timestamp lmTime, Long txnId, Double kursValue, Integer amountNtd) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		long current = getCurrent(fi, true);
		current += amount;
		fi.getConnection().setGlobalConfig(C.idr_bookkeeping, C.total, String.valueOf(current), true);
		
		PreparedStatement pstmt = conn.prepareStatement("insert into idr_history (lm_time, lm_user, amount, total, comment, txn_id, kurs_value, amount_ntd) values (?,?,?,?,?,?,?,?)");
		try {
			if (lmTime == null)
				pstmt.setTimestamp(1, fi.getConnection().getCurrentTime());
			else
				pstmt.setTimestamp(1, lmTime);
			pstmt.setString(2, fi.getSessionInfo().getUserName());
			pstmt.setLong(3, amount);
			pstmt.setLong(4, current);
			pstmt.setString(5, comment);
			if (txnId == null) pstmt.setNull(6, Types.BIGINT); else pstmt.setLong(6, txnId);
			if (kursValue == null) pstmt.setNull(7, Types.DOUBLE); else pstmt.setDouble(7, kursValue);
			if (amountNtd == null) pstmt.setNull(8, Types.INTEGER); else pstmt.setInt(8, amountNtd);
			pstmt.executeUpdate();
		} finally {
			pstmt.close();
		}
		
		return current;
	}
	
}
