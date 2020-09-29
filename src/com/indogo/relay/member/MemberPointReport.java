package com.indogo.relay.member;

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

public class MemberPointReport implements IFunction {

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, C.action, true);
		Connection conn = fi.getConnection().getConnection();
		
		if (action.equals(C.init)) {
			StringBuilder sb = new StringBuilder();
			
			try (Statement stmt = conn.createStatement()) {
				try (ResultSet r = stmt.executeQuery("select sum(remit_point) from member")) {
					r.next();
					sb.append(r.getLong(1));
				}
				try (ResultSet r = stmt.executeQuery("select remit_point, count(*) from member where is_wait_confirm in (3, 4) group by remit_point order by remit_point desc")) {
					int size = 0;
					StringBuilder sub = new StringBuilder();
					while (r.next()) {
						sub.append(C.char_31).append(r.getInt(1)).append(C.char_30).append(r.getInt(2));
						size++;
					}
					sb.append(C.char_31).append(size).append(sub);
				}
				try (ResultSet r = stmt.executeQuery("select count(*), sum(remit_point) from member_point where reason_id = 4")) {
					r.next();
					long count = r.getLong(1);
					long sum = r.getLong(2) * -1;
					sb.append(C.char_31).append(count).append(C.char_31).append(sum);
				}
			}
			
			return sb.toString();
		} else
			throw new Exception(String.format(C.unknown_action, action));
	}

}
