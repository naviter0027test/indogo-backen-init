package com.lionpig.webui.http.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.lionpig.webui.http.FunctionException;
import com.lionpig.webui.http.FunctionManager;
import com.lionpig.webui.http.util.AjaxMessage;

public class MultipartFormData extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2855369912417144606L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
		resp.setCharacterEncoding("UTF-8");
		PrintWriter pw = resp.getWriter();
		try {
			Hashtable<String, String> params = new Hashtable<String, String>();
			Hashtable<String, FileItem> files = new Hashtable<String, FileItem>();
			
			if (isMultipart) {
				DiskFileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				@SuppressWarnings("rawtypes")
				List items = upload.parseRequest(req);
				
				@SuppressWarnings("rawtypes")
				Iterator iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = (FileItem) iter.next();
					if (item.isFormField()) {
						params.put(item.getFieldName(), item.getString("UTF-8"));
					}
					else {
						files.put(item.getFieldName(), item);
					}
				}
			}
			else {
				@SuppressWarnings("rawtypes")
				Enumeration en = req.getParameterNames();
				while (en.hasMoreElements()) {
					String K = en.nextElement().toString();
					String V = req.getParameter(K);
					params.put(K, V);
				}
			}
			
			if (!params.containsKey("F"))
				throw new FunctionException(2, "Incorrect message format: Function must be defined");
			
			FunctionManager.execute(req, resp, getServletContext(), params, files);
		}
		catch (FunctionException E) {
			pw.write(AjaxMessage.parseError(E.getErrorCode(), E));
		}
		catch (Exception E) {
			pw.write(AjaxMessage.parseError(1, E));
		}
	}
}
