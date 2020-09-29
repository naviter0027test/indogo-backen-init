package com.lionpig.webui.http.struct;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MenuInfo {
	private int rowId;
	private String title;
	private String url;
	private String groupName;
	private String type;
	private String pageId;
	private int pathRowId;
	private List<MenuInfo> child;
	private Hashtable<String, MenuInfo> group;

	public int getRowId() {
		return rowId;
	}
	public String getTitle() {
		return title;
	}
	public String getUrl() {
		return url;
	}
	public String getGroupName() {
		return groupName;
	}
	public String getType() {
		return type;
	}
	public String getPageId() {
		return pageId;
	}
	public int getPathRowId() {
		return pathRowId;
	}
	
	public MenuInfo(int rowId, String title, String url, String groupName, String type, String pageId, int pathRowId) {
		this.rowId = rowId;
		this.title = title;
		this.url = url;
		this.groupName = groupName;
		this.type = type;
		this.pageId = pageId;
		this.pathRowId = pathRowId;
		this.child = new ArrayList<MenuInfo>();
		this.group = new Hashtable<String, MenuInfo>();
	}
	
	public void addChild(MenuInfo mi) {
		child.add(mi);
		if (mi.getGroupName() != null) {
			group.put(mi.getGroupName(), mi);
		}
	}
	
	public MenuInfo getChild(int i) {
		return child.get(i);
	}
	
	public MenuInfo getChild(String groupName) {
		return group.get(groupName);
	}
	
	public int sizeChild() {
		return child.size();
	}
}
