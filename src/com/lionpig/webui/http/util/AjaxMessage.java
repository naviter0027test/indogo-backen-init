package com.lionpig.webui.http.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.gson.Gson;
import com.lionpig.webui.http.struct.SessionInfo;

public class AjaxMessage {
	public int ajaxerrcode;
	public String ajaxerrmsg;
	public String ajaxerrstack;
	public String ajaxdata;
	public String ajaxinfo;
	
	public AjaxMessage() {}
	
	public static String parseOk(String data, SessionInfo info) {
		String s;
		if (info == null)
			s = "";
		else
			s = info.toString();
		return parse(0, "", "", data, s, false);
	}
	
	private static String parse(int errcode, String errmsg, String errstack, String data, String info, boolean isHtml) {
		if (isHtml) {
			return (errstack == null ? "" : "<span style=\"color:#ff0000\">" + TextToHTML(errstack) + "</span><input type=\"hidden\" id=\"ajax-message-error-code\" value=\"" + errcode + "\"/>");
		}
		else {
			AjaxMessage msg = new AjaxMessage();
			msg.ajaxerrcode = errcode;
			msg.ajaxerrmsg = errmsg == null ? "" : errmsg;
			msg.ajaxerrstack = errstack == null ? "" : errstack;
			msg.ajaxdata = data == null ? "" : data;
			msg.ajaxinfo = info == null ? "" : info;
			Gson gson = new Gson();
			String json = gson.toJson(msg);
			return json;
		}
	}
	
	public static String parseError(int errcode, String errmsg, String errstack, boolean isHtml) {
		return parse(errcode, errmsg, errstack, "", "", isHtml);
	}
	public static String parseError(int errcode, String errmsg, String errstack) {
		return parseError(errcode, errmsg, errstack, false);
	}
	public static String parseError(int code, Throwable E, boolean isHtml) {
		if (E == null)
			return null;
		
		String msg = E.getMessage();
		StringWriter o1 = null;
		PrintWriter o2 = null;
		try {
			o1 = new StringWriter();
			o2 = new PrintWriter(o1);
			E.printStackTrace(o2);
			return parseError(code, msg, o1.toString(), isHtml);
		}
		catch (Exception e3) {
			return parseError(code, msg, msg, isHtml);
		}
		finally {
			try {
				if (o2 != null)
					o2.close();
				if (o1 != null)
					o1.close();
			}
			catch (Exception e2) {}
		}
	}
	public static String parseError(int code, Throwable E) {
		return parseError(code, E, false);
	}
	public static String parseRelayError(Throwable E) {
		if (E == null)
			return null;
		
		String msg = E.getMessage();
		StringWriter o1 = null;
		PrintWriter o2 = null;
		try {
			o1 = new StringWriter();
			o2 = new PrintWriter(o1);
			E.printStackTrace(o2);
			return "<relayerror>" + msg + "</relayerror><relaystack>" + o1.toString() + "</relaystack>";
		}
		catch (Exception e3) {
			return "<relayerror>" + msg + "</relayerror><relaystack>" + msg + "</relaystack>";
		}
		finally {
			try {
				if (o2 != null)
					o2.close();
				if (o1 != null)
					o1.close();
			}
			catch (Exception e2) {}
		}
	}
	
	public static String TextToHTML(String text) {
		if(text == null) {
			return null;
		}
		return text.replaceAll("\r", "")
		.replaceAll("\"", "&quot;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll("&", "&amp;")
		.replaceAll("\n", "<br/>");
	}
}
