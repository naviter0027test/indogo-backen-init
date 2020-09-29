package com.lionpig.webui.http.struct;

import java.io.File;
import java.util.Hashtable;

import javax.servlet.ServletContext;

import org.apache.commons.fileupload.FileItem;

import com.lionpig.language.L;
import com.lionpig.sql.S;
import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.util.C;

public class FunctionItem {
	private IConnection conn;
	private String SID;
	private SessionInfo info;
	private LogInfo log;
	private Hashtable<String, String> params;
	private Hashtable<String, FileItem> uploadedFiles;
	private ServletContext servletContext;
	private File tempFolder;
	private L language;
	private S sql;
	
	public FunctionItem(IConnection conn, String SID, SessionInfo info, LogInfo log,
			Hashtable<String, String> params, Hashtable<String, FileItem> uploadedFiles,
			ServletContext servletContext, File tempFolder) {
		this.conn = conn;
		this.SID = SID;
		this.info = info;
		this.log = log;
		this.params = params;
		this.uploadedFiles = uploadedFiles;
		this.servletContext = servletContext;
		this.tempFolder = tempFolder;
		this.language = L.getInstance(params);
		this.sql = S.getInstance(language, servletContext.getInitParameter(C.DB_TYPE));
	}
	
	public IConnection getConnection() {
		return conn;
	}
	public String getSID() {
		return SID;
	}
	public SessionInfo getSessionInfo() {
		return info;
	}
	public LogInfo getLogInfo() {
		return log;
	}
	public Hashtable<String, String> getRequestParameters() {
		return params;
	}
	public Hashtable<String, FileItem> getUploadedFiles() {
		return uploadedFiles;
	}
	public ServletContext getServletContext() {
		return servletContext;
	}
	public File getTempFolder() {
		return tempFolder;
	}
	public L getLanguage() {
		return language;
	}
	public S getSql() {
		return sql;
	}
}
