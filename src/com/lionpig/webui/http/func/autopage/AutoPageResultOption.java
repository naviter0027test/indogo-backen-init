package com.lionpig.webui.http.func.autopage;

import java.sql.ResultSet;

public class AutoPageResultOption implements IAutoPageResult {

	public String execute(ResultSet r) throws Exception {
		StringBuilder sb = new StringBuilder();
		String s;
		while (r.next()) {
			s = r.getString(1);
			if (s != null)
				sb.append("<option>").append(s).append("</option>");
		}
		return sb.toString();
	}

}
