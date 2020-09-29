package com.indogo.relay.onlineshopping;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.model.onlineshopping.VendorModel;
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
import com.lionpig.webui.http.util.Stringify;

public class Vendor extends AbstractFunction implements ITablePage {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.vendor;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		L l = fi.getLanguage();
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.vendor_name, C.columnTypeString, C.columnDirectionDefault, true, false, l.vendor_name()));
		cols.add(new TablePageColumn(C.contact_person, C.columnTypeString, C.columnDirectionDefault, true, false, l.contact_person()));
		cols.add(new TablePageColumn(C.phone_no, C.columnTypeString, C.columnDirectionDefault, true, false, l.phone_no()));
		cols.add(new TablePageColumn(C.fax_no, C.columnTypeString, C.columnDirectionDefault, true, false, l.fax_no()));
		cols.add(new TablePageColumn(C.address, C.columnTypeString, C.columnDirectionDefault, true, false, l.address()));
		cols.add(new TablePageColumn(C.email, C.columnTypeString, C.columnDirectionDefault, true, false, l.email()));
		cols.add(new TablePageColumn(C.vendor_desc, C.columnTypeString, C.columnDirectionDefault, true, false, l.vendor_desc()));
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDefault, true, false, l.lm_time()));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, l.lm_user()));
		cols.add(new TablePageColumn(C.vendor_id, C.columnTypeNumber, C.columnDirectionDefault, true, false, l.vendor_id(), true, true));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		cols.get(C.vendor_name).setValue(r.getString(C.vendor_name));
		cols.get(C.contact_person).setValue(r.getString(C.contact_person));
		cols.get(C.phone_no).setValue(r.getString(C.phone_no));
		cols.get(C.fax_no).setValue(r.getString(C.fax_no));
		cols.get(C.address).setValue(r.getString(C.address));
		cols.get(C.email).setValue(r.getString(C.email));
		cols.get(C.lm_time).setValue(r.getTimestamp(C.lm_time));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.vendor_id).setValue(r.getInt(C.vendor_id));
		cols.get(C.vendor_desc).setValue(r.getString(C.vendor_desc));
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
		return null;
	}

	@Override
	protected String onInit(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(l.vendor_name())
		.append(C.char_31).append(l.contact_person())
		.append(C.char_31).append(l.phone_no())
		.append(C.char_31).append(l.fax_no())
		.append(C.char_31).append(l.address())
		.append(C.char_31).append(l.email())
		.append(C.char_31).append(l.vendor_maintenance_title())
		.append(C.char_31).append(l.vendor_desc());
		return sb.toString();
	}

	@Override
	protected String onInsert(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		VendorModel vendorModel = new VendorModel();
		vendorModel.vendor_name = Helper.getString(params, C.vendor_name, true, l.vendor_name());
		vendorModel.contact_person = Helper.getString(params, C.contact_person, false, l.contact_person());
		vendorModel.phone_no = Helper.getString(params, C.phone_no, false, l.phone_no());
		vendorModel.fax_no = Helper.getString(params, C.fax_no, false, l.fax_no());
		vendorModel.address = Helper.getString(params, C.address, false, l.address());
		vendorModel.email = Helper.getString(params, C.email, false, l.email());
		vendorModel.vendor_desc = Helper.getString(params, C.vendor_desc, false, l.vendor_desc());
		
		Connection conn = fi.getConnection().getConnection();
		try {
			insert(fi, vendorModel);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.vendor_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(vendorModel.vendor_id), null));
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
		VendorModel vendorModel = new VendorModel();
		vendorModel.vendor_id = Helper.getInt(params, C.vendor_id, true, l.vendor_id());
		vendorModel.vendor_name = Helper.getString(params, C.vendor_name, true, l.vendor_name());
		vendorModel.contact_person = Helper.getString(params, C.contact_person, false, l.contact_person());
		vendorModel.phone_no = Helper.getString(params, C.phone_no, false, l.phone_no());
		vendorModel.fax_no = Helper.getString(params, C.fax_no, false, l.fax_no());
		vendorModel.address = Helper.getString(params, C.address, false, l.address());
		vendorModel.email = Helper.getString(params, C.email, false, l.email());
		vendorModel.lm_time = Helper.getTimestamp(params, C.lm_time, true, l.lm_time());
		vendorModel.vendor_desc = Helper.getString(params, C.vendor_desc, false, l.vendor_desc());
		
		Connection conn = fi.getConnection().getConnection();
		try {
			update(fi, vendorModel);
			
			List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
			filter.add(new TablePageFilter(C.vendor_id, C.columnTypeNumber, C.operationEqual, Stringify.getString(vendorModel.vendor_id), null));
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
		VendorModel vendorModel = new VendorModel();
		vendorModel.vendor_id = Helper.getInt(params, C.vendor_id, true, l.vendor_id());
		vendorModel.lm_time = Helper.getTimestamp(params, C.lm_time, true, l.lm_time());
		
		Connection conn = fi.getConnection().getConnection();
		try {
			delete(fi, vendorModel);
			
			conn.commit();
			
			return C.emptyString;
		} catch (Exception e) {
			conn.rollback();
			throw e;
		}
	}

	@Override
	protected String onGet(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi,
			Hashtable<String, String> params, String action, L l, S s) throws Exception {
		int vendor_id = Helper.getInt(params, C.vendor_id, true, l.vendor_id());
		VendorModel m = get(fi, vendor_id);
		StringBuilder sb = new StringBuilder();
		sb.append(Stringify.getString(m.vendor_name))
		.append(C.char_31).append(Stringify.getString(m.contact_person))
		.append(C.char_31).append(Stringify.getString(m.phone_no))
		.append(C.char_31).append(Stringify.getString(m.fax_no))
		.append(C.char_31).append(Stringify.getString(m.address))
		.append(C.char_31).append(Stringify.getString(m.email))
		.append(C.char_31).append(Stringify.getTimestamp(m.lm_time))
		.append(C.char_31).append(Stringify.getString(m.vendor_desc));
		return sb.toString();
	}
	
	public void insert(FunctionItem fi, VendorModel vendorModel) throws Exception {
		vendorModel.vendor_id = (int) fi.getConnection().getSeq(C.vendor_id, true);
		vendorModel.lm_time = fi.getConnection().getCurrentTime();
		vendorModel.lm_user = fi.getSessionInfo().getUserName();
		
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.vendor_insert()));
		try {
			pstmt.setInt(1, vendorModel.vendor_id);
			pstmt.setString(2, vendorModel.vendor_name);
			pstmt.setString(3, vendorModel.contact_person);
			pstmt.setString(4, vendorModel.phone_no);
			pstmt.setString(5, vendorModel.fax_no);
			pstmt.setString(6, vendorModel.address);
			pstmt.setString(7, vendorModel.email);
			pstmt.setTimestamp(8, vendorModel.lm_time);
			pstmt.setString(9, vendorModel.lm_user);
			pstmt.setString(10, vendorModel.vendor_desc);
			pstmt.executeUpdate();
		} finally {
			pstmt.close();
		}
	}
	
	public void update(FunctionItem fi, VendorModel vendorModel) throws Exception {
		L l = fi.getLanguage();
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.vendor_update()));
		PreparedStatementWrapper pstmtSelect = new PreparedStatementWrapper(conn.prepareStatement(sql.vendor_select_for_update()));
		try {
			Timestamp old_lm_time;
			pstmtSelect.setInt(1, vendorModel.vendor_id);
			ResultSet r = pstmtSelect.executeQuery();
			try {
				if (r.next()) {
					old_lm_time = r.getTimestamp(1);
				} else
					throw new Exception(l.data_not_exist(C.vendor_id, Stringify.getString(vendorModel.vendor_id)));
			} finally {
				r.close();
			}
			
			if (old_lm_time.getTime() != vendorModel.lm_time.getTime())
				throw new Exception(fi.getLanguage().data_already_updated_by_another_user(C.vendor_id, Stringify.getString(vendorModel.vendor_id)));
			
			vendorModel.lm_time = fi.getConnection().getCurrentTime();
			vendorModel.lm_user = fi.getSessionInfo().getUserName();
			
			pstmt.setString(1, vendorModel.vendor_name);
			pstmt.setString(2, vendorModel.contact_person);
			pstmt.setString(3, vendorModel.phone_no);
			pstmt.setString(4, vendorModel.fax_no);
			pstmt.setString(5, vendorModel.address);
			pstmt.setString(6, vendorModel.email);
			pstmt.setTimestamp(7, vendorModel.lm_time);
			pstmt.setString(8, vendorModel.lm_user);
			pstmt.setString(9, vendorModel.vendor_desc);
			pstmt.setInt(10, vendorModel.vendor_id);
			pstmt.executeUpdate();
		} finally {
			pstmt.close();
			pstmtSelect.close();
		}
	}
	
	public void delete(FunctionItem fi, VendorModel vendorModel) throws Exception {
		L l = fi.getLanguage();
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.vendor_delete()));
		PreparedStatementWrapper pstmtSelect = new PreparedStatementWrapper(conn.prepareStatement(sql.vendor_select_for_update()));
		try {
			Timestamp old_lm_time;
			pstmtSelect.setInt(1, vendorModel.vendor_id);
			ResultSet r = pstmtSelect.executeQuery();
			try {
				if (r.next()) {
					old_lm_time = r.getTimestamp(1);
				} else
					throw new Exception(l.data_not_exist(C.vendor_id, Stringify.getString(vendorModel.vendor_id)));
			} finally {
				r.close();
			}
			
			if (old_lm_time.getTime() != vendorModel.lm_time.getTime())
				throw new Exception(fi.getLanguage().data_already_updated_by_another_user(C.vendor_id, Stringify.getString(vendorModel.vendor_id)));
			
			pstmt.setInt(1, vendorModel.vendor_id);
			pstmt.executeUpdate();
		} finally {
			pstmt.close();
			pstmtSelect.close();
		}
	}
	
	public VendorModel get(FunctionItem fi, int vendor_id) throws Exception {
		L l = fi.getLanguage();
		S sql = fi.getSql();
		Connection conn = fi.getConnection().getConnection();
		PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(sql.vendor_get()));
		try {
			pstmt.setInt(1, vendor_id);
			ResultSetWrapper r = pstmt.executeQueryWrapper();
			try {
				if (r.next()) {
					VendorModel m = new VendorModel();
					m.vendor_id = vendor_id;
					m.vendor_name = r.getString(1);
					m.contact_person = r.getString(2);
					m.phone_no = r.getString(3);
					m.fax_no = r.getString(4);
					m.address = r.getString(5);
					m.email = r.getString(6);
					m.lm_time = r.getTimestamp(7);
					m.lm_user = r.getString(8);
					m.vendor_desc = r.getString(9);
					return m;
				} else
					throw new Exception(l.data_not_exist(C.vendor_id, Stringify.getString(vendor_id)));
			} finally {
				r.close();
			}
		} finally {
			pstmt.close();
		}
	}

}
