package com.indogo.relay.bookkeeping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indogo.model.bookkeeping.UsdHistoryModel;
import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.func.TablePage;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.tablepage.ITablePage;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageRowAttribute;
import com.lionpig.webui.http.tablepage.TablePageSort;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.DateFormat;
import com.lionpig.webui.http.util.Helper;
import com.lionpig.webui.http.util.Stringify;

public class UsdBookkeeping implements ITablePage, IFunction {

	@Override
	public String getTableOwner() {
		return null;
	}

	@Override
	public String getTableName() {
		return C.usd_history;
	}

	@Override
	public List<TablePageColumn> getColumns(FunctionItem fi) {
		List<TablePageColumn> cols = new ArrayList<>();
		cols.add(new TablePageColumn(C.lm_time, C.columnTypeDateTime, C.columnDirectionDesc, true, false, C.lm_time));
		cols.add(new TablePageColumn(C.seq_no, C.columnTypeNumber, C.columnDirectionDesc, true, false, C.seq_no));
		cols.add(new TablePageColumn(C.lm_user, C.columnTypeString, C.columnDirectionDefault, true, false, C.lm_user));
		cols.add(new TablePageColumn(C.amount_ntd, C.columnTypeNumber, C.columnDirectionDefault, true, false, "NTD"));
		cols.add(new TablePageColumn(C.usd_to_ntd, C.columnTypeNumber, C.columnDirectionDefault, true, false, "USD to NTD Kurs Rate"));
		cols.add(new TablePageColumn(C.amount_usd, C.columnTypeNumber, C.columnDirectionDefault, true, false, "USD"));
		cols.add(new TablePageColumn(C.usd_to_idr, C.columnTypeNumber, C.columnDirectionDefault, true, false, "USD to IDR Kurs Rate"));
		cols.add(new TablePageColumn(C.amount_idr, C.columnTypeNumber, C.columnDirectionDefault, true, false, "IDR"));
		cols.add(new TablePageColumn(C.ntd_to_idr, C.columnTypeNumber, C.columnDirectionDefault, true, false, "NTD to IDR Kurs Rate"));
		cols.add(new TablePageColumn(C.amount_ntd_used, C.columnTypeNumber, C.columnDirectionDefault, true, false, "NTD Used"));
		cols.add(new TablePageColumn(C.comment, C.columnTypeString, C.columnDirectionDefault, true, false, "Comment"));
		return cols;
	}

	@Override
	public void populateRowData(FunctionItem fi, Hashtable<String, TablePageColumn> cols, ResultSetWrapperStringify r,
			boolean isHtml, TablePageRowAttribute rowAttr) throws Exception {
		Timestamp lm_time = r.unwrap().getTimestamp(C.lm_time);
		if (lm_time.getTime() != max_lm_time.getTime()) {
			rowAttr.HtmlClass.add("disable-delete");
			rowAttr.HtmlClass.add("disable-update");
		}
		cols.get(C.lm_time).setValue(Stringify.getTimestamp(lm_time));
		cols.get(C.seq_no).setValue(r.getInt(C.seq_no));
		cols.get(C.lm_user).setValue(r.getString(C.lm_user));
		cols.get(C.amount_ntd).setValue(r.getIntCurrency(C.amount_ntd));
		cols.get(C.usd_to_ntd).setValue(r.getDoubleCurrency(C.usd_to_ntd));
		cols.get(C.amount_usd).setValue(r.getDoubleCurrency(C.amount_usd));
		cols.get(C.usd_to_idr).setValue(r.getDoubleCurrency(C.usd_to_idr));
		cols.get(C.amount_idr).setValue(r.getLongCurrency(C.amount_idr));
		cols.get(C.ntd_to_idr).setValue(r.getDoubleCurrency(C.ntd_to_idr));
		cols.get(C.amount_ntd_used).setValue(r.getIntCurrency(C.amount_ntd_used));
		cols.get(C.comment).setValue(r.getString(C.comment));
	}
	
	private Timestamp max_lm_time = null;

