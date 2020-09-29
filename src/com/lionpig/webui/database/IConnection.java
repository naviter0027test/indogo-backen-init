package com.lionpig.webui.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.List;

import com.lionpig.webui.http.struct.AutoPageInfo;
import com.lionpig.webui.http.struct.LogInfo;
import com.lionpig.webui.http.struct.MenuInfo;
import com.lionpig.webui.http.struct.RelayInfo;
import com.lionpig.webui.http.struct.SessionInfo;
import com.lionpig.webui.http.struct.SpParamInfo;
import com.lionpig.webui.http.struct.SqlInputInfo;
import com.lionpig.webui.http.struct.SqlOutputInfo;
import com.lionpig.webui.http.struct.admin.PasswordConfirmation;
import com.lionpig.webui.http.tablepage.TablePageColumn;
import com.lionpig.webui.http.tablepage.TablePageFilter;
import com.lionpig.webui.http.tablepage.TablePageResult;
import com.lionpig.webui.http.tablepage.TablePageSort;

public interface IConnection {
	public static final String VERSION = "1.5";
	
	public Connection getConnection() throws Exception;
	public Connection getConnection(String dbName) throws Exception;
	public void close();
	public void logout(String SID) throws Exception;
	public boolean checkSessionId(String SID, boolean doUpdate, String serverIp, String serverName, int serverPort) throws Exception;
	public String createSession(String userName, String password) throws Exception;
	public boolean checkMenuAuthorize(String SID, int menuRowId) throws Exception;
	public SessionInfo getUserInfo(String SID) throws Exception;
	public List<MenuInfo> getMenu(String SID) throws Exception;
	public MenuInfo getMenu(String SID, int menuRowId) throws Exception;
	public RelayInfo getRelayInfo(String relayId) throws Exception;
	public String getParamValue(String SID, String paramName) throws Exception;
	public void removeParam(String SID, String paramName) throws Exception;
	public void setParamValue(String SID, String paramName, String paramValue) throws Exception;
	public void log(LogInfo info);
	public Hashtable<String, String> getGlobalConfig(String groupName) throws Exception;
	public String getGlobalConfig(String groupName, String configName) throws Exception;
	public String getGlobalConfig(String groupName, String configName, boolean isLock) throws Exception;
	public void setGlobalConfig(String groupName, String configName, String configValue) throws Exception;
	public void setGlobalConfig(String groupName, String configName, String configValue, boolean isJoinTransaction) throws Exception;
	public int getLogVerbose();
	public int getSessionCount() throws Exception;
	public void lock() throws Exception;
	public long getSeq(String name, boolean isJoinTransaction) throws Exception;
	public Timestamp getCurrentTime() throws Exception;
	
	public TablePageResult tablePageBegin(String tableOwner, String tableName, int page, int offset, List<TablePageColumn> column, List<TablePageSort> sort, List<TablePageFilter> filter, String sessionId, String pageId, String joinSql) throws Exception;
	public void tablePageEnd();
	public boolean isAllowedToExportTablePage(int user_row_id, String class_name) throws Exception;
	
	// auto page functions:
	public AutoPageInfo autoPageGetDetail(String pageId) throws Exception;
	public SqlInputInfo autoPageGetSqlInput(String pageId, String inputName) throws Exception;
	public List<SqlInputInfo> autoPageGetSqlInputs(String pageId) throws Exception;
	public List<SqlOutputInfo> autoPageGetSqlOutputs(String pageId) throws Exception;
	public int autoPageSetSqlTempTable(String pageId, String SID, ResultSet source, List<SqlOutputInfo> cols) throws Exception;
	public void autoPageClearSqlTempTable(String pageId, String SID) throws Exception;
	public List<SpParamInfo> autoPageGetSpParams(String pageId) throws Exception;
	
	// administrator functions:
	// 1. user
	public PasswordConfirmation adminUserCreate(String userName, String emailAddress, int[] roleIds) throws Exception;
	public void adminUserDelete(String userName) throws Exception;
	public void adminUserChangePassword(String userName, String oldPassword, String newPassword) throws Exception;
	public List<RoleNameListModel> adminUserGetRoles(String userName) throws Exception;
	public void adminUserUpdate(int userRowId, String userName, boolean disabled, String emailAddress, int[] roleIds) throws Exception;
	public PasswordConfirmation adminUserResetPassword(String userName) throws Exception;
	public void adminUserConfirmAccount(String userName, String confirmId, String newPassword) throws Exception;
	
	// 2. role
	public List<RoleNameListModel> adminRoleGetAll() throws Exception;
	
	// 3. history
	public void logHistory(HistoryTable table, Long rowId, HistoryAction historyAction, String actionDesc, Timestamp actionTime, String userName, HistoryData... datas) throws Exception;
}
