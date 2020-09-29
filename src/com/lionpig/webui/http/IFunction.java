package com.lionpig.webui.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.http.struct.FunctionItem;

public interface IFunction {
	public String execute(
		HttpServletRequest req,
		HttpServletResponse resp,
		FunctionItem fi
	) throws Exception;
}
