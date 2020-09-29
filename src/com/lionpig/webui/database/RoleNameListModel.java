package com.lionpig.webui.database;

public class RoleNameListModel {
	public int ROLE_ID;
	public String ROLE_NAME;
	public String ROLE_DESC;
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RoleNameListModel) {
			RoleNameListModel target = (RoleNameListModel)obj;
			return this.ROLE_ID == target.ROLE_ID;
		}
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return new Integer(ROLE_ID).hashCode();
	}
}