	@Override
	public boolean populateRowDataBefore(FunctionItem fi, List<TablePageColumn> column, List<TablePageSort> sort,
		List<TablePageFilter> filter) throws Exception {
		PreparedStatement pstmtLastLmTime = fi.getConnection().getConnection().prepareStatement("select max(lm_time) from usd_history");
		try {
			ResultSet r = pstmtLastLmTime.executeQuery();
			try {
				if (r.next()) {
					max_lm_time = r.getTimestamp(1);
				}
			} finally {
				r.close();
			}
		} finally {
			pstmtLastLmTime.close();
		}
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
		
		if (action.equals(C.insert)) {
			String action_2 = Helper.getString(params, C.action_2, true);
			String row;
			if (action_2.equals(C.usd)) {
				int amount_ntd = Helper.getInt(params, C.amount_ntd, true);
				double usd_to_ntd = Helper.getDouble(params, C.usd_to_ntd, true);
				double amount_usd = Helper.getDouble(params, C.amount_usd, true);
				String comment = Helper.getString(params, C.comment, false);
				
				try {
					Timestamp lm_time = insertUSD(fi, amount_ntd, usd_to_ntd, amount_usd, comment);
					
					List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
					filter.add(new TablePageFilter("lm_time", "DATETIME", "=", DateFormat.getInstance().format(lm_time), null));
					TablePage p = new TablePage();
					row = p.getRows(this, fi, 0, 1, this.getColumns(fi), null, filter, null, null, null);
					conn.commit();
				} catch (Exception e) {
					conn.rollback();
					throw e;
				}
			} else if (action_2.equals(C.idr)) {
				double amount_usd_used = Helper.getDouble(params, C.amount_usd_used, true);
				double usd_to_idr = Helper.getDouble(params, C.usd_to_idr, true);
				long amount_idr = Helper.getLong(params, C.amount_idr, true);
				String comment = Helper.getString(params, C.comment, false);
				
				try {
					Timestamp lm_time = insertIDR(fi, amount_usd_used, usd_to_idr, amount_idr, comment);
					
					List<TablePageFilter> filter = new ArrayList<TablePageFilter>();
					filter.add(new TablePageFilter("lm_time", "DATETIME", "=", DateFormat.getInstance().format(lm_time), null));
					TablePage p = new TablePage();
					row = p.getRows(this, fi, 0, 1, this.getColumns(fi), null, filter, null, null, null);
					conn.commit();
				} catch (Exception e) {
					conn.rollback();
					throw e;
				}
			} else
				throw new Exception(String.format(C.unknown_action, action_2));
			
			StringBuilder sb = new StringBuilder(50);
			sb.append(this.getTotalAsString(fi)).append(C.char_31)
			.append(row);
			return sb.toString();
		} else if (action.equals(C.total)) {
			return getTotalAsString(fi);
		} else if (action.equals(C.delete)) {
			Timestamp lm_time = Helper.getTimestamp(params, C.lm_time, true);
			try {
				this.delete(fi, lm_time);
				conn.commit();
				return "1";
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}
	
	private String getTotalAsString(FunctionItem fi) throws Exception {
		return String.format("%,.2f", getTotal(fi));
	}
	
	public double getTotal(FunctionItem fi) throws Exception {
		Connection conn = fi.getConnection().getConnection();
		Statement stmt = conn.createStatement();
		try {
			ResultSet r = stmt.executeQuery("select sum(amount_usd) from usd_account");
			try {
				r.next();
				return r.getDouble(1);
			} finally {
				r.close();
			}
		} finally {
			stmt.close();
		}
	}
	
	public Timestamp insertUSD(FunctionItem fi, int amount_ntd, double usd_to_ntd, double amount_usd, String comment) throws Exception {
		fi.getConnection().getGlobalConfig(C.usd_history, C.lock, true);
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("insert into usd_history (lm_time, seq_no, lm_user, amount_ntd, usd_to_ntd, amount_usd, comment) values (?,?,?,?,?,?,?)");
		PreparedStatement pstmtUsdAccount = conn.prepareStatement("insert into usd_account (lm_time, lm_user, amount_ntd, usd_to_ntd, amount_usd) values (?,?,?,?,?)");
		try {
			Timestamp lm_time = fi.getConnection().getCurrentTime();
			String lm_user = fi.getSessionInfo().getUserName();
			
			pstmt.setTimestamp(1, lm_time);
			pstmt.setInt(2, 1);
			pstmt.setString(3, lm_user);
			pstmt.setInt(4, amount_ntd);
			pstmt.setDouble(5, usd_to_ntd);
			pstmt.setDouble(6, amount_usd);
			pstmt.setString(7, comment);
			pstmt.executeUpdate();
			
			pstmtUsdAccount.setTimestamp(1, lm_time);
			pstmtUsdAccount.setString(2, lm_user);
			pstmtUsdAccount.setInt(3, amount_ntd);
			pstmtUsdAccount.setDouble(4, usd_to_ntd);
			pstmtUsdAccount.setDouble(5, amount_usd);
			pstmtUsdAccount.executeUpdate();
			
			return lm_time;
		} finally {
			pstmt.close();
			pstmtUsdAccount.close();
		}
	}
	
	public Timestamp insertIDR(FunctionItem fi, double amount_usd_used, double usd_to_idr, long amount_idr, String comment) throws Exception {
		if (amount_usd_used <= 0)
			throw new Exception("USD value must be larger than 0");
		if (usd_to_idr <= 0)
			throw new Exception("USD to IDR kurs rate must be large than 0");
		if (amount_idr <= 0)
			throw new Exception("IDR value must be larger than 0");
		
		fi.getConnection().getGlobalConfig(C.usd_history, C.lock, true);
		
		long original_idr = amount_idr;
		Connection conn = fi.getConnection().getConnection();
		Statement stmt = conn.createStatement();
		PreparedStatement pstmtGetAccountUSD = conn.prepareStatement("select amount_usd, amount_ntd, usd_to_ntd from usd_account where lm_time = ? for update");
		PreparedStatement pstmtInsert = conn.prepareStatement("insert into usd_history (lm_time, seq_no, lm_user, amount_usd, usd_to_idr, amount_idr, ntd_to_idr, amount_ntd_used, comment, lm_time_used) values (?,?,?,?,?,?,?,?,?,?)");
		PreparedStatement pstmtDeleteAccountUSD = conn.prepareStatement("delete from usd_account where lm_time = ?");
		PreparedStatement pstmtUpdateAccountUSD = conn.prepareStatement("update usd_account set amount_usd = ?, amount_ntd = ? where lm_time = ?");
		try {
			Timestamp currentTime = fi.getConnection().getCurrentTime();
			int currentSeqNo = 1;
			String currentUser = fi.getSessionInfo().getUserName();
			
			Timestamp minLmTime;
			ResultSet r = stmt.executeQuery("select min(lm_time) from usd_account");
			try {
				r.next();
				minLmTime = r.getTimestamp(1);
				if (minLmTime == null)
					throw new Exception("not enough USD, need to have additional " + String.format("%,.2f", amount_usd_used) + " dollars to exchange to IDR");
			} finally {
				r.close();
			}
			
			double accountUsd = 0;
			int accountNtd = 0;
			double accountUsdToNtd = 0;
			pstmtGetAccountUSD.setTimestamp(1, minLmTime);
			r = pstmtGetAccountUSD.executeQuery();
			try {
				if (r.next()) {
					accountUsd = r.getDouble(1);
					accountNtd = r.getInt(2);
					accountUsdToNtd = r.getDouble(3);
				} else
					throw new Exception(String.format(C.data_not_exist, Stringify.getTimestamp(minLmTime)));
			} finally {
				r.close();
			}
			
			while (accountUsd <= amount_usd_used && amount_usd_used > 0) {
				amount_usd_used = amount_usd_used - accountUsd;
				long amount_idr_gain = (long) (accountUsd * usd_to_idr);
				amount_idr = amount_idr - amount_idr_gain;
				double ntd_to_idr = (double)amount_idr_gain / (double)accountNtd;
				
				pstmtInsert.setTimestamp(1, currentTime);
				pstmtInsert.setInt(2, currentSeqNo);
				pstmtInsert.setString(3, currentUser);
				pstmtInsert.setDouble(4, -accountUsd);
				pstmtInsert.setDouble(5, usd_to_idr);
				pstmtInsert.setLong(6, amount_idr_gain);
				pstmtInsert.setDouble(7, ntd_to_idr);
				pstmtInsert.setInt(8, accountNtd);
				pstmtInsert.setString(9, comment);
				pstmtInsert.setTimestamp(10, minLmTime);
				pstmtInsert.executeUpdate();
				currentSeqNo++;
				
				pstmtDeleteAccountUSD.setTimestamp(1, minLmTime);
				pstmtDeleteAccountUSD.executeUpdate();
				
				if (amount_usd_used > 0) {
					r = stmt.executeQuery("select min(lm_time) from usd_account");
					try {
						r.next();
						minLmTime = r.getTimestamp(1);
						if (minLmTime == null)
							throw new Exception("not enough USD, need to have additional " + String.format("%,.2f", amount_usd_used) + " dollars to exchange to IDR");
					} finally {
						r.close();
					}
					
					pstmtGetAccountUSD.setTimestamp(1, minLmTime);
					r = pstmtGetAccountUSD.executeQuery();
					try {
						if (r.next()) {
							accountUsd = r.getDouble(1);
							accountNtd = r.getInt(2);
							accountUsdToNtd = r.getDouble(3);
						} else
							throw new Exception(String.format(C.data_not_exist, Stringify.getTimestamp(minLmTime)));
					} finally {
						r.close();
					}
				}
			}
			
			if (accountUsd > amount_usd_used && amount_usd_used > 0) {
				accountUsd = accountUsd - amount_usd_used;
				int amount_ntd_used = (int) (amount_usd_used * accountUsdToNtd);
				accountNtd = accountNtd - amount_ntd_used;
				double ntd_to_idr = (double)amount_idr / (double)amount_ntd_used;
				
				pstmtInsert.setTimestamp(1, currentTime);
				pstmtInsert.setInt(2, currentSeqNo);
				pstmtInsert.setString(3, currentUser);
				pstmtInsert.setDouble(4, -amount_usd_used);
				pstmtInsert.setDouble(5, usd_to_idr);
				pstmtInsert.setLong(6, amount_idr);
				pstmtInsert.setDouble(7, ntd_to_idr);
				pstmtInsert.setInt(8, amount_ntd_used);
				pstmtInsert.setString(9, comment);
				pstmtInsert.setTimestamp(10, minLmTime);
				pstmtInsert.executeUpdate();
				currentSeqNo++;
				
				pstmtUpdateAccountUSD.setDouble(1, accountUsd);
				pstmtUpdateAccountUSD.setInt(2, accountNtd);
				pstmtUpdateAccountUSD.setTimestamp(3, minLmTime);
				pstmtUpdateAccountUSD.executeUpdate();
			}
			
			String idrComment = "added from USD account";
			if (!Helper.isNullOrEmpty(comment)) {
				idrComment = idrComment + ", with additional comment:\n" + comment;
			}
			
			new IdrBookkeeping().add(fi, original_idr, idrComment, currentTime);
			
			return currentTime;
		} finally {
			pstmtUpdateAccountUSD.close();
			pstmtInsert.close();
			pstmtGetAccountUSD.close();
			stmt.close();
		}
	}
	
	public void delete(FunctionItem fi, Timestamp lm_time) throws Exception {
		fi.getConnection().getGlobalConfig(C.usd_history, C.lock, true);
		
		Connection conn = fi.getConnection().getConnection();
		Statement stmt = conn.createStatement();
		PreparedStatement pstmtLockUsdHistory = conn.prepareStatement("select seq_no, amount_ntd, amount_usd, amount_idr, amount_ntd_used, lm_time_used from usd_history where lm_time = ? order by seq_no for update");
		PreparedStatement pstmtLockUsdAccount = conn.prepareStatement("select amount_ntd, amount_usd from usd_account where lm_time = ? for update");
		PreparedStatement pstmtUpdateUsdAccount = conn.prepareStatement("update usd_account set amount_ntd = ?, amount_usd = ?, lm_user = ? where lm_time = ?");
		PreparedStatement pstmtInsertUsdAccount = conn.prepareStatement("insert into usd_account (lm_time, lm_user, amount_ntd, usd_to_ntd, amount_usd) values (?,?,?,?,?)");
		PreparedStatement pstmtGetUsdToNtd = conn.prepareStatement("select usd_to_ntd from usd_history where lm_time = ? and seq_no = 1");
		PreparedStatement pstmtDeleteUsdHistory = conn.prepareStatement("delete from usd_history where lm_time = ?");
		PreparedStatement pstmtDeleteUsdAccount = conn.prepareStatement("delete from usd_account where lm_time = ?");
		try {
			Timestamp old_lm_time;
			ResultSet r = stmt.executeQuery("select max(lm_time) from usd_history");
			try {
				r.next();
				old_lm_time = r.getTimestamp(1);
			} finally {
				r.close();
			}
			
			if (old_lm_time.getTime() != lm_time.getTime()) {
				throw new Exception("input lm_time incorrect");
			}
			
			List<UsdHistoryModel> list = new ArrayList<>();
			pstmtLockUsdHistory.setTimestamp(1, lm_time);
			r = pstmtLockUsdHistory.executeQuery();
			try {
				while (r.next()) {
					UsdHistoryModel m = new UsdHistoryModel();
					m.seq_no = r.getInt(1);
					int i = r.getInt(2);
					if (r.wasNull()) m.amount_ntd = null; else m.amount_ntd = i;
					double d = r.getDouble(3);
					if (r.wasNull()) m.amount_usd = null; else m.amount_usd = d;
					long l = r.getLong(4);
					if (r.wasNull()) m.amount_idr = null; else m.amount_idr = l;
					i = r.getInt(5);
					if (r.wasNull()) m.amount_ntd_used = null; else m.amount_ntd_used = i;
					m.lm_time_used = r.getTimestamp(6);
					list.add(m);
				}
			} finally {
				r.close();
			}
			
			long amount_idr_reclaim = 0;
			
			for (UsdHistoryModel m : list) {
				if (m.lm_time_used != null) {
					// idr case
					amount_idr_reclaim = amount_idr_reclaim + m.amount_idr;
					
					boolean isExist = false;
					int amount_ntd = 0;
					double amount_usd = 0;
					pstmtLockUsdAccount.setTimestamp(1, m.lm_time_used);
					r = pstmtLockUsdAccount.executeQuery();
					try {
						if (r.next()) {
							amount_ntd = r.getInt(1);
							amount_usd = r.getDouble(2);
							isExist = true;
						}
					} finally {
						r.close();
					}
					
					if (isExist) {
						amount_ntd = amount_ntd + m.amount_ntd_used;
						amount_usd = amount_usd - m.amount_usd;
						pstmtUpdateUsdAccount.setInt(1, amount_ntd);
						pstmtUpdateUsdAccount.setDouble(2, amount_usd);
						pstmtUpdateUsdAccount.setString(3, fi.getSessionInfo().getUserName());
						pstmtUpdateUsdAccount.setTimestamp(4, m.lm_time_used);
						pstmtUpdateUsdAccount.executeUpdate();
					} else {
						double usd_to_ntd;
						pstmtGetUsdToNtd.setTimestamp(1, m.lm_time_used);
						r = pstmtGetUsdToNtd.executeQuery();
						try {
							if (r.next()) {
								usd_to_ntd = r.getDouble(1);
							} else {
								usd_to_ntd = (double)m.amount_ntd_used / Math.abs(m.amount_usd);
							}
						} finally {
							r.close();
						}
						
						pstmtInsertUsdAccount.setTimestamp(1, m.lm_time_used);
						pstmtInsertUsdAccount.setString(2, fi.getSessionInfo().getUserName());
						pstmtInsertUsdAccount.setInt(3, m.amount_ntd_used);
						pstmtInsertUsdAccount.setDouble(4, usd_to_ntd);
						pstmtInsertUsdAccount.setDouble(5, Math.abs(m.amount_usd));
						pstmtInsertUsdAccount.executeUpdate();
					}
				}
			}
			
			if (amount_idr_reclaim > 0) {
				new IdrBookkeeping().add(fi, -amount_idr_reclaim, "usd bookkeeping deleted");
			}
			
			pstmtDeleteUsdAccount.setTimestamp(1, lm_time);
			pstmtDeleteUsdAccount.executeUpdate();
			
			pstmtDeleteUsdHistory.setTimestamp(1, lm_time);
			pstmtDeleteUsdHistory.executeUpdate();
		} finally {
			pstmtLockUsdHistory.close();
		}
	}
}
