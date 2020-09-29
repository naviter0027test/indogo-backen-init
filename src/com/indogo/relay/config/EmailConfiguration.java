package com.indogo.relay.config;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;

import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.IFunction;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class EmailConfiguration implements IFunction {

	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp,
			FunctionItem fi) throws Exception {
		Hashtable<String, String> params = fi.getRequestParameters();
		String action = Helper.getString(params, "action", true);
		if (action.equals("init")) {
			IConnection c = fi.getConnection();
			String with_tls = c.getGlobalConfig("ADMIN_EMAIL", "WITH_TLS");
			StringBuilder sb = new StringBuilder();
			sb.append(c.getGlobalConfig("ADMIN_EMAIL", "HOST_NAME")).append(C.char_31)
			.append(c.getGlobalConfig("ADMIN_EMAIL", "SMTP_PORT")).append(C.char_31)
			.append(c.getGlobalConfig("ADMIN_EMAIL", "USER_NAME")).append(C.char_31)
			.append("").append(C.char_31)
			.append(with_tls == null ? "false" : with_tls.toLowerCase()).append(C.char_31)
			.append(c.getGlobalConfig("ADMIN_EMAIL", "FROM"));
			return sb.toString();
		}
		else if (action.equals("update")) {
			String with_tls = Helper.getString(params, "with_tls", false);
			IConnection c = fi.getConnection();
			c.setGlobalConfig("ADMIN_EMAIL", "HOST_NAME", Helper.getString(params, "host_name", true));
			c.setGlobalConfig("ADMIN_EMAIL", "SMTP_PORT", Helper.getString(params, "smtp_port", true));
			c.setGlobalConfig("ADMIN_EMAIL", "USER_NAME", Helper.getString(params, "user_name", false));
			c.setGlobalConfig("ADMIN_EMAIL", "PASSWORD", Helper.getString(params, "password", false));
			c.setGlobalConfig("ADMIN_EMAIL", "WITH_TLS", with_tls == null ? "false" : with_tls);
			c.setGlobalConfig("ADMIN_EMAIL", "FROM", Helper.getString(params, "mail_from", true));
			return "1";
		}
		else
			throw new Exception(String.format(C.unknown_action, action));
	}
	
	public void sendMail(FunctionItem fi, String to, String subject, String htmlBody) throws Exception {
		Hashtable<String, String> conf = fi.getConnection().getGlobalConfig("ADMIN_EMAIL");
		int smtpPort = Integer.parseInt(conf.get("SMTP_PORT"));
		String login = conf.get("USER_NAME");
		String password = conf.get("PASSWORD");
		boolean withTLS = Boolean.parseBoolean(conf.get("WITH_TLS"));
		String hostName = conf.get("HOST_NAME");
		String from = conf.get("FROM");
		
		HtmlEmail email = new HtmlEmail();
		email.setSmtpPort(smtpPort);
		if (login != null)
			email.setAuthenticator(new DefaultAuthenticator(login, password));
		email.setDebug(false);
		email.setStartTLSEnabled(withTLS);
		email.setHostName(hostName);
		email.setFrom(from);
		email.addTo(to);
		email.setSubject(subject);
		email.setHtmlMsg(htmlBody);
		email.send();
	}

}
