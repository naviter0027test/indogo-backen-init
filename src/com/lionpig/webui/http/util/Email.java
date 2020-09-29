package com.lionpig.webui.http.util;

import java.util.Hashtable;
import java.util.List;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.SimpleEmail;

import com.lionpig.webui.http.struct.FunctionItem;

public class Email {
	
	private int smtpPort;
	private String login, password, hostName, from;
	private boolean withTLS;
	
	public Email(FunctionItem fi) throws Exception {
		Hashtable<String, String> conf = fi.getConnection().getGlobalConfig("ADMIN_EMAIL");
		smtpPort = Integer.parseInt(conf.get("SMTP_PORT"));
		login = conf.get("USER_NAME");
		password = conf.get("PASSWORD");
		withTLS = Boolean.parseBoolean(conf.get("WITH_TLS"));
		hostName = conf.get("HOST_NAME");
		from = conf.get("FROM");
	}
	
	public void send(List<String> listTo, String subject, String msg) {
		for (String to : listTo) {
			try {
				SimpleEmail email = new SimpleEmail();
				email.setSmtpPort(smtpPort);
				if (login != null)
					email.setAuthenticator(new DefaultAuthenticator(login, password));
				email.setDebug(false);
				email.setTLS(withTLS);
				email.setHostName(hostName);
				email.setFrom(from);
				email.addTo(to);
				email.setCharset("UTF-8");
				email.setSubject(subject);
				email.setMsg(msg);
				email.send();
			}
			catch (Exception ignore) {
				ignore.printStackTrace();
			}
		}
	}
}
