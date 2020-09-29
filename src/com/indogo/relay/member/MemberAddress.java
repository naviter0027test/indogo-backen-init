package com.indogo.relay.member;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.AddressType;
import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.PreparedStatementWrapper;
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

public class MemberAddress extends AbstractFunction implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.member_address;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.type_name, C.columnTypeString, C.columnDirectionNone, false, true, "Type"));
		cols.add(new TablePageColumn(C.address_value, C.columnTypeString, C.columnDirectionDefault, true, false, "Address"));
		cols.add(new TablePageColumn(C.member_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.member_id, true, true));
		cols.add(new TablePageColumn(C.address_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.address_id, true, true));
		cols.add(new TablePageColumn(C.type_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, C.type_id, true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		AddressType addressType = AddressType.get(r.unwrap().getInt(C.type_id));
		
		cols.get(C.type_name).setValue(addressType.getName());
		cols.get(C.address_value).setValue(r.getString(C.address_value));
		cols.get(C.member_id).setValue(r.getLong(C.member_id));
		cols.get(C.address_id).setValue(r.getInt(C.address_id));
		cols.get(C.type_id).setValue(String.valueOf(addressType.getId()));
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
		return MemberConfiguration.getAddressInHtmlOptionFormat(fi);
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		long member_id = Helper.getLong(params, C.member_id, true);
		AddressType address_type = AddressType.get(Helper.getInt(params, C.type_id, true));
		String address_value = Helper.getString(params, C.address_value, true);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			int address_id = insert(fi, member_id, address_type, address_value);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.member_id, C.columnTypeNumber, C.operationEqual, String.valueOf(member_id), null));
			filter.add(new TablePageFilter(C.address_id, C.columnTypeNumber, C.operationEqual, String.valueOf(address_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
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
		long member_id = Helper.getLong(params, C.member_id, true);
		AddressType address_type = AddressType.get(Helper.getInt(params, C.type_id, true));
		String address_value = Helper.getString(params, C.address_value, true);
		int address_id = Helper.getInt(params, C.address_id, true);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			update(fi, member_id, address_id, address_type, address_value);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.member_id, C.columnTypeNumber, C.operationEqual, String.valueOf(member_id), null));
			filter.add(new TablePageFilter(C.address_id, C.columnTypeNumber, C.operationEqual, String.valueOf(address_id), null));
			TablePage p = new TablePage();
			String row = p.getRows(this, fi, 1, 1, this.getColumns(fi), null, filter, null, null, null);
			
			conn.commit();
			
			return row;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}

	@Override
	protected String onDelete(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		long member_id = Helper.getLong(params, C.member_id, true);
		int address_id = Helper.getInt(params, C.address_id, true);
		
		Connection conn = fi.getConnection().getConnection();
		try {
			delete(fi, member_id, address_id);
			
			conn.commit();
			
			return "1";
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
	
	public int insert(FunctionItem fi, long member_id, AddressType address_type, String address_value) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		int address_id = (int) fi.getConnection().getSeq(C.address + "_" + member_id, true);
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("insert into member_address (member_id, address_id, type_id, address_value) values (?,?,?,?)"))) {
			pstmt.setLong(1, member_id);
			pstmt.setInt(2, address_id);
			pstmt.setInt(3, address_type.getId());
			pstmt.setString(4, address_value);
			pstmt.executeUpdate();
		}
		return address_id;
	}
	
	public void update(FunctionItem fi, long member_id, int address_id, AddressType address_type, String address_value) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("update member_address set type_id = ?, address_value = ? where member_id = ? and address_id = ?"))) {
			pstmt.setInt(1, address_type.getId());
			pstmt.setString(2, address_value);
			pstmt.setLong(3, member_id);
			pstmt.setInt(4, address_id);
			pstmt.executeUpdate();
		}
	}
	
	public void delete(FunctionItem fi, long member_id, int address_id) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement("delete from member_address where member_id = ? and address_id = ?"))) {
			pstmt.setLong(1, member_id);
			pstmt.setInt(2, address_id);
			pstmt.executeUpdate();
		}
	}

}
