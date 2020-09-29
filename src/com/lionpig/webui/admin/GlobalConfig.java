package com.lionpig.webui.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class GlobalConfig implements IFunction {

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		Connection conn = fi.getConnection().getConnection();
		
		if (action.equals(C.init)) {
			Statement stmt = conn.createStatement();
			try {
				ResultSet r = stmt.executeQuery("select group_name, config_name, config_value from global_config where group_name not in ('ADMIN', 'ADMIN_EMAIL', 'seq', 'SERVER') and (group_name, config_name) not in (('idr_bookkeeping', 'total'), ('member', 'kurs_value'), ('member', 'kurs_lm_time')) order by group_name, config_name");
				try {
					StringBuilder sb = new StringBuilder();
					while (r.next()) {
						sb.append(r.getString(1))
						.append(C.char_31).append(r.getString(2))
						.append(C.char_31).append(r.getString(3))
						.append(C.char_30);
					}
					if (sb.length() > 0)
						sb.delete(sb.length() - 1, sb.length());
					return sb.toString();
				} finally {
					r.close();
				}
			} finally {
				stmt.close();
			}
		} else if (action.equals(C.update)) {
			String group_name = Helper.getString(params, C.group_name, true);
			String config_name = Helper.getString(params, C.config_name, true);
			String config_value = Helper.getString(params, C.config_value, true);
			fi.getConnection().setGlobalConfig(group_name, config_name, config_value);
			return fi.getConnection().getGlobalConfig(group_name, config_name);
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}

}
