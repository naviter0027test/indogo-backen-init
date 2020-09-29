package com.lionpig.webui.database;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;

import com.lionpig.webui.http.FunctionException;
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
import com.lionpig.webui.http.util.DateFormat;

public class MySqlConnection implements IConnection {
	private Connection conn;
	private int verbose = 9;
	private PreparedStatement pstmtLog;
	private PreparedStatement pstmtTablePage = null;
	private PreparedStatement pstmtTablePageTotalRecord = null;
	private String SSO_URL = null;
	private int MAX_IDLE_SECONDS = 3600;
	private int MAX_PASSWORD_RETRY_COUNT = 0;
	
	private void init(String ip, String port, String instance, String acc, String pass) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + instance + "?useUnicode=true&characterEncoding=UTF8", acc, pass); 
		conn.setAutoCommit(false);
		
		pstmtLog = conn.prepareStatement("INSERT INTO debug_log (" +
				"log_time," +
				"log_verbose," +
				"log_value," +
				"user_row_id," +
				"user_name," +
				"session_id," +
				"relay_id," +
				"relay_url," +
				"function_name," +
				"class_name" +
				") VALUES (SYSDATE(),?,?,?,?,?,?,?,?,?)");
		
		try {
			MAX_IDLE_SECONDS = Integer.parseInt(this.getGlobalConfig("ADMIN", "MAX_IDLE_SECOND"));
		}
		catch (Exception ignore) {}
		
		try {
			SSO_URL = this.getGlobalConfig("ADMIN", "SSO_URL");
		}
		catch (Exception ignore) {}
		
		try {
			verbose = Integer.parseInt(this.getGlobalConfig("ADMIN", "LOG_VERBOSE"));
		}
		catch (Exception ignore) {}
		
		try {
			MAX_PASSWORD_RETRY_COUNT = Integer.parseInt(this.getGlobalConfig("ADMIN", "PASSWORD_RETRY_COUNT"));
		}
		catch (Exception ignore) {}
	}
	
	public MySqlConnection(ServletContext sc) throws Exception {
		init(
			sc.getInitParameter("DB_IP"),
			sc.getInitParameter("DB_PORT"),
			sc.getInitParameter("DB_INSTANCE"),
			sc.getInitParameter("DB_ACCOUNT"),
			sc.getInitParameter("DB_PASSWORD")
		);
	}
	
	public MySqlConnection(String ip, String port, String instance, String acc, String pass) throws Exception {
		init(ip, port, instance, acc, pass);
	}

	@Override
	public Connection getConnection() throws Exception {
		return conn;
	}

	@Override
	public Connection getConnection(String dbName) throws Exception {
		PreparedStatement pstmt = conn.prepareStatement("SELECT db_ip, db_port, db_instance, db_acc, db_pass, db_type FROM db_config WHERE db_name = ?");
		try {
			pstmt.setString(1, dbName);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					String ip = r.getString("db_ip");
					int port = r.getInt("db_port");
					String sid = r.getString("db_instance");
					String acc = r.getString("db_acc");
					String pass = r.getString("db_pass");
					String type = r.getString("db_type");
					
					if (type.equals("ORACLE")) {
						Class.forName("oracle.jdbc.driver.OracleDriver");
						return DriverManager.getConnection("jdbc:oracle:thin:@" + ip + ":" + port + ":" + sid, acc, pass);
					}
					else if (type.equals("SQLSERVER")) {
						Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
						return DriverManager.getConnection("jdbc:sqlserver://" + ip + ":" + port + ";databaseName=" + sid, acc, pass);
					}
					else if (type.equals("MYSQL")) {
						Class.forName("com.mysql.jdbc.Driver");
						return DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + sid, acc, pass);
					}
					else {
						throw new Exception("Unknown DB_TYPE [" + type + "]");
					}
				}
				else {
					throw new Exception("Cannot found configuration for [" + dbName + "]");
				}
			}
			finally {
				r.close();
			}
		}
		finally {
			try {
				pstmt.close();
			}
			catch (Exception ignore) {}
		}
	}

	@Override
	public void close() {
		try {
			if (!conn.isClosed())
				conn.close();
		}
		catch (Exception E) {}
	}

	@Override
	public void logout(String SID) throws Exception {
		PreparedStatement pstmt = null;
		PreparedStatement pstmtSession = null;
		PreparedStatement pstmtSessionDelete = null;
		try {
			pstmt = conn.prepareStatement("SELECT user_name FROM user_list WHERE session_id = ? FOR UPDATE");
			pstmt.setString(1, SID);
			ResultSet r = pstmt.executeQuery();
			String userName = null;
			if (r.next()) {
				userName = r.getString("user_name");
			}
			r.close();
			if (userName != null) {
				pstmtSession = conn.prepareStatement("SELECT last_active_time FROM session_list WHERE session_id = ? FOR UPDATE");
				pstmtSession.setString(1, SID);
				r = pstmtSession.executeQuery();
				boolean isExist = false;
				if (r.next())
					isExist = true;
				r.close();
				
				if (isExist) {
					pstmtSessionDelete = conn.prepareStatement("DELETE FROM session_list WHERE session_id = ?");
					pstmtSessionDelete.setString(1, SID);
					pstmtSessionDelete.executeUpdate();
				}
			}
			conn.commit();
		}
		catch (Exception E) {
			conn.rollback();
			throw E;
		}
		finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (pstmtSession != null) {
					pstmtSession.close();
				}
				if (pstmtSessionDelete != null) {
					pstmtSessionDelete.close();
				}
			}
			catch (Exception E) {}
		}
	}

	@Override
	public boolean checkSessionId(String SID, boolean doUpdate,
			String serverIp, String serverName, int serverPort)
			throws Exception {
		PreparedStatement pstmtUpdateSession = null;
		PreparedStatement pstmtSession = null;
		try {
			boolean isExist;
			
			pstmtSession = conn.prepareStatement("SELECT last_active_time FROM session_list WHERE session_id = ? FOR UPDATE");
			pstmtSession.setString(1, SID);
			ResultSet r = pstmtSession.executeQuery();
			if (r.next())
				isExist = true;
			else
				isExist = false;
			r.close();
			
			if (isExist && doUpdate) {
				pstmtUpdateSession = conn.prepareStatement("UPDATE session_list SET last_active_time = SYSDATE(), server_ip = ?, server_name = ?, server_port = ? WHERE session_id = ?");
				pstmtUpdateSession.setString(1, serverIp);
				pstmtUpdateSession.setString(2, serverName);
				pstmtUpdateSession.setInt(3, serverPort);
				pstmtUpdateSession.setString(4, SID);
				pstmtUpdateSession.executeUpdate();
			}

			conn.commit();
			return isExist;
		}
		catch (Exception E) {
			conn.rollback();
			throw E;
		}
		finally {
			if (pstmtUpdateSession != null) {
				try {
					pstmtUpdateSession.close();
				}
				catch (Exception ignore) {}
			}
			if (pstmtSession != null) {
				try {
					pstmtSession.close();
				}
				catch (Exception ignore) {}
			}
		}
	}

	@Override
	public String createSession(String userName, String password)
			throws Exception {
		boolean checkPasswordRetryCount = false;
		int currentRetryCount = 0;
		PreparedStatement pstmtSelectUser = null;
		PreparedStatement pstmtUpdateUser = null;
		PreparedStatement pstmtSelectLastActiveTime = null;
		PreparedStatement pstmtUpdateSession = null;
		PreparedStatement pstmtInsertSession = null;
		try {
			pstmtSelectUser = conn.prepareStatement("SELECT password, disabled, session_id, retry_count FROM user_list WHERE user_name = ? FOR UPDATE");
			pstmtSelectUser.setString(1, userName);
			ResultSet r = pstmtSelectUser.executeQuery();
			String sid;
			try {
				if (r.next()) {
					if (r.getString("disabled").equals("N")) {
						if (r.getString("password") == null) {
							if (SSO_URL != null) {
								throw new FunctionException(6, SSO_URL);
							}
							else {
								throw new FunctionException(5, "UserName [" + userName + "] is restricted to use Single Sign On");
							}
						}
						else if (r.getString("password").startsWith("confirm")) {
							throw new Exception("This account is waiting for confirmation");
						}
						else if (password.equals(r.getString("password"))) {
							sid = r.getString("session_id");
						}
						else {
							if (MAX_PASSWORD_RETRY_COUNT > 0) {
								checkPasswordRetryCount = true;
								currentRetryCount = r.getInt("retry_count");
							}
							throw new Exception("Password incorrect");
						}
					}
					else {
						throw new Exception("UserName [" + userName + "] disabled");
					}
				}
				else {
					throw new Exception("UserName [" + userName + "] doesn't exist");
				}
			}
			finally {
				r.close();
			}
			
			if (sid != null) {
				pstmtSelectLastActiveTime = conn.prepareStatement("SELECT last_active_time FROM session_list WHERE session_id = ? FOR UPDATE");
				pstmtSelectLastActiveTime.setString(1, sid);
				
				pstmtUpdateSession = conn.prepareStatement("UPDATE session_list SET last_active_time = SYSDATE() WHERE session_id = ?");
				pstmtUpdateSession.setString(1, sid);
				
				r = pstmtSelectLastActiveTime.executeQuery();
				try {
					if (!r.next()) {
						sid = null;
					}
				} finally {
					r.close();
				}
				
				if (sid != null) {
					pstmtUpdateSession.setString(1, sid);
					int count = pstmtUpdateSession.executeUpdate();
					if (count == 0)
						throw new Exception("Update session last active time return nothing");
				}
			}
			
			if (sid == null) {
				pstmtInsertSession = conn.prepareStatement("INSERT INTO session_list (session_id, last_active_time) VALUES (?, SYSDATE())");
				pstmtUpdateUser = conn.prepareStatement("UPDATE user_list SET session_id = ?, retry_count = 0 WHERE user_name = ?");
				
				sid = UUID.randomUUID().toString().replaceAll("-", "");
				
				pstmtInsertSession.setString(1, sid);
				pstmtInsertSession.executeUpdate();
				
				pstmtUpdateUser.setString(1, sid);
				pstmtUpdateUser.setString(2, userName);
				pstmtUpdateUser.executeUpdate();
			}
			else {
				pstmtUpdateUser = conn.prepareStatement("UPDATE user_list SET retry_count = 0 WHERE user_name = ?");
				pstmtUpdateUser.setString(1, userName);
				pstmtUpdateUser.executeUpdate();
			}
			
			conn.commit();
			return sid;
		}
		catch (Exception E) {
			if (checkPasswordRetryCount) {
				PreparedStatement pstmtUpdateRetryCount = null;
				try {
					currentRetryCount++;
					if (currentRetryCount > MAX_PASSWORD_RETRY_COUNT) {
						pstmtUpdateRetryCount = conn.prepareStatement("UPDATE user_list SET disabled = 'Y' WHERE user_name = ?");
						pstmtUpdateRetryCount.setString(1, userName);
						pstmtUpdateRetryCount.executeUpdate();
					}
					else {
						pstmtUpdateRetryCount = conn.prepareStatement("UPDATE user_list SET retry_count = ? WHERE user_name = ?");
						pstmtUpdateRetryCount.setInt(1, currentRetryCount);
						pstmtUpdateRetryCount.setString(2, userName);
						pstmtUpdateRetryCount.executeUpdate();
					}
					conn.commit();
				}
				catch (Exception ignore) {
					conn.rollback();
				}
				finally {
					if (pstmtUpdateRetryCount != null)
						pstmtUpdateRetryCount.close();
				}
			}
			else {
				conn.rollback();
			}
			throw E;
		}
		finally {
			try {
				if (pstmtSelectUser != null)
					pstmtSelectUser.close();
				if (pstmtInsertSession != null)
					pstmtInsertSession.close();
				if (pstmtSelectLastActiveTime != null)
					pstmtSelectLastActiveTime.close();
				if (pstmtUpdateSession != null)
					pstmtUpdateSession.close();
				if (pstmtUpdateUser != null)
					pstmtUpdateUser.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public boolean checkMenuAuthorize(String SID, int menuRowId)
			throws Exception {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("SELECT COUNT(*) " +
				"FROM user_list a, " +
					"role_menu b, " +
				    "menu c, " +
				    "menu_path d, " +
				    "user_role e " +
				"WHERE a.session_id = ? " +
					"AND a.user_row_id = e.user_row_id " +
					"AND b.role_id = e.role_id " +
					"AND c.menu_row_id = b.menu_row_id " +
					"AND d.path_row_id = c.path_row_id " +
					"AND c.menu_row_id = ?");
			pstmt.setString(1, SID);
			pstmt.setInt(2, menuRowId);
			ResultSet r = pstmt.executeQuery();
			int count = 0;
			if (r.next()) {
				count = r.getInt(1);
			}
			r.close();
			
			if (count <= 0)
				return false;
			else
				return true;
		}
		finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				}
				catch (Exception E) {}
			}
		}
	}

	@Override
	public SessionInfo getUserInfo(String SID) throws Exception {
		PreparedStatement pstmt = conn.prepareStatement("SELECT a.user_row_id, a.user_name, b.last_active_time, a.alias_id FROM user_list a, session_list b WHERE a.session_id = ? AND a.session_id = b.session_id");
		try {
			pstmt.setString(1, SID);

			int user_row_id;
			String user_name;
			Timestamp last_active_time;
			String alias_id;
			
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					user_row_id = r.getInt("user_row_id");
					user_name = r.getString("user_name");
					last_active_time = r.getTimestamp("last_active_time");
					alias_id = r.getString("alias_id");
				}
				else {
					return null;
				}
			}
			finally {
				r.close();
			}
			
			return new SessionInfo(
				user_row_id,
				user_name,
				DateFormat.getInstance().format(last_active_time),
				MAX_IDLE_SECONDS,
				this.adminUserGetRoles(user_name),
				alias_id
			);
		}
		finally {
			if (pstmt != null)
				pstmt.close();
		}
	}

	@Override
	public List<MenuInfo> getMenu(String SID) throws Exception {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("SELECT DISTINCT c.menu_row_id, c.title, c.url, d.path_name, c.menu_type_id, c.page_id, c.path_row_id, d.display_seq AS seq1, c.display_seq AS seq2 " +
				"FROM user_list a, " +
					"role_menu b, " +
				    "menu c, " +
				    "menu_path d, " +
				    "user_role e " +
				"WHERE a.session_id = ? " +
					"AND a.user_row_id = e.user_row_id " +
					"AND b.role_id = e.role_id " +
					"AND c.menu_row_id = b.menu_row_id " +
					"AND d.path_row_id = c.path_row_id " +
				"ORDER BY seq1, seq2");
			pstmt.setString(1, SID);
			
			Hashtable<String, MenuInfo> ht = new Hashtable<String, MenuInfo>();
			List<MenuInfo> list = new ArrayList<MenuInfo>();
			ResultSet r = pstmt.executeQuery();
			try {
				while (r.next()) {
					String[] tokens = r.getString("path_name").split(">");
					if (tokens.length > 0) {
						MenuInfo parent = ht.get(tokens[0]);
						if (parent == null) {
							parent = new MenuInfo(0, null, null, tokens[0], null, null, -1);
							ht.put(tokens[0], parent);
							list.add(parent);
						}
						for (int i = 1; i < tokens.length; i++) {
							MenuInfo mi = parent.getChild(tokens[i]);
							if (mi == null) {
								mi = new MenuInfo(0, null, null, tokens[i], null, null, -1);
								parent.addChild(mi);
							}
							parent = mi;
						}
						if (parent != null) {
							parent.addChild(new MenuInfo(r.getInt("menu_row_id"), r.getString("title"), r.getString("url"), null, r.getString("menu_type_id"), r.getString("page_id"), r.getInt("path_row_id")));
						}
					}
				}
			}
			finally {
				r.close();
			}
			return list;
		}
		finally {
			try {
				if (pstmt != null)
					pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public MenuInfo getMenu(String SID, int menuRowId) throws Exception {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("SELECT c.menu_row_id, c.title, c.url, d.path_name, c.menu_type_id, c.page_id, c.path_row_id " +
				"FROM user_list a, " +
					"role_menu b, " +
				    "menu c, " +
				    "menu_path d, " +
				    "user_role e " +
				"WHERE a.session_id = ? " +
					"AND c.menu_row_id = ? " +
					"AND a.user_row_id = e.user_row_id " +
					"AND b.role_id = e.role_id " +
					"AND c.menu_row_id = b.menu_row_id " +
					"AND d.path_row_id = c.path_row_id");
			pstmt.setString(1, SID);
			pstmt.setInt(2, menuRowId);
			
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					return new MenuInfo(r.getInt("menu_row_id"), r.getString("title"), r.getString("url"), null, r.getString("menu_type_id"), r.getString("page_id"), r.getInt("path_row_id"));
				}
				else {
					return null;
				}
			}
			finally {
				r.close();
			}
		}
		finally {
			try {
				if (pstmt != null)
					pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public RelayInfo getRelayInfo(String relayId) throws Exception {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("SELECT url, login_name, login_pass, relay_type, class_name FROM relay_config WHERE relay_id = ?");
			pstmt.setString(1, relayId);
			
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					return new RelayInfo(
						relayId,
						r.getString("url"),
						r.getString("login_name"),
						r.getString("login_pass"),
						r.getString("relay_type"),
						r.getString("class_name")
					);
				}
				else {
					return null;
				}
			}
			finally {
				r.close();
			}
		}
		finally {
			try {
				if (pstmt != null)
					pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public String getParamValue(String SID, String paramName) throws Exception {
		if (SID == null)
			return null;
		if (paramName == null)
			return null;
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("SELECT param_value FROM session_param WHERE session_id = ? AND param_name = ?");
			pstmt.setString(1, SID);
			pstmt.setString(2, paramName);
			
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next())
					return r.getString("param_value");
				else
					return null;
			}
			finally {
				r.close();
			}
		}
		finally {
			try {
				if (pstmt != null)
					pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public void removeParam(String SID, String paramName) throws Exception {
		if (SID == null)
			return;
		if (paramName == null)
			return;
		
		PreparedStatement pstmtLock = null;
		PreparedStatement pstmtDelete = null;
		try {
			boolean isExist;
			
			pstmtLock = conn.prepareStatement("SELECT param_value FROM session_param WHERE session_id = ? AND param_name = ? FOR UPDATE");
			pstmtLock.setString(1, SID);
			pstmtLock.setString(2, paramName);
			ResultSet r = pstmtLock.executeQuery();
			try {
				isExist = r.next();
			}
			finally {
				r.close();
			}
			
			if (isExist) {
				pstmtDelete = conn.prepareStatement("DELETE FROM session_param WHERE session_id = ? AND param_name = ?");
				pstmtDelete.setString(1, SID);
				pstmtDelete.setString(2, paramName);
				int count = pstmtDelete.executeUpdate();
				if (count == 0)
					throw new Exception("setParamValue delete failed");
			}
			
			conn.commit();
		}
		catch (Exception E) {
			conn.rollback();
			throw E;
		}
		finally {
			if (pstmtLock != null) {
				try {
					pstmtLock.close();
				}
				catch (Exception E) {}
			}
			if (pstmtDelete != null) {
				try {
					pstmtDelete.close();
				}
				catch (Exception E) {}
			}
		}
	}

	@Override
	public void setParamValue(String SID, String paramName, String paramValue)
			throws Exception {
		if (SID == null)
			return;
		if (paramName == null)
			return;
		if (paramValue == null)
			return;
		
		PreparedStatement pstmtLock = null;
		PreparedStatement pstmtUpdate = null;
		PreparedStatement pstmtInsert = null;
		try {
			boolean isExist;
			
			pstmtLock = conn.prepareStatement("SELECT param_value FROM session_param WHERE session_id = ? AND param_name = ? FOR UPDATE");
			pstmtLock.setString(1, SID);
			pstmtLock.setString(2, paramName);
			ResultSet r = pstmtLock.executeQuery();
			try {
				isExist = r.next();
			}
			finally {
				r.close();
			}
			
			if (isExist) {
				pstmtUpdate = conn.prepareStatement("UPDATE session_param SET param_value = ? WHERE session_id = ? AND param_name = ?");
				pstmtUpdate.setString(1, paramValue);
				pstmtUpdate.setString(2, SID);
				pstmtUpdate.setString(3, paramName);
				int count = pstmtUpdate.executeUpdate();
				if (count == 0)
					throw new Exception("setParamValue update failed");
			}
			else {
				pstmtInsert = conn.prepareStatement("INSERT INTO session_param (session_id, param_name, param_value) VALUES (?, ?, ?)");
				pstmtInsert.setString(1, SID);
				pstmtInsert.setString(2, paramName);
				pstmtInsert.setString(3, paramValue);
				int count = pstmtInsert.executeUpdate();
				if (count == 0)
					throw new Exception("setParamValue insert failed");
			}
			
			conn.commit();
		}
		catch (Exception E) {
			conn.rollback();
			throw E;
		}
		finally {
			if (pstmtLock != null) {
				try {
					pstmtLock.close();
				}
				catch (Exception E) {}
			}
			if (pstmtUpdate != null) {
				try {
					pstmtUpdate.close();
				}
				catch (Exception E) {}
			}
			if (pstmtInsert != null) {
				try {
					pstmtInsert.close();
				}
				catch (Exception E) {}
			}
		}
	}

	@Override
	public void log(LogInfo info) {
		if (verbose < info.getVerbose())
			return;
		try {
			pstmtLog.setInt(1, info.getVerbose());
			pstmtLog.setString(2, info.getMessage());
			pstmtLog.setInt(3, info.getUserRowId());
			pstmtLog.setString(4, info.getUserName());
			pstmtLog.setString(5, info.getSessionId());
			pstmtLog.setString(6, info.getRelayId());
			pstmtLog.setString(7, info.getRelayUrl());
			pstmtLog.setString(8, info.getFunctionName());
			pstmtLog.setString(9, info.getClassName());
			pstmtLog.executeUpdate();
			conn.commit();
		}
		catch (Exception E) {}
	}

	@Override
	public Hashtable<String, String> getGlobalConfig(String groupName)
			throws Exception {
		if (groupName == null)
			return null;
		
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("SELECT config_name, config_value FROM global_config WHERE group_name = ?");
			pstmt.setString(1, groupName);
			
			ResultSet r = pstmt.executeQuery();
			try {
				Hashtable<String, String> ht = new Hashtable<String, String>();
				String v;
				while (r.next()) {
					v = r.getString("config_value");
					if (v != null && v.length() > 0)
						ht.put(r.getString("config_name"), v);
				}
				return ht;
			}
			finally {
				r.close();
			}
		}
		finally {
			try {
				if (pstmt != null)
					pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public String getGlobalConfig(String groupName, String configName)
			throws Exception {
		return getGlobalConfig(groupName, configName, false);
	}
	
	@Override
	public String getGlobalConfig(String groupName, String configName, boolean isLock)
			throws Exception {
		if (groupName == null || configName == null)
			return null;
		
		PreparedStatement pstmt = null;
		try {
			if (isLock)
				pstmt = conn.prepareStatement("SELECT config_value FROM global_config WHERE group_name = ? AND config_name = ? for update");
			else
				pstmt = conn.prepareStatement("SELECT config_value FROM global_config WHERE group_name = ? AND config_name = ?");
			pstmt.setString(1, groupName);
			pstmt.setString(2, configName);
			
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					String v = r.getString("config_value");
					if (v != null && v.length() > 0)
						return v;
					else
						return null;
				}
				else {
					return null;
				}
			}
			finally {
				r.close();
			}
		}
		finally {
			try {
				if (pstmt != null)
					pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public void setGlobalConfig(String groupName, String configName,
			String configValue) throws Exception {
		setGlobalConfig(groupName, configName, configValue, false);
	}

	@Override
	public void setGlobalConfig(String groupName, String configName,
			String configValue, boolean isJoinTransaction) throws Exception {
		PreparedStatement pstmtUpd = conn.prepareStatement("UPDATE global_config SET config_value = ? WHERE group_name = ? AND config_name = ?");
		PreparedStatement pstmtIns = null;
		try {
			pstmtUpd.setString(1, configValue);
			pstmtUpd.setString(2, groupName);
			pstmtUpd.setString(3, configName);
			int count = pstmtUpd.executeUpdate();
			
			if (count == 0) {
				pstmtIns = conn.prepareStatement("INSERT INTO global_config (group_name, config_name, config_value) VALUES ( ?, ?, ? )");
				pstmtIns.setString(1, groupName);
				pstmtIns.setString(2, configName);
				pstmtIns.setString(3, configValue);
				pstmtIns.executeUpdate();
			}
			
			if (!isJoinTransaction)
				conn.commit();
		}
		catch (Exception e) {
			if (!isJoinTransaction)
				conn.rollback();
			throw e;
		}
		finally {
			pstmtUpd.close();
			if (pstmtIns != null)
				pstmtIns.close();
		}
	}

	@Override
	public int getLogVerbose() {
		return verbose;
	}

	@Override
	public int getSessionCount() throws Exception {
		Statement stmt = conn.createStatement();
		try {
			ResultSet r = stmt.executeQuery("SELECT COUNT(*) FROM session_list");
			try {
				r.next();
				return r.getInt(1);
			}
			finally {
				r.close();
			}
		}
		finally {
			stmt.close();
		}
	}

	@Override
	public void lock() throws Exception {
		PreparedStatement pstmtLock = conn.prepareStatement("SELECT config_value FROM global_config WHERE group_name = ? AND config_name = ? FOR UPDATE");
		try {
			pstmtLock.setString(1, "SERVER");
			pstmtLock.setString(2, "LOCK");
			pstmtLock.executeQuery().close();
		}
		finally {
			pstmtLock.close();
		}
	}

	@Override
	public long getSeq(String name, boolean isJoinTransaction) throws Exception {
		PreparedStatement pstmtLock = conn.prepareStatement("SELECT config_value FROM global_config WHERE group_name = ? AND config_name = ? FOR UPDATE");
		PreparedStatement pstmtUpd = conn.prepareStatement("UPDATE global_config SET config_value = ? WHERE group_name = ? AND config_name = ?");
		PreparedStatement pstmtIns = conn.prepareStatement("INSERT INTO global_config (group_name, config_name, config_value) VALUES (?,?,?)");
		try {
			long value;
			boolean isInsert;
			pstmtLock.setString(1, "seq");
			pstmtLock.setString(2, name);
			ResultSet r = pstmtLock.executeQuery();
			try {
				if (r.next()) {
					value = Long.parseLong(r.getString("config_value"));
					isInsert = false;
				}
				else {
					value = 1;
					isInsert = true;
				}
			}
			finally {
				r.close();
			}
			
			if (isInsert) {
				pstmtIns.setString(1, "seq");
				pstmtIns.setString(2, name);
				pstmtIns.setString(3, String.valueOf(value + 1));
				pstmtIns.executeUpdate();
			}
			else {
				pstmtUpd.setString(1, String.valueOf(value + 1));
				pstmtUpd.setString(2, "seq");
				pstmtUpd.setString(3, name);
				pstmtUpd.executeUpdate();
			}
			
			if (!isJoinTransaction)
				conn.commit();
			
			return value;
		}
		catch (Exception e) {
			if (!isJoinTransaction)
				conn.rollback();
			throw e;
		}
		finally {
			pstmtLock.close();
			pstmtUpd.close();
		}
	}

	@Override
	public Timestamp getCurrentTime() throws Exception {
		Statement stmt = conn.createStatement();
		try {
			ResultSet r = stmt.executeQuery("SELECT sysdate()");
			try {
				r.next();
				DateFormat df = DateFormat.getInstance();
				return new Timestamp(df.parse(df.format(r.getTimestamp(1))).getTime());
			}
			finally {
				r.close();
			}
		}
		finally {
			stmt.close();
		}
	}

	@Override
	public TablePageResult tablePageBegin(String tableOwner, String tableName,
			int page, int offset, List<TablePageColumn> column,
			List<TablePageSort> sort, List<TablePageFilter> filter,
			String sessionId, String pageId, String joinSql)
			throws Exception {
		if (pstmtTablePage == null) {
			boolean isJoinSqlTempTable = sessionId != null && sessionId.length() > 0 && pageId != null && pageId.length() > 0 && joinSql != null && joinSql.length() > 0;
			
			String sqlColumn = null;
			if (column == null || column.size() == 0)
				sqlColumn = "";
			else
				sqlColumn = TablePageColumn.convertToSql(column);
			
			StringBuilder sqlFilter = new StringBuilder();
			List<Object> listFilterValue = new ArrayList<Object>();
			if (filter == null || filter.size() == 0) {
				if (isJoinSqlTempTable)
					sqlFilter.append("WHERE b.session_id = '").append(sessionId).append("' and b.page_id = '").append(pageId).append("' and ").append(joinSql);
			} else {
				if (isJoinSqlTempTable) {
					sqlFilter.append("WHERE b.session_id = '").append(sessionId).append("' and b.page_id = '").append(pageId).append("' and ").append(joinSql).append(" AND (").append(TablePageFilter.convertToSql(filter, listFilterValue)).append(")");
				} else {
					sqlFilter.append("WHERE ").append(TablePageFilter.convertToSql(filter, listFilterValue));
				}
			}
			
			String sqlSort = null;
			if (sort != null && sort.size() > 0)
				sqlSort = TablePageSort.convertToSql(sort);
			else {
				for (TablePageColumn c : column) {
					if (c.isVirtualColumn())
						continue;
					sqlSort = c.getColumnName();
					break;
				}
			}
			
			StringBuilder sql = new StringBuilder();
			
			if (page > 0) {
				if (isJoinSqlTempTable) {
					sql.append("select ").append(sqlColumn).append(" from ");
					if (tableOwner == null)
						sql.append(tableName);
					else
						sql.append(tableOwner).append(".").append(tableName);
					sql.append(" a, sql_temp_table b ").append(sqlFilter).append(" order by ").append(sqlSort).append(" limit ?, ?");
				} else {
					sql.append("select ").append(sqlColumn).append(" from ");
					if (tableOwner == null)
						sql.append(tableName);
					else
						sql.append(tableOwner).append(".").append(tableName);
					sql.append(" a ").append(sqlFilter).append(" order by ").append(sqlSort).append(" limit ?, ?");
				}

				// get total record
				StringBuilder sqlTotalRecord = new StringBuilder();
				
				if (isJoinSqlTempTable) {
					sqlTotalRecord.append("select count(*) from ");
					if (tableOwner == null)
						sqlTotalRecord.append(tableName);
					else
						sqlTotalRecord.append(tableOwner).append(".").append(tableName);
					sqlTotalRecord.append(" a, sql_temp_table b ").append(sqlFilter);
				} else {
					sqlTotalRecord.append("select count(*) from ");
					if (tableOwner == null)
						sqlTotalRecord.append(tableName);
					else
						sqlTotalRecord.append(tableOwner).append(".").append(tableName);
					sqlTotalRecord.append(" a ").append(sqlFilter);
				}
				
				pstmtTablePageTotalRecord = conn.prepareStatement(sqlTotalRecord.toString());
			}
			else {
				if (isJoinSqlTempTable) {
					sql.append("select ").append(sqlColumn).append(" from ");
					if (tableOwner == null)
						sql.append(tableName);
					else
						sql.append(tableOwner).append(".").append(tableName);
					sql.append(" a, sql_temp_table b ").append(sqlFilter).append(" order by ").append(sqlSort);
				} else {
					sql.append("select ").append(sqlColumn).append(" from ");
					if (tableOwner == null)
						sql.append(tableName);
					else
						sql.append(tableOwner).append(".").append(tableName);
					sql.append(" a ").append(sqlFilter).append(" order by ").append(sqlSort);
				}
			}
			
			pstmtTablePage = conn.prepareStatement(sql.toString());
			
			int k;
			for (k = 0; k < listFilterValue.size(); k++) {
				Object obj = listFilterValue.get(k);
				if (obj instanceof String) {
					pstmtTablePage.setString(k+1, (String)obj);
				}
				else if (obj instanceof Date) {
					pstmtTablePage.setTimestamp(k+1, new Timestamp(((Date)obj).getTime()));
				}
				else if (obj instanceof Double) {
					pstmtTablePage.setDouble(k+1, (Double)obj);
				}
				else {
					pstmtTablePage.setString(k+1, obj.toString());
				}
				
				if (pstmtTablePageTotalRecord != null) {
					if (obj instanceof String) {
						pstmtTablePageTotalRecord.setString(k+1, (String)obj);
					}
					else if (obj instanceof Date) {
						pstmtTablePageTotalRecord.setTimestamp(k+1, new Timestamp(((Date)obj).getTime()));
					}
					else if (obj instanceof Double) {
						pstmtTablePageTotalRecord.setDouble(k+1, (Double)obj);
					}
					else {
						pstmtTablePageTotalRecord.setString(k+1, obj.toString());
					}
				}
			}
			
			if (page > 0) {
				pstmtTablePage.setInt(k+1, (page - 1) * offset);
				pstmtTablePage.setInt(k+2, offset);
			}

			int totalRecord = 0;
			if (pstmtTablePageTotalRecord != null) {
				ResultSet r = pstmtTablePageTotalRecord.executeQuery();
				try {
					r.next();
					totalRecord = r.getInt(1);
				}
				finally {
					r.close();
				}
			}
			
			return new TablePageResult(pstmtTablePage.executeQuery(), totalRecord);
		}
		else {
			throw new Exception("You must call tablePageEnd before begin a new one");
		}
	}

	@Override
	public void tablePageEnd() {
		if (pstmtTablePage != null) {
			try {
				pstmtTablePage.close();
			}
			catch (SQLException ignore) {}
			
			try {
				pstmtTablePageTotalRecord.close();
			}
			catch (SQLException ignore) {}
			
			pstmtTablePage = null;
			pstmtTablePageTotalRecord = null;
		}
	}

	@Override
	public AutoPageInfo autoPageGetDetail(String pageId) throws Exception {
		PreparedStatement pstmt = conn.prepareStatement("SELECT db_name, page_type, cmd_text FROM auto_page WHERE page_id = ?");
		try {
			pstmt.setString(1, pageId);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					return new AutoPageInfo(pageId, r.getString("page_type"), r.getString("cmd_text"), r.getString("db_name"));
				}
				else {
					throw new Exception("Page id [" + pageId + "] doesn't exist");
				}
			}
			finally {
				r.close();
			}
		}
		finally {
			try {
				pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public SqlInputInfo autoPageGetSqlInput(String pageId, String inputName)
			throws Exception {
		PreparedStatement pstmt = conn.prepareStatement("SELECT input_id, input_type, input_seq, input_op, input_logic, input_optional, input_list_source, input_default_value, input_custom_flag FROM sql_input WHERE page_id = ? AND input_name = ? ORDER BY input_seq");
		try {
			pstmt.setString(1, pageId);
			pstmt.setString(2, inputName);
			ResultSet r = pstmt.executeQuery();
			if (r.next()) {
				SqlInputInfo info = new SqlInputInfo(
					r.getString("input_id"),
					inputName,
					r.getString("input_type"),
					r.getInt("input_seq"),
					r.getString("input_op"),
					r.getString("input_logic"),
					r.getString("input_optional"),
					r.getString("input_list_source"),
					r.getString("input_default_value"),
					r.getInt("input_custom_flag")
				);
				return info;
			}
			else
				return null;
		}
		finally {
			try {
				pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public List<SqlInputInfo> autoPageGetSqlInputs(String pageId)
			throws Exception {
		PreparedStatement pstmt = conn.prepareStatement("SELECT input_id, input_name, input_type, input_seq, input_op, input_logic, input_optional, input_list_source, input_default_value, input_custom_flag FROM sql_input WHERE page_id = ? ORDER BY input_seq");
		try {
			pstmt.setString(1, pageId);
			ResultSet r = pstmt.executeQuery();
			List<SqlInputInfo> list = new ArrayList<SqlInputInfo>();
			while (r.next()) {
				SqlInputInfo info = new SqlInputInfo(
					r.getString("input_id"),
					r.getString("input_name"),
					r.getString("input_type"),
					r.getInt("input_seq"),
					r.getString("input_op"),
					r.getString("input_logic"),
					r.getString("input_optional"),
					r.getString("input_list_source"),
					r.getString("input_default_value"),
					r.getInt("input_custom_flag")
				);
				list.add(info);
			}
			r.close();
			return list;
		}
		finally {
			try {
				pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public List<SqlOutputInfo> autoPageGetSqlOutputs(String pageId)
			throws Exception {
		PreparedStatement pstmt = conn.prepareStatement("SELECT output_id, output_name, output_seq, output_type FROM sql_output WHERE page_id = ? ORDER BY output_seq");
		try {
			pstmt.setString(1, pageId);
			ResultSet r = pstmt.executeQuery();
			List<SqlOutputInfo> list = new ArrayList<SqlOutputInfo>();
			while (r.next()) {
				SqlOutputInfo info = new SqlOutputInfo(
					r.getString("output_id"),
					r.getString("output_name"),
					r.getInt("output_seq"),
					r.getString("output_type")
				);
				list.add(info);
			}
			r.close();
			return list;
		}
		finally {
			try {
				pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public int autoPageSetSqlTempTable(String pageId, String SID,
			ResultSet source, List<SqlOutputInfo> cols) throws Exception {
		// update Session Id last active time
		PreparedStatement pstmtSID = conn.prepareStatement("SELECT session_id FROM session_list WHERE session_id = ? FOR UPDATE");
		PreparedStatement pstmtSIDUpdate = conn.prepareStatement("UPDATE session_list SET last_active_time = SYSDATE() WHERE session_id = ?");
		try {
			pstmtSID.setString(1, SID);
			ResultSet r = pstmtSID.executeQuery();
			if (r.next()) {
				pstmtSIDUpdate.setString(1, SID);
				pstmtSIDUpdate.executeUpdate();
				conn.commit();
			}
			else {
				throw new Exception("Session Id [" + SID + "] not exist");
			}
		}
		finally {
			pstmtSID.close();
		}
		
		PreparedStatement pstmt = conn.prepareStatement("INSERT INTO sql_temp_table (session_id, page_id, " +
				"item1, item2, item3, item4, item5, item6, item7, item8, item9, item10, item11, item12, item13, item14, item15, item16, item17, item18, item19, item20, item21, item22, item23, item24, item25, item26, item27, item28, item29, item30, " +
				"date1, date2, date3, date4, date5, date6, date7, date8, date9, date10, " +
				"num1, num2, num3, num4, num5, num6, num7, num8, num9, num10) VALUES (" +
				"?,?," +
				"?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
				"?,?,?,?,?,?,?,?,?,?," +
				"?,?,?,?,?,?,?,?,?,?)");

		int ITEM_START = 3, ITEM_OFFSET = 30;
		int DATE_START = 33, DATE_OFFSET = 10;
		int NUM_START = 43, NUM_OFFSET = 10;
		
		try {
			ResultSetMetaData meta = source.getMetaData();
			Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
			for (int i = 1; i <= meta.getColumnCount(); i++)
				ht.put(meta.getColumnName(i), meta.getColumnType(i));
			
			String outputType, outputId;
			int iItem = ITEM_START, iDate = DATE_START, iNum = NUM_START;
			int iItemMax = ITEM_START + ITEM_OFFSET;
			int iDateMax = DATE_START + DATE_OFFSET;
			int iNumMax = NUM_START + NUM_OFFSET;
			List<SqlOutputInfo> validatedCols = new ArrayList<SqlOutputInfo>();
			for (SqlOutputInfo col : cols) {
				outputType = col.getOutputType();
				outputId = col.getOutputId();
				
				col.setParameterIndex(0);
				if (ht.containsKey(outputId)) {
					if (outputType.equals("text")) {
						if (iItem < iItemMax) {
							col.setParameterIndex(iItem);
							col.setTempColumnName("item" + (iItem - ITEM_START + 1));
							iItem++;
						}
						else
							throw new Exception("Temp Table column not enough to store output with name [" + col.getOutputName() + "] and type [" + outputType + "]");
					}
					else if (outputType.equals("date")) {
						if (iDate < iDateMax) {
							col.setParameterIndex(iDate);
							col.setTempColumnName("date" + (iDate - DATE_START + 1));
							iDate++;
						}
						else
							throw new Exception("Temp Table column not enough to store output with name [" + col.getOutputName() + "] and type [" + outputType + "]");
					}
					else if (outputType.equals("datetime")) {
						if (iDate < iDateMax) {
							col.setParameterIndex(iDate);
							col.setTempColumnName("date" + (iDate - DATE_START + 1));
							iDate++;
						}
						else
							throw new Exception("Temp Table column not enough to store output with name [" + col.getOutputName() + "] and type [" + outputType + "]");
					}
					else if (outputType.equals("number")) {
						if (iNum < iNumMax) {
							col.setParameterIndex(iNum);
							col.setTempColumnName("num" + (iNum - NUM_START + 1));
							iNum++;
						}
						else
							throw new Exception("Temp Table column not enough to store output with name [" + col.getOutputName() + "] and type [" + outputType + "]");
					}
					else {
						throw new Exception("Output type [" + outputType + "] not supported for output name [" + col.getOutputName() + "]");
					}
				}
				
				if (col.getParameterIndex() > 0) {
					validatedCols.add(col);
				}
			}
			
			// insert into SQL_TEMP_TABLE
			int rowIndex = 0;
			while (source.next()) {
				// reset parameter
				pstmt.setString(1, SID);
				pstmt.setString(2, pageId);
				for (int i = ITEM_START; i < iItemMax; i++)
					pstmt.setNull(i, Types.VARCHAR);
				for (int i = DATE_START; i < iDateMax; i++)
					pstmt.setNull(i, Types.DATE);
				for (int i = NUM_START; i < iNumMax; i++)
					pstmt.setNull(i, Types.NUMERIC);
				
				// assign parameter value
				for (SqlOutputInfo col : validatedCols) {
					outputType = col.getOutputType();
					outputId = col.getOutputId();
					if (outputType.equals("text")) {
						pstmt.setString(col.getParameterIndex(), source.getString(outputId));
					}
					else if (outputType.equals("date")) {
						pstmt.setDate(col.getParameterIndex(), source.getDate(outputId));
					}
					else if (outputType.equals("datetime")) {
						pstmt.setTimestamp(col.getParameterIndex(), source.getTimestamp(outputId));
					}
					else if (outputType.equals("number")) {
						pstmt.setDouble(col.getParameterIndex(), source.getDouble(outputId));
					}
					else {
						throw new Exception("Output type [" + outputType + "] not supported for output name [" + col.getOutputName() + "]");
					}
				}
				pstmt.addBatch();
				rowIndex++;
				if (rowIndex % 5000 == 0) {
					pstmt.executeBatch();
					pstmt.clearBatch();
				}
			}
			pstmt.executeBatch();
			conn.commit();
			
			return rowIndex;
		}
		catch (Exception E) {
			conn.rollback();
			throw E;
		}
		finally {
			pstmt.close();
		}
	}

	@Override
	public void autoPageClearSqlTempTable(String pageId, String SID)
			throws Exception {
		PreparedStatement pstmtDelete = conn.prepareStatement("DELETE FROM sql_temp_table WHERE session_id = ? AND page_id = ?");
		try {
			pstmtDelete.setString(1, SID);
			pstmtDelete.setString(2, pageId);
			pstmtDelete.executeUpdate();
			conn.commit();
		}
		catch (Exception E) {
			conn.rollback();
			throw E;
		}
		finally {
			pstmtDelete.close();
		}
	}

	@Override
	public List<SpParamInfo> autoPageGetSpParams(String pageId)
			throws Exception {
		PreparedStatement pstmt = conn.prepareStatement("SELECT param_name, param_type, param_direction, param_seq FROM sp_param WHERE page_id = ? ORDER BY param_seq");
		try {
			pstmt.setString(1, pageId);
			ResultSet r = pstmt.executeQuery();
			List<SpParamInfo> list = new ArrayList<SpParamInfo>();
			while (r.next()) {
				SpParamInfo info = new SpParamInfo(
					pageId,
					r.getString("param_name"),
					r.getString("param_type"),
					r.getString("param_direction"),
					r.getInt("param_seq")
				);
				list.add(info);
			}
			r.close();
			return list;
		}
		finally {
			try {
				pstmt.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public PasswordConfirmation adminUserCreate(String userName,
			String emailAddress, int[] roleIds) throws Exception {
		String confirmId = UUID.randomUUID().toString().replaceAll("-", "");
		PreparedStatement pstmtGet = conn.prepareStatement("SELECT config_value FROM global_config WHERE group_name = ? AND config_name = ? FOR UPDATE");
		PreparedStatement pstmtInsert = conn.prepareStatement("INSERT INTO user_list (user_row_id, user_name, password, disabled, email_address) VALUES (?, ?, ?, ?, ?)");
		PreparedStatement pstmtInsRoles = conn.prepareStatement("INSERT INTO USER_ROLE (user_row_id, role_id) VALUES (?, ?)");
		PreparedStatement pstmtUpd = conn.prepareStatement("UPDATE global_config SET config_value = ? WHERE group_name = ? AND config_name = ?");
		try {
			int rowId;
			pstmtGet.setString(1, "ADMIN");
			pstmtGet.setString(2, "USER_ROW_ID");
			ResultSet r = pstmtGet.executeQuery();
			try {
				if (r.next()) {
					rowId = Integer.parseInt(r.getString("config_value"));
				}
				else {
					throw new Exception("Configuration error. Cannot found ADMIN.USER_ROW_ID in GLOBAL_CONFIG");
				}
			}
			finally {
				r.close();
			}
			
			pstmtUpd.setString(1, String.valueOf(rowId + 1));
			pstmtUpd.setString(2, "ADMIN");
			pstmtUpd.setString(3, "USER_ROW_ID");
			pstmtUpd.executeUpdate();
			
			pstmtInsert.setInt(1, rowId);
			pstmtInsert.setString(2, userName);
			pstmtInsert.setString(3, "confirm" + confirmId);
			pstmtInsert.setString(4, "Y");
			pstmtInsert.setString(5, emailAddress);
			pstmtInsert.executeUpdate();
			
			for (int roleId : roleIds) {
				pstmtInsRoles.setInt(1, rowId);
				pstmtInsRoles.setInt(2, roleId);
				pstmtInsRoles.executeUpdate();
			}
			
			conn.commit();
			
			return new PasswordConfirmation(rowId, emailAddress, confirmId);
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
		finally {
			pstmtGet.close();
			pstmtInsert.close();
			pstmtInsRoles.close();
			pstmtUpd.close();
		}
	}

	@Override
	public void adminUserDelete(String userName) throws Exception {
		PreparedStatement pstmtGet = conn.prepareStatement("SELECT user_row_id FROM user_list WHERE user_name = ? FOR UPDATE");
		PreparedStatement pstmtDel = conn.prepareStatement("DELETE FROM user_list WHERE user_row_id = ?");
		PreparedStatement pstmtDelRole = conn.prepareStatement("DELETE FROM user_role WHERE user_row_id = ?");
		try {
			int userRowId;
			pstmtGet.setString(1, userName);
			ResultSet r = pstmtGet.executeQuery();
			try {
				if (r.next()) {
					userRowId = r.getInt("user_row_id");
				}
				else {
					throw new Exception("USER_NAME [" + userName + "] not exist");
				}
			}
			finally {
				r.close();
			}
			
			pstmtDelRole.setInt(1, userRowId);
			pstmtDelRole.executeUpdate();
			
			pstmtDel.setInt(1, userRowId);
			pstmtDel.executeUpdate();
			
			conn.commit();
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
		finally {
			pstmtGet.close();
			pstmtDel.close();
			pstmtDelRole.close();
		}
	}

	@Override
	public void adminUserChangePassword(String userName, String oldPassword,
			String newPassword) throws Exception {
		boolean isPasswordMismatch = false;
		int retryCount = 0;
		PreparedStatement pstmtGet = conn.prepareStatement("SELECT disabled, password, retry_count FROM user_list WHERE user_name = ? FOR UPDATE");
		PreparedStatement pstmtUpdate = conn.prepareStatement("UPDATE user_list SET password = ? WHERE user_name = ?");
		try {
			pstmtGet.setString(1, userName);
			ResultSet r = pstmtGet.executeQuery();
			String password;
			boolean disabled;
			if (r.next()) {
				password = r.getString("password");
				disabled = r.getString("disabled").equals("Y");
				retryCount = r.getInt("retry_count") + 1;
			}
			else {
				throw new Exception("User Name [" + userName + "] doesn't exist in database");
			}
			r.close();
			
			if (disabled)
				throw new Exception("UserName [" + userName + "] disabled");
			if (password.startsWith("confirm"))
				throw new Exception("This account still waiting for confirmation");
			if (!password.equals(oldPassword)) {
				isPasswordMismatch = true;
				throw new Exception("Provided password not match with in database for user [" + userName + "]");
			}
			
			pstmtUpdate.setString(1, newPassword);
			pstmtUpdate.setString(2, userName);
			pstmtUpdate.executeUpdate();
			conn.commit();
		}
		catch (Exception e) {
			if (isPasswordMismatch) {
				PreparedStatement pstmt = null;
				try {
					if (MAX_PASSWORD_RETRY_COUNT > 0 && retryCount > MAX_PASSWORD_RETRY_COUNT) {
						pstmt = conn.prepareStatement("UPDATE user_list SET disabled = 'Y' WHERE user_name = ?");
						pstmt.setString(1, userName);
						pstmt.executeUpdate();
					}
					else {
						pstmt = conn.prepareStatement("UPDATE user_list SET retry_count = ? WHERE user_name = ?");
						pstmt.setInt(1, retryCount);
						pstmt.setString(2, userName);
						pstmt.executeUpdate();
					}
					conn.commit();
				}
				catch (Exception ignore) {
					conn.rollback();
				}
				finally {
					pstmt.close();
				}
			}
			else
				conn.rollback();
			throw e;
		}
		finally {
			try {
				pstmtGet.close();
			}
			catch (Exception E) {}
			try {
				pstmtUpdate.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public List<RoleNameListModel> adminUserGetRoles(String userName) throws Exception {
		PreparedStatement pstmt = null;
		PreparedStatement pstmtRole = null;
		try {
			pstmt = conn.prepareStatement("SELECT user_row_id FROM user_list WHERE user_name = ?");
			pstmt.setString(1, userName);
			ResultSet r = pstmt.executeQuery();
			int userRowId;
			try {
				if (r.next()) {
					userRowId = r.getInt("user_row_id");
				}
				else {
					throw new Exception("UserName [" + userName + "] doesn't exist");
				}
			}
			finally {
				r.close();
			}
			
			pstmtRole = conn.prepareStatement("SELECT a.role_id, b.role_name FROM user_role a, role_name_list b WHERE a.user_row_id = ? and a.role_id = b.role_id ORDER BY b.role_name");
			pstmtRole.setInt(1, userRowId);
			
			r = pstmtRole.executeQuery();
			try {
				List<RoleNameListModel> list = new ArrayList<RoleNameListModel>();
				while (r.next()) {
					RoleNameListModel m = new RoleNameListModel();
					m.ROLE_ID = r.getInt("role_id");
					m.ROLE_NAME = r.getString("role_name");
					list.add(m);
				}
				return list;
			}
			finally {
				r.close();
			}
		}
		finally {
			try {
				if (pstmt != null)
					pstmt.close();
			}
			catch (Exception E) {}
			try {
				if (pstmtRole != null)
					pstmtRole.close();
			}
			catch (Exception E) {}
		}
	}

	@Override
	public void adminUserUpdate(int userRowId, String userName,
			boolean disabled, String emailAddress, int[] roleIds) throws Exception {
		PreparedStatement pstmtGet = conn.prepareStatement("SELECT disabled FROM user_list WHERE user_row_id = ? FOR UPDATE");
		PreparedStatement pstmtUpd = conn.prepareStatement("UPDATE user_list SET user_name = ?, disabled = ?, email_address = ? WHERE user_row_id = ?");
		PreparedStatement pstmtDelRoles = conn.prepareStatement("DELETE FROM user_role WHERE user_row_id = ?");
		PreparedStatement pstmtInsRoles = conn.prepareStatement("INSERT INTO user_role (user_row_id, role_id) VALUES (?, ?)");
		try {
			pstmtGet.setInt(1, userRowId);
			ResultSet r = pstmtGet.executeQuery();
			try {
				if (!r.next())
					throw new Exception("USER_ROW_ID [" + userRowId + "] not exist");
			}
			finally {
				r.close();
			}
			
			pstmtUpd.setString(1, userName);
			pstmtUpd.setString(2, disabled ? "Y" : "N");
			pstmtUpd.setString(3, emailAddress);
			pstmtUpd.setInt(4, userRowId);
			pstmtUpd.executeUpdate();
			
			pstmtDelRoles.setInt(1, userRowId);
			pstmtDelRoles.executeUpdate();
			
			for (int i = 0; i < roleIds.length; i++) {
				pstmtInsRoles.setInt(1, userRowId);
				pstmtInsRoles.setInt(2, roleIds[i]);
				pstmtInsRoles.executeUpdate();
			}
			
			conn.commit();
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
		finally {
			pstmtGet.close();
			pstmtUpd.close();
			pstmtDelRoles.close();
			pstmtInsRoles.close();
		}
	}

	@Override
	public PasswordConfirmation adminUserResetPassword(String userName)
			throws Exception {
		String confirmId = UUID.randomUUID().toString().replaceAll("-", "");
		PreparedStatement pstmtGet = conn.prepareStatement("SELECT user_row_id, email_address FROM user_list WHERE user_name = ? FOR UPDATE");
		PreparedStatement pstmtUpd = conn.prepareStatement("UPDATE user_list SET password = ? WHERE user_row_id = ?");
		try {
			int userRowId;
			String emailAddress;
			pstmtGet.setString(1, userName);
			ResultSet r = pstmtGet.executeQuery();
			try {
				if (r.next()) {
					userRowId = r.getInt("user_row_id");
					emailAddress = r.getString("email_address");
				}
				else {
					throw new Exception("USER_NAME [" + userName + "] not exist");
				}
			}
			finally {
				r.close();
			}
			
			pstmtUpd.setString(1, "confirm" + confirmId);
			pstmtUpd.setInt(2, userRowId);
			pstmtUpd.executeUpdate();
			
			conn.commit();
			
			return new PasswordConfirmation(userRowId, emailAddress, confirmId);
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
		finally {
			pstmtGet.close();
			pstmtUpd.close();
		}
	}

	@Override
	public void adminUserConfirmAccount(String userName, String confirmId,
			String newPassword) throws Exception {
		PreparedStatement pstmtGet = conn.prepareStatement("SELECT password FROM user_list WHERE user_name = ? FOR UPDATE");
		PreparedStatement pstmtUpd = null;
		try {
			String s;
			pstmtGet.setString(1, userName);
			ResultSet r = pstmtGet.executeQuery();
			try {
				if (r.next())
					s = r.getString("password");
				else
					throw new Exception("USER_NAME [" + userName + "] not exist");
			}
			finally {
				r.close();
			}
			
			if (!s.equals("confirm" + confirmId))
				throw new Exception("ConfirmId [" + confirmId + "] not match");
			
			// mantis #10: account confirmed should set DISABLED as N
			pstmtUpd = conn.prepareStatement("UPDATE user_list SET password = ?, retry_count = 0, disabled = 'N' WHERE user_name = ?");
			pstmtUpd.setString(1, newPassword);
			pstmtUpd.setString(2, userName);
			pstmtUpd.executeUpdate();
			
			conn.commit();
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
		finally {
			pstmtGet.close();
			if (pstmtUpd != null)
				pstmtUpd.close();
		}
	}

	@Override
	public List<RoleNameListModel> adminRoleGetAll() throws Exception {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement("SELECT role_id, role_name, role_desc FROM role_name_list");
			ResultSet r = pstmt.executeQuery();
			List<RoleNameListModel> list = new ArrayList<RoleNameListModel>();
			while (r.next()) {
				RoleNameListModel m = new RoleNameListModel();
				m.ROLE_ID = r.getInt(1);
				m.ROLE_NAME = r.getString(2);
				m.ROLE_DESC = r.getString(3);
				list.add(m);
			}
			return list;
		}
		finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				}
				catch (Exception ignore) {}
			}
		}
	}
	
	private static final SecureRandom numberGenerator = new SecureRandom();
	
	@Override
	public void logHistory(HistoryTable table, Long rowId, HistoryAction historyAction, String actionDesc, Timestamp actionTime, String userName, HistoryData... datas) throws Exception {
		PreparedStatement pstmt = conn.prepareStatement("insert into history (log_id, table_id, row_id, action_time, action_id, action_desc, action_user) values (?, ?, ?, ?, ?, ?, ?)");
		PreparedStatement pstmtData = conn.prepareStatement("insert into history_data (log_id, data_seq, attr_name, old_attr_value, new_attr_value) values (?, ?, ?, ?, ?)");
		try {
			byte[] randomBytes = new byte[8];
			numberGenerator.nextBytes(randomBytes);
			long logId = 0;
			for (int i = 0; i < 8; i++)
				logId = (logId << 8) | (randomBytes[i] & 0xff);
			
			pstmt.setLong(1, logId);
			pstmt.setInt(2, table.getId());
			if (rowId == null)
				pstmt.setNull(3, Types.NUMERIC);
			else
				pstmt.setLong(3, rowId);
			pstmt.setTimestamp(4, actionTime);
			pstmt.setInt(5, historyAction.getId());
			if (StringUtils.isEmpty(actionDesc))
				pstmt.setNull(6, Types.VARCHAR);
			else
				pstmt.setString(6, actionDesc);
			pstmt.setString(7, userName);
			pstmt.executeUpdate();
			
			for (int i = 0; i < datas.length; i++) {
				HistoryData data = datas[i];
				pstmtData.setLong(1, logId);
				pstmtData.setInt(2, i);
				pstmtData.setString(3, data.getAttrName());
				pstmtData.setString(4, data.getOldAttrValue());
				pstmtData.setString(5, data.getNewAttrValue());
				pstmtData.addBatch();
			}
			pstmtData.executeBatch();
		} finally {
			pstmt.close();
			pstmtData.close();
		}
	}
	
	private static String SQL_1 = "select b.allow_export\r\n" + 
			"from user_role a inner join role_table_page b on a.role_id = b.role_id\r\n" + 
			"inner join table_page c on b.table_page_id = c.table_page_id\r\n" + 
			"where a.user_row_id = ? and c.class_name = ?";
	
	@Override
	public boolean isAllowedToExportTablePage(int user_row_id, String class_name) throws Exception {
		try (PreparedStatementWrapper pstmt = new PreparedStatementWrapper(conn.prepareStatement(SQL_1))) {
			pstmt.setInt(1, user_row_id);
			pstmt.setString(2, class_name);
			try (ResultSetWrapper r = pstmt.executeQueryWrapper()) {
				if (r.next()) {
					int allow_export = r.getInt(1);
					return allow_export == 1;
				} else {
					return false;
				}
			}
		}
	}

}
