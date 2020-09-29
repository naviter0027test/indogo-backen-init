package com.lionpig.webui.http.func;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.struct.MenuInfo;

public class Menu implements IFunction {

	public String execute(HttpServletRequest req, HttpServletResponse resp, FunctionItem fi) throws Exception {
		String SID = fi.getSID();
		IConnection conn = fi.getConnection();
		Hashtable<String, String> params = fi.getRequestParameters();
		
		if (params.containsKey("MenuRowId")) {
			int menuRowId = Integer.parseInt(params.get("MenuRowId"));
			MenuInfo mi = conn.getMenu(SID, menuRowId);
			if (mi == null)
				throw new Exception("MenuRowId [" + menuRowId + "] not exist");
			return parseHyperlink(SID, mi);
		}
		else {
			List<MenuInfo> list = conn.getMenu(SID);
//			StringBuilder sb = new StringBuilder();
//			for (int i = 0; i < list.size(); i++) {
//				sb.append("<div value=\"").append(list.get(i).getGroupName()).append("\">")
//					.append(createMenu(list.get(i), SID))
//					.append("</div>");
//			}
//			return sb.toString();
			
			StringBuilder sb = new StringBuilder();
			this.createToolbar(sb, list, SID);
			return sb.toString();
		}
	}
	
	private void createToolbar(StringBuilder summary, List<MenuInfo> listMenuInfo, String SID) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"menu-toolbar\">\n<tr>\n");
		int iMax = listMenuInfo.size();
		for (int i = 0; i < iMax; i++) {
			MenuInfo mi = listMenuInfo.get(i);
			if (mi.getGroupName() == null) {
				sb.append("<td nowrap>").append(parseHyperlink(SID, mi)).append("</td>\n");
			}
			else {
				sb.append("<td nowrap name=\"").append(this.createMenuItem(summary, mi, SID)).append("\">").append(mi.getGroupName()).append("</td>\n");
			}
		}
		sb.append("</tr>\n</table>\n");
		summary.append(sb.toString());
	}
	
	private int counter = 0;
	private String createMenuItem(StringBuilder summary, MenuInfo parent, String SID) throws Exception {
		counter++;
		String menuItemId = "menu-item-" + counter;
		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"menu-item\" id=\"").append(menuItemId).append("\">\n");
		int iMax = parent.sizeChild();
		for (int i = 0; i < iMax; i++) {
			MenuInfo mi = parent.getChild(i);
			if (mi.getGroupName() == null) {
				sb.append("<tr><td nowrap>").append(parseHyperlink(SID, mi)).append("</td></tr>\n");
			}
			else {
				sb.append("<tr><td nowrap name=\"").append(createMenuItem(summary, mi, SID)).append("\">").append(mi.getGroupName()).append("</td></tr>\n");
			}
		}
		sb.append("</table>\n");
		summary.append(sb.toString());
		return menuItemId;
	}
	
	@SuppressWarnings("unused")
	private String createMenu(MenuInfo mi, String SID) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		int iMax = mi.sizeChild();
		for (int i = 0; i < iMax; i++) {
			MenuInfo child = mi.getChild(i);
			if (child.getGroupName() == null) {
				sb.append("<li>").append(parseHyperlink(SID, child)).append("</li>");
			}
			else {
				sb.append("<li><a href=\"#GROUP\">").append(child.getGroupName()).append("</a>").append(createMenu(child, SID)).append("</li>");
			}
		}
		sb.append("</ul>");
		return sb.toString();
	}
	
	private String parseHyperlink(String SID, MenuInfo mi) throws Exception {
		String menuType = mi.getType();
		if (menuType.equals("html")) {
			return "<a href=\"" + mi.getUrl() + "?SID=" + SID + "&RowId=" + mi.getRowId() + "\">" + mi.getTitle() + "</a>";
		}
		else if (menuType.equals("auto")) {
			if (mi.getPageId() == null)
				throw new Exception("Incorrect format, MenuRowId [" + mi.getRowId() + "] is an auto page but doesn't define PageId");
			return "<a href=\"servlet/AutoPage?id=" + mi.getPageId() + "&SID=" + SID + "&RowId=" + mi.getRowId() + "\">" + mi.getTitle() + "</a>";
		}
		else {
			throw new Exception("Unknown menu type [" + menuType + "] for MenuRowId [" + mi.getRowId() + "]");
		}
	}
}
