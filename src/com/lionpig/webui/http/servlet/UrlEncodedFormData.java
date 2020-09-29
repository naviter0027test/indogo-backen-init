package com.lionpig.webui.http.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.http.FunctionManager;
import com.lionpig.webui.http.util.AjaxMessage;

public class UrlEncodedFormData extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -351772978303313214L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter pw = resp.getWriter();
		
		try {
			Hashtable<String, String> params = new Hashtable<String, String>();
			@SuppressWarnings("rawtypes")
			Enumeration en = req.getParameterNames();
			while (en.hasMoreElements()) {
				String K = en.nextElement().toString();
				String V = req.getParameter(K);
				params.put(K, V);
			}
			FunctionManager.execute(req, resp, getServletContext(), params, null);
		}
		catch (Exception E) {
			pw.write(AjaxMessage.parseError(1, E));
		}
	}
}
