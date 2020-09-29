package com.lionpig.webui.http.struct;

import java.io.File;
import java.util.List;

import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.database.RoleNameListModel;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.License;

public class SessionInfo {
	private int userRowId;
	private String userName;
	private String lastActiveTime;
	private int maxIdleSecond;
	private String serverName;
	private List<RoleNameListModel> roles;
	private String aliasId;
	private String warningMessage = C.emptyString;
	
	public int getUserRowId() { return userRowId; }
	public String getUserName() { return userName; }
	public String getLastActiveTime() { return lastActiveTime; }
	public int getMaxIdleSecond() { return maxIdleSecond; }
	public String getTempFolderPath() {
		return "temp" + File.separator + userName;
	}
	
	public void setServerName(String s) {
		this.serverName = s;
	}
	public String getServerName() {
		return this.serverName;
	}
	
	public void setWarningMessage(String warningMessage) {
		this.warningMessage = warningMessage;
	}
	public String getWarningMessage() {
		return warningMessage;
	}
	
	public SessionInfo(int userRowId, String userName, String lastActiveTime, int maxIdleSecond, List<RoleNameListModel> roles, String aliasId) {
		this.userRowId = userRowId;
		this.userName = userName;
		this.lastActiveTime = lastActiveTime;
		this.maxIdleSecond = maxIdleSecond;
		this.roles = roles;
		this.aliasId = aliasId;
	}
	
	@Override
	public String toString() {
		License l = License.getInstance();
		StringBuilder sb = new StringBuilder();
		sb.append("<div id=\"U\">").append(userName).append("</div>");
		sb.append("<div id=\"L\">").append(lastActiveTime).append("</div>");
		sb.append("<div id=\"UserRowId\">").append(userRowId).append("</div>");
		sb.append("<div id=\"Version\">").append(IConnection.VERSION).append("</div>");
		sb.append("<div id=\"MaxIdleTime\">").append(maxIdleSecond).append("</div>");
		sb.append("<div id=\"ServerName\">").append(serverName).append("</div>");
		sb.append("<div id=\"EnvironmentName\">").append(l.getLicenseType()).append("</div>");
		sb.append("<div id=\"RoleName\">");
		if (roles.size() > 0) {
			sb.append(roles.get(0).ROLE_NAME);
			for (int i = 1; i < roles.size(); i++) {
				sb.append(",").append(roles.get(i).ROLE_NAME);				
			}
		}
		sb.append("</div>");
		sb.append("<div id=\"AliasId\">").append(aliasId).append("</div>");
		sb.append("<div id=\"WarningMessage\">").append(warningMessage).append("</div>");
		return sb.toString();
	}
}
