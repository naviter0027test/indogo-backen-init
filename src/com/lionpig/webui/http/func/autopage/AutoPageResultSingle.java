package com.lionpig.webui.http.func.autopage;

import java.sql.ResultSet;

public class AutoPageResultSingle implements IAutoPageResult {

	public String execute(ResultSet r) throws Exception {
		if (r.next()) {
			String s = r.getString(1);
			if (s == null)
				return "";
			else
				return s;
		}
		else {
			return null;
		}
	}

}
