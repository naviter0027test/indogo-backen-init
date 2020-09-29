package com.indogo.relay.report;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import com.indogo.model.report.EmployeePerformanceModel;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class EmployeePerformance implements IFunction {

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		if (action.equals(C.employee_performance)) {
			int interval_day = Helper.getInt(params, C.interval_day, true);
			List<EmployeePerformanceModel> rows = getEmployeePerformanceOverall(fi, interval_day);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			StringBuilder sb = new StringBuilder();
			for (EmployeePerformanceModel row : rows) {
				sb.append(dateFormat.format(row.action_time))
				.append(C.char_31).append(row.action_user)
				.append(C.char_31).append(row.score)
				.append(C.char_31).append(row.color_id)
				.append(C.char_30);
			}
			if (sb.length() > 0)
				sb.delete(sb.length() - 1, sb.length());
			return sb.toString();
		}
		return null;
	}
	
	public List<EmployeePerformanceModel> getEmployeePerformanceOverall(FunctionItem fi, int interval_day) throws Exception {
		String encoding = null;
		String sql = FileUtils.readFileToString(new File(fi.getTempFolder().getParentFile(), "employee_performance.sql"), encoding);
		String score_create_invoice = fi.getConnection().getGlobalConfig(C.employee_performance, C.score_create_invoice);
		String score_create_member = fi.getConnection().getGlobalConfig(C.employee_performance, C.score_create_member);
		String score_verify_old_member = fi.getConnection().getGlobalConfig(C.employee_performance, C.score_verify_old_member);
		String score_verify_new_member = fi.getConnection().getGlobalConfig(C.employee_performance, C.score_verify_new_member);
		String score_create_signature = fi.getConnection().getGlobalConfig(C.employee_performance, C.score_create_signature);
		
		String sqlAfter = String.format(sql, interval_day, score_create_invoice, score_create_member, score_create_signature, score_verify_new_member, score_verify_old_member);
		
		Connection conn = fi.getConnection().getConnection();
		try (Statement stmt = conn.createStatement()) {
			try (ResultSet r = stmt.executeQuery(sqlAfter)) {
				List<EmployeePerformanceModel> list = new ArrayList<>();
				while (r.next()) {
					EmployeePerformanceModel m = new EmployeePerformanceModel();
					m.action_time = r.getTimestamp(1);
					m.action_user = r.getString(2);
					m.score = r.getLong(3);
					m.color_id = r.getString(4);
					list.add(m);
				}
				return list;
			}
		}
	}

}
