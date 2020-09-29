package com.lionpig.webui.http.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Date;
import java.util.Hashtable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class License {
	private static License singleton = new License();
	public static License getInstance() {
		return singleton;
	}
	
	private String key;
	private License() {
		key = "lJIVfbvgYb4t6YnovEueCg==";
	}

	private String licenseType = "trial";
	public String getLicenseType() {
		return licenseType;
	}
	
	private Date warningDate = null;
	public Date getWarningDate() {
		return warningDate;
	}
	
	private Date expireDate = null;
	public Date getExpireDate() {
		return expireDate;
	}
	
	private final static String LOCK = "lock";
	private final static String LICENSE_TYPE = "LICENSE_TYPE";
	private final static String WARNING_DATE = "WARNING_DATE";
	private final static String EXPIRE_DATE = "EXPIRE_DATE";
	
	public void init(String s) throws Exception {
		if (s == null || s.length() == 0)
			return;
		
		synchronized (LOCK) {
			SecretKeySpec skeySpec = new SecretKeySpec(Base64.decodeBase64(key), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] original = cipher.doFinal(Base64.decodeBase64(s));
			String msg = new String(original);
			
			// read license
			Hashtable<String, String> license = new Hashtable<String, String>();
			StringReader sr = new StringReader(msg);
			BufferedReader br = new BufferedReader(sr);
			String line;
			String[] tokens;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;
				tokens = line.split("=");
				if (tokens.length >= 2)
					license.put(tokens[0], tokens[1]);
			}
			
			// set variable
			if (license.containsKey(LICENSE_TYPE))
				this.licenseType = license.get(LICENSE_TYPE);
			if (license.containsKey(WARNING_DATE))
				this.warningDate = DateFormat.getInstance().parse(license.get(WARNING_DATE));
			else
				this.warningDate = DateFormat.getInstance().parse("20170504");
			
			if (license.containsKey(EXPIRE_DATE))
				this.expireDate = DateFormat.getInstance().parse(license.get(EXPIRE_DATE));
			else
				this.expireDate = DateFormat.getInstance().parse("20170512");
		}
	}
	
	public static void main(String[] args) {
		try {
			String msg = "@brc04cp";
			byte[] original = msg.getBytes();
			
			byte[] key = Base64.decodeBase64("L4QH41H5VoSJVDmL/OB/0g==");
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] b = cipher.doFinal(original);
			String s = Base64.encodeBase64URLSafeString(b);
			System.out.println(s);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
