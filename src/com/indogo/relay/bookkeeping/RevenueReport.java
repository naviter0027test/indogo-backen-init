package com.indogo.relay.bookkeeping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.database.ResultSetWrapperStringify;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class RevenueReport implements IFunction {

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		
		if (action.equals(C.getCurrent)) {
			int closeHour = Helper.getInt(params, C.close_hour, true);
			Timestamp startDate = Helper.getTimestamp(params, C.start_date, true);
			Timestamp endDate = Helper.getTimestamp(params, C.end_date, true);
			return this.get(fi, closeHour, startDate, endDate);
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}
	
	public String get(FunctionItem fi, int closeHour, Timestamp startDate, Timestamp endDate) throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.set(Calendar.HOUR_OF_DAY, closeHour);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Timestamp newStartDate = new Timestamp(c.getTimeInMillis());
		
		c.setTime(endDate);
		c.add(Calendar.DATE, 1);
		c.set(Calendar.HOUR_OF_DAY, closeHour);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Timestamp newEndDate = new Timestamp(c.getTimeInMillis());
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT c.lm_time, c.ntd_sell, c.kurs_buy, c.idr_buy, d.ntd_buy, d.kurs_sell, d.idr_sell, ((d.idr_sell/d.ntd_buy)*c.ntd_sell) - c.idr_buy AS profit_idr, ((c.ntd_sell/c.idr_buy)*d.idr_sell) - d.ntd_buy AS profit_ntd\n")
		.append("FROM (\n")
		.append("SELECT lm_time, SUM(amount_ntd_used) AS ntd_sell, AVG(ntd_to_idr) AS kurs_buy, SUM(amount_idr) AS idr_buy\n")
		.append("FROM (\n")
		.append("SELECT DATE(lm_time - INTERVAL 8 HOUR) AS lm_time, amount_ntd_used, ntd_to_idr, amount_idr\n")
		.append("FROM usd_history\n")
		.append("WHERE lm_time >= ? AND lm_time < ? AND amount_ntd_used IS NOT NULL\n")
		.append(") a\n")
		.append("GROUP BY lm_time\n")
		.append(") c, (\n")
		.append("SELECT lm_time, SUM(amount_ntd) AS ntd_buy, AVG(kurs_value) AS kurs_sell, SUM(amount) AS idr_sell\n")
		.append("FROM (\n")
		.append("SELECT DATE(lm_time - INTERVAL 8 HOUR) AS lm_time, amount * -1 AS amount, kurs_value, amount_ntd\n")
		.append("FROM idr_history\n")
		.append("WHERE lm_time >= ? AND lm_time < ? AND txn_id IS NOT NULL) b\n")
		.append("GROUP BY lm_time\n")
		.append(") d\n")
		.append("WHERE c.lm_time = d.lm_time\n")
		.append("ORDER BY lm_time");
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement(sb.toString());
		try {
			pstmt.setTimestamp(1, newStartDate);
			pstmt.setTimestamp(2, newEndDate);
			pstmt.setTimestamp(3, newStartDate);
			pstmt.setTimestamp(4, newEndDate);
			
			sb = new StringBuilder();
			try (ResultSetWrapperStringify r = new ResultSetWrapperStringify(pstmt.executeQuery())) {
				while (r.next()) {
					sb.append(r.getDate(1)).append(C.char_31)
					.append(r.getIntCurrency(2)).append(C.char_31)
					.append(r.getDoubleCurrency(3)).append(C.char_31)
					.append(r.getLongCurrency(4)).append(C.char_31)
					.append(r.getIntCurrency(5)).append(C.char_31)
					.append(r.getDoubleCurrency(6)).append(C.char_31)
					.append(r.getLongCurrency(7)).append(C.char_31)
					.append(r.getLongCurrency(8)).append(C.char_31)
					.append(r.getIntCurrency(9)).append(C.char_30);
				}
			}
			if (sb.length() > 0)
				sb.delete(sb.length() - 1, sb.length());
			return sb.toString();
		} finally {
			pstmt.close();
		}
	}

}
