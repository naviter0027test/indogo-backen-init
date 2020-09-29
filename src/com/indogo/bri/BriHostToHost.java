package com.indogo.bri;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.indogo.bri.wsdl.BrifastserviceBindingStub;
import com.indogo.bri.wsdl.BrifastserviceLocator;
import com.indogo.bri.wsdl.InquiryAccount_CT;
import com.indogo.bri.wsdl.InquiryAccount_CT_Result;
import com.indogo.bri.wsdl.InquiryTransaction_CT;
import com.indogo.bri.wsdl.InquiryTransaction_CT_Result;
import com.indogo.bri.wsdl.InquiryVostro_CT;
import com.indogo.bri.wsdl.InquiryVostro_CT_Result;
import com.indogo.bri.wsdl.PaymentAccount_CT;
import com.indogo.bri.wsdl.PaymentAccount_CT_Result;
import com.indogo.bri.wsdl.RequestTokenCT;
import com.indogo.bri.wsdl.RequestTokenCTResult;
import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.database.MySqlConnection;
import com.lionpig.webui.http.util.C;

public class BriHostToHost {
	
	public static void main(String[] args) {
		try {
			BriHostToHost bri = new BriHostToHost(new MySqlConnection("192.168.1.2", "3306", "indogo", "indogo", "indogo#go#"));
			String token = bri.requestToken();
			String accName = bri.inquiryAccount(token, "009", "315616003");
			System.out.println("accName = " + accName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Log LOG = LogFactory.getLog(BriHostToHost.class);
	private final static String MASTER_KEY = "L4QH41H5VoSJVDmL/OB/0g==";
	
	private String ipAddress, username, password, agentCode;
	private String url;
	private BrifastserviceBindingStub stub;
	
	public BriHostToHost(IConnection conn) throws Exception {
		Hashtable<String, String> conf = conn.getGlobalConfig(C.bri_h2h);
		
		String encryptedPassword = conf.get(C.password);
		SecretKeySpec skeySpec = new SecretKeySpec(Base64.decodeBase64(MASTER_KEY), C.AES);
		Cipher cipher = Cipher.getInstance(C.AES);
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] original = cipher.doFinal(Base64.decodeBase64(encryptedPassword));
		String password = new String(original);
		
		init(conn, password, conf);
	}
	
	public BriHostToHost(IConnection conn, String password) throws Exception {
		Hashtable<String, String> conf = conn.getGlobalConfig(C.bri_h2h);
		init(conn, password, conf);
	}
	
	private void init(IConnection conn, String password, Hashtable<String, String> conf) throws Exception {
		this.password = password;
		
		String[] ipAddresses = StringUtils.split(conf.get(C.ip_address), ',');
		username = conf.get(C.username);
		agentCode = conf.get(C.agent_code);
		url = conf.get(C.url);
		
		Set<String> sets = new HashSet<>();
		for (String ip : ipAddresses) {
			sets.add(ip);
		}
		
		ipAddress = null;
		StringBuilder sb = new StringBuilder();
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
	    while (en.hasMoreElements()) {
	        NetworkInterface i = en.nextElement();
	        for (Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements();) {
	            InetAddress addr = en2.nextElement();
	            if (!addr.isLoopbackAddress()) {
	                if (addr instanceof Inet4Address) {
	                	String s = addr.getHostAddress();
	                	sb.append(s).append("\n");
	                	if (sets.contains(s)) {
	                		ipAddress = s;
	                		break;
	                	}
	                }
	            }
	        }
	        
	        if (ipAddress != null) {
	        	break;
	        }
	    }
	    
	    if (ipAddress == null) {
	    	throw new Exception("Cannot find suitable ip address. Detected ip addresses are:\n" + sb.toString());
	    }
		
		BrifastserviceLocator l = new BrifastserviceLocator();
		l.setBrifastservicePortEndpointAddress(url);
		stub = new BrifastserviceBindingStub(new URL(url), l);
	}
	
	public static String encrypt(String msg) throws Exception {
		byte[] original = msg.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(Base64.decodeBase64(MASTER_KEY), C.AES);
		Cipher cipher = Cipher.getInstance(C.AES);
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] b = cipher.doFinal(original);
		return Base64.encodeBase64URLSafeString(b);
	}
	
	public String requestToken() throws Exception {
		RequestTokenCTResult o = stub.requestToken(new RequestTokenCT(username, password, agentCode, ipAddress));
		LOG.debug("requestTokenCTResult.statusCode = " + o.getStatusCode());
		LOG.debug("requestTokenCTResult.message = " + o.getMessage());
		LOG.debug("requestTokenCTResult.token = " + o.getToken());
		
		if (o.getStatusCode().equals("0001")) {
			return o.getToken();
		} else {
			throw new BriHostToHostException(o.getStatusCode(), o.getMessage());
		}
	}
	
	public String inquiryAccount(String token, String bankCode, String bankAcc) throws Exception {
		InquiryAccount_CT_Result o = stub.inquiryAccount(new InquiryAccount_CT(username, password, agentCode, ipAddress, token, bankCode, bankAcc));
		if (o.getStatusCode().equals("0001")) {
			return o.getAccountName();
		} else {
			throw new BriHostToHostException(o.getStatusCode(), o.getMessage());
		}
	}
	
	public String paymentAccount(String token, String paymentInfo, String bankAcc, String recipientName, String beneficiaryPhoneNo, String beneficiaryId, String senderName, String senderAddress, String arcNo, String bankCode, long amount) throws Exception {
		PaymentAccount_CT_Result o = stub.paymentAccount(new PaymentAccount_CT(username, password, agentCode, ipAddress, token, paymentInfo, bankAcc, recipientName, C.INDONESIA, beneficiaryPhoneNo, C.emptyString, beneficiaryId, "4", "ID", senderName, senderAddress, C.emptyString, C.emptyString, arcNo, "4", "ID", "IDR", "IDR", bankCode, String.valueOf(amount), "", "TW"));
		if (o.getStatusCode().equals("0001")) {
			return o.getTicketNumber();
		} else {
			throw new BriHostToHostException(o.getStatusCode(), o.getMessage());
		}
	}
	
	public VostroInfo inquiryVostro(String token) throws Exception {
		InquiryVostro_CT_Result o = stub.inquiryVostro(new InquiryVostro_CT(username, password, agentCode, ipAddress, token, "IDR"));
		if (o.getStatusCode().equals("0001")) {
			VostroInfo i = new VostroInfo();
			i.accountName = o.getAccountName();
			i.balance = o.getBalance();
			i.currency = o.getCurrency();
			return i;
		} else {
			throw new BriHostToHostException(o.getStatusCode(), o.getMessage());
		}
	}
	
	public String inquiryTransaction(String token, String paymentInfo) throws Exception {
		InquiryTransaction_CT_Result o = stub.inquiryTransaction(new InquiryTransaction_CT(username, password, agentCode, ipAddress, token, paymentInfo));
		if (o.getStatusCode().equals("0001")) {
			return o.getMessage();
		} else {
			throw new BriHostToHostException(o.getStatusCode(), o.getMessage());
		}
	}
}
