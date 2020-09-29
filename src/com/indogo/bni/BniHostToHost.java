package com.indogo.bni;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import com.lionpig.webui.database.IConnection;
import com.lionpig.webui.http.struct.FunctionItem;
import com.lionpig.webui.http.util.C;
import com.lionpig.webui.http.util.Helper;

public class BniHostToHost implements AutoCloseable {
	private static Log LOG = LogFactory.getLog(BniHostToHost.class);
	private final static String MASTER_KEY = "YFEul6kg3Rdbv/B4FPTDcw==";
	
	private IConnection conn;
	private KeyStore keyStore;
	private char[] password;
	private String clientId;
	private String privateKeyAliasId;
	private String url;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private CloseableHttpClient httpClient;
	
	public BniHostToHost(IConnection conn) throws Exception {
		this.conn = conn;
		String defaultKeyStoreFilename = conn.getGlobalConfig(C.bni_h2h, C.default_key_store_filename);
		
		PreparedStatement pstmt = conn.getConnection().prepareStatement("select pwd from bni_key_store where file_name = ?");
		try {
			pstmt.setString(1, defaultKeyStoreFilename);
			ResultSet r = pstmt.executeQuery();
			try {
				if (r.next()) {
					String encryptedPassword = r.getString(1);
					SecretKeySpec skeySpec = new SecretKeySpec(Base64.decodeBase64(MASTER_KEY), "AES");
					Cipher cipher = Cipher.getInstance("AES");
					cipher.init(Cipher.DECRYPT_MODE, skeySpec);
					byte[] original = cipher.doFinal(Base64.decodeBase64(encryptedPassword));
					password = new String(original).toCharArray();
				} else {
					throw new Exception("key store " + defaultKeyStoreFilename + " not found");
				}
			} finally {
				r.close();
			}
		} finally {
			pstmt.close();
		}
		
		init(defaultKeyStoreFilename);
	}
	
	public BniHostToHost(IConnection conn, char[] password) throws Exception {
		this.conn = conn;
		this.password = password;
		String defaultKeyStoreFilename = conn.getGlobalConfig(C.bni_h2h, C.default_key_store_filename);
		init(defaultKeyStoreFilename);
	}
	
	private void init(String filename) throws Exception {
		String keyStoreFileLocation = new File(conn.getGlobalConfig(C.bni_h2h, C.jks_directory), filename).getAbsolutePath();
		
		keyStore = KeyStore.getInstance(C.jks);
		FileInputStream stream = new FileInputStream(keyStoreFileLocation);
		try {
			keyStore.load(stream, password);
		} finally {
			stream.close();
		}
		
		clientId = conn.getGlobalConfig(C.bni_h2h, C.client_id);
		privateKeyAliasId = conn.getGlobalConfig(C.bni_h2h, C.private_key_alias_id);
		url = conn.getGlobalConfig(C.bni_h2h, C.url);
		url = org.apache.commons.lang3.StringUtils.replace(url, "{port}", conn.getGlobalConfig(C.bni_h2h, C.port));
		
		int httpConnectionTimeout = Integer.parseInt(conn.getGlobalConfig(C.bni_h2h, C.http_connection_timeout));
		int httpSoTimeout = Integer.parseInt(conn.getGlobalConfig(C.bni_h2h, C.http_so_timeout));
		int httpRequestTimeout = Integer.parseInt(conn.getGlobalConfig(C.bni_h2h, C.http_request_timeout));
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(httpSoTimeout)
				.setConnectTimeout(httpConnectionTimeout)
				.setConnectionRequestTimeout(httpRequestTimeout)
				.build();
		
		SocketConfig socketConfig = SocketConfig.custom()
				.setSoTimeout(httpSoTimeout)
				.build();
		
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] certs, String arg1) throws CertificateException {
				return true;
			}
		}).build();
		
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
		
		Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create().register(C.https, sslsf).build();
		HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);
		httpClient = HttpClients.custom()
		        .setConnectionManager(cm)
		        .setDefaultRequestConfig(requestConfig)
		        .setDefaultSocketConfig(socketConfig)
		        .build();
	}
	
	private String sign(String data) throws Exception {
		Key privateKey = keyStore.getKey(privateKeyAliasId, password);
		Signature instance = Signature.getInstance("SHA1withRSA");
		instance.initSign((PrivateKey)privateKey);
		instance.update(data.getBytes());
		byte[] signature = instance.sign();
		return StringUtils.newStringUtf8(Base64.encodeBase64(signature, false));
	}
	
	private String sendRequest(String body) throws Exception {
		StringEntity stringEntity = new StringEntity(body);
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("Content-Type", "text/xml;charset=utf-8");
		httpPost.addHeader("SOAPAction", "");
		httpPost.setEntity(stringEntity);
		CloseableHttpResponse response = httpClient.execute(httpPost);
		try {
			HttpEntity entity = response.getEntity();
			try {
				InputStream inputStream = entity.getContent();
				String replyString = IOUtils.toString(inputStream, "UTF-8");
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					int idxStart = replyString.indexOf("<errorCode>");
					if (idxStart >= 0) {
						int idxEnd = replyString.indexOf("</errorCode>", idxStart);
						if (idxEnd >= 0) {
							String errorCode = replyString.substring(idxStart + 11, idxEnd);
							
							String errorDesc = C.emptyString;
							idxStart = replyString.indexOf("<errorDescription>");
							if (idxStart >= 0) {
								idxEnd = replyString.indexOf("</errorDescription>", idxStart);
								if (idxEnd >= 0) {
									errorDesc = replyString.substring(idxStart + 18, idxEnd);
								}
							}

							String uuid = UUID.randomUUID().toString().replaceAll("-", "");
							LOG.error(uuid + " bni_send  = " + body);
							LOG.error(uuid + " bni_reply = " + replyString);
							throw new ClientProtocolException("[" + errorCode + "] " + errorDesc);
						}
					} else {
						String errorDesc = C.emptyString;
						idxStart = replyString.indexOf("<errorDescription>");
						if (idxStart >= 0) {
							int idxEnd = replyString.indexOf("</errorDescription>", idxStart);
							if (idxEnd >= 0) {
								errorDesc = replyString.substring(idxStart + 18, idxEnd);

								String uuid = UUID.randomUUID().toString().replaceAll("-", "");
								LOG.error(uuid + " bni_send = " + body);
								LOG.error(uuid + " bni_reply = " + replyString);
								throw new ClientProtocolException(errorDesc);
							}
						}
					}
					
					return replyString;
				} else {
					int idxStart = replyString.indexOf("<errorCode>");
					if (idxStart >= 0) {
						int idxEnd = replyString.indexOf("</errorCode>", idxStart);
						if (idxEnd >= 0) {
							String errorCode = replyString.substring(idxStart + 11, idxEnd);
							
							String errorDesc = C.emptyString;
							idxStart = replyString.indexOf("<errorDescription>");
							if (idxStart >= 0) {
								idxEnd = replyString.indexOf("</errorDescription>", idxStart);
								if (idxEnd >= 0) {
									errorDesc = replyString.substring(idxStart + 18, idxEnd);
								}
							}

							String uuid = UUID.randomUUID().toString().replaceAll("-", "");
							LOG.error(uuid + " bni_send  = " + body);
							LOG.error(uuid + " bni_reply = " + replyString);
							throw new ClientProtocolException("[" + errorCode + "] " + errorDesc);
						}
					} else {
						String errorDesc = C.emptyString;
						idxStart = replyString.indexOf("<errorDescription>");
						if (idxStart >= 0) {
							int idxEnd = replyString.indexOf("</errorDescription>", idxStart);
							if (idxEnd >= 0) {
								errorDesc = replyString.substring(idxStart + 18, idxEnd);

								String uuid = UUID.randomUUID().toString().replaceAll("-", "");
								LOG.error(uuid + " bni_send = " + body);
								LOG.error(uuid + " bni_reply = " + replyString);
								throw new ClientProtocolException(errorDesc);
							}
						}
					}
					
					String uuid = UUID.randomUUID().toString().replaceAll("-", "");
					LOG.error(uuid + " bni_send  = " + body);
					LOG.error(uuid + " bni_reply = " + replyString);
					throw new ClientProtocolException(response.getStatusLine().toString());
				}
			} finally {
				EntityUtils.consume(entity);
			}
		} finally {
			response.close();
		}
	}
	
	private String getNodeValue(String xml, String fieldName) throws Exception {
		int idxStart;
		
		idxStart = xml.indexOf("<" + fieldName + "/>");
		if (idxStart >= 0) {
			return C.emptyString;
		}
		
		idxStart = xml.indexOf("<" + fieldName + ">") + fieldName.length() + 2;
		if (idxStart >= 0) {
			int idxEnd = xml.indexOf("</" + fieldName + ">", idxStart);
			if (idxEnd >= 0) {
				return xml.substring(idxStart, idxEnd);
			} else {
				throw new Exception(fieldName + " not found");
			}
		} else {
			throw new Exception(fieldName + " not found");
		}
	}
	
	public String getTrxDate() throws Exception {
		Timestamp currentTime = conn.getCurrentTime();
		String str = dateFormat.format(currentTime);
		return new StringBuilder().append(str).append("T00:00:00").toString();
	}
	
	public BniPaymentResult paymentCreditToAccount(String trxDate, String refNumber, int amount, String orderingName, String orderingAddress, String beneficiaryAccount, String beneficiaryName, String beneficiaryAddress, String beneficiaryPhoneNo) throws Exception {
		String signature = sign(clientId + refNumber + trxDate + amount + beneficiaryAccount);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">")
		.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">")
		.append("<processPO xmlns=\"http://service.bni.co.id/remm\">")
		.append("<header xmlns=\"\">")
		.append("<clientId>").append(clientId).append("</clientId>")
		.append("<signature>").append(signature).append("</signature>")
		.append("</header>")
		.append("<paymentOrder xmlns=\"\">")
		.append("<refNumber>").append(refNumber).append("</refNumber>")
		.append("<serviceType>BNI</serviceType>")
		.append("<trxDate>").append(trxDate).append("</trxDate>")
		.append("<currency>IDR</currency>")
		.append("<amount>").append(amount).append("</amount>")
		.append("<orderingName>").append(orderingName).append("</orderingName>")
		.append("<orderingAddress1>").append(orderingAddress).append("</orderingAddress1>")
		.append("<orderingAddress2/>")
		.append("<orderingPhoneNumber/>")
		.append("<beneficiaryAccount>").append(beneficiaryAccount).append("</beneficiaryAccount>")
		.append("<beneficiaryName>").append(beneficiaryName).append("</beneficiaryName>")
		.append("<beneficiaryAddress1>").append(beneficiaryAddress).append("</beneficiaryAddress1>")
		.append("<beneficiaryAddress2/>")
		.append("<beneficiaryPhoneNumber>").append(beneficiaryPhoneNo).append("</beneficiaryPhoneNumber>")
		.append("<acctWithInstcode>A</acctWithInstcode>")
		.append("<acctWithInstName>BNINIDJAXXX</acctWithInstName>")
		.append("<acctWithInstAddress1/>")
		.append("<acctWithInstAddress2/>")
		.append("<acctWithInstAddress3/>")
		.append("<detailPayment1/>")
		.append("<detailPayment2/>")
		.append("<detailCharges>OUR</detailCharges>")
		.append("</paymentOrder>")
		.append("</processPO>")
		.append("</s:Body>")
		.append("</s:Envelope>");
		
		String reply = sendRequest(sb.toString());
		
		BniPaymentResult result = new BniPaymentResult();
		result.message = getNodeValue(reply, "message");
		result.trxDate = trxDate;
		return result;
	}
	
	public BniPaymentResult paymentCashPickup(String trxDate, String refNumber, int amount, String orderingName, String orderingAddress, String beneficiaryName, String beneficiaryAddress, String beneficiaryPhoneNo, String beneficiaryAddress2) throws Exception {
		String signature = sign(clientId + refNumber + trxDate + amount);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">")
		.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">")
		.append("<processPO xmlns=\"http://service.bni.co.id/remm\">")
		.append("<header xmlns=\"\">")
		.append("<clientId>").append(clientId).append("</clientId>")
		.append("<signature>").append(signature).append("</signature>")
		.append("</header>")
		.append("<paymentOrder xmlns=\"\">")
		.append("<refNumber>").append(refNumber).append("</refNumber>")
		.append("<serviceType>BNI</serviceType>")
		.append("<trxDate>").append(trxDate).append("</trxDate>")
		.append("<currency>IDR</currency>")
		.append("<amount>").append(amount).append("</amount>")
		.append("<orderingName>").append(orderingName).append("</orderingName>")
		.append("<orderingAddress1>").append(Helper.isNullOrEmpty(orderingAddress) ? C.TAIWAN : orderingAddress).append("</orderingAddress1>")
		.append("<orderingAddress2/>")
		.append("<orderingPhoneNumber/>")
		.append("<beneficiaryAccount></beneficiaryAccount>")
		.append("<beneficiaryName>").append(beneficiaryName).append("</beneficiaryName>")
		.append("<beneficiaryAddress1>").append(Helper.isNullOrEmpty(beneficiaryAddress) ? C.INDONESIA : beneficiaryAddress).append("</beneficiaryAddress1>")
		.append("<beneficiaryAddress2>").append(beneficiaryAddress2).append("</beneficiaryAddress2>")
		.append("<beneficiaryPhoneNumber>").append(beneficiaryPhoneNo).append("</beneficiaryPhoneNumber>")
		.append("<acctWithInstcode>A</acctWithInstcode>")
		.append("<acctWithInstName>BNINIDJAXXX</acctWithInstName>")
		.append("<acctWithInstAddress1/>")
		.append("<acctWithInstAddress2/>")
		.append("<acctWithInstAddress3/>")
		.append("<detailPayment1/>")
		.append("<detailPayment2/>")
		.append("<detailCharges>OUR</detailCharges>")
		.append("</paymentOrder>")
		.append("</processPO>")
		.append("</s:Body>")
		.append("</s:Envelope>");
		
		String reply = sendRequest(sb.toString());
		
		BniPaymentResult r = new BniPaymentResult();
		r.message = getNodeValue(reply, "message");
		r.trxDate = trxDate;
		return r;
	}
	
	public BniPaymentResult paymentInterbank(String trxDate, String refNumber, int amount, String orderingName, String orderingAddress, String beneficiaryAccount, String beneficiaryName, String beneficiaryAddress, String beneficiaryPhoneNo, String acctWithInstName) throws Exception {
		String signature = sign(clientId + refNumber + trxDate + amount + beneficiaryAccount);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">")
		.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">")
		.append("<processPO xmlns=\"http://service.bni.co.id/remm\">")
		.append("<header xmlns=\"\">")
		.append("<clientId>").append(clientId).append("</clientId>")
		.append("<signature>").append(signature).append("</signature>")
		.append("</header>")
		.append("<paymentOrder xmlns=\"\">")
		.append("<refNumber>").append(refNumber).append("</refNumber>")
		.append("<serviceType>INTERBANK</serviceType>")
		.append("<trxDate>").append(trxDate).append("</trxDate>")
		.append("<currency>IDR</currency>")
		.append("<amount>").append(amount).append("</amount>")
		.append("<orderingName>").append(orderingName).append("</orderingName>")
		.append("<orderingAddress1>").append(Helper.isNullOrEmpty(orderingAddress) ? C.TAIWAN : orderingAddress).append("</orderingAddress1>")
		.append("<orderingAddress2/>")
		.append("<orderingPhoneNumber/>")
		.append("<beneficiaryAccount>").append(beneficiaryAccount).append("</beneficiaryAccount>")
		.append("<beneficiaryName>").append(beneficiaryName).append("</beneficiaryName>")
		.append("<beneficiaryAddress1>").append(Helper.isNullOrEmpty(beneficiaryAddress) ? C.INDONESIA : beneficiaryAddress).append("</beneficiaryAddress1>")
		.append("<beneficiaryAddress2/>")
		.append("<beneficiaryPhoneNumber>").append(beneficiaryPhoneNo).append("</beneficiaryPhoneNumber>")
		.append("<acctWithInstcode>A</acctWithInstcode>")
		.append("<acctWithInstName>").append(acctWithInstName).append("</acctWithInstName>")
		.append("<acctWithInstAddress1/>")
		.append("<acctWithInstAddress2/>")
		.append("<acctWithInstAddress3/>")
		.append("<detailPayment1/>")
		.append("<detailPayment2/>")
		.append("<detailCharges>OUR</detailCharges>")
		.append("</paymentOrder>")
		.append("</processPO>")
		.append("</s:Body>")
		.append("</s:Envelope>");
		
		String reply = sendRequest(sb.toString());
		
		BniPaymentResult r = new BniPaymentResult();
		r.message = getNodeValue(reply, "message");
		r.trxDate = trxDate;
		return r;
	}
	
	public BniPaymentResult paymentClearing(String trxDate, String refNumber, int amount, String orderingName, String orderingAddress, String beneficiaryAccount, String beneficiaryName, String beneficiaryAddress, String beneficiaryPhoneNo, String acctWithInstName) throws Exception {
		String signature = sign(clientId + refNumber + trxDate + amount + beneficiaryAccount);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">")
		.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">")
		.append("<processPO xmlns=\"http://service.bni.co.id/remm\">")
		.append("<header xmlns=\"\">")
		.append("<clientId>").append(clientId).append("</clientId>")
		.append("<signature>").append(signature).append("</signature>")
		.append("</header>")
		.append("<paymentOrder xmlns=\"\">")
		.append("<refNumber>").append(refNumber).append("</refNumber>")
		.append("<serviceType>CLR</serviceType>")
		.append("<trxDate>").append(trxDate).append("</trxDate>")
		.append("<currency>IDR</currency>")
		.append("<amount>").append(amount).append("</amount>")
		.append("<orderingName>").append(orderingName).append("</orderingName>")
		.append("<orderingAddress1>").append(Helper.isNullOrEmpty(orderingAddress) ? C.TAIWAN : orderingAddress).append("</orderingAddress1>")
		.append("<orderingAddress2/>")
		.append("<orderingPhoneNumber/>")
		.append("<beneficiaryAccount>").append(beneficiaryAccount).append("</beneficiaryAccount>")
		.append("<beneficiaryName>").append(beneficiaryName).append("</beneficiaryName>")
		.append("<beneficiaryAddress1>").append(Helper.isNullOrEmpty(beneficiaryAddress) ? C.INDONESIA : beneficiaryAddress).append("</beneficiaryAddress1>")
		.append("<beneficiaryAddress2/>")
		.append("<beneficiaryPhoneNumber>").append(beneficiaryPhoneNo).append("</beneficiaryPhoneNumber>")
		.append("<acctWithInstcode>A</acctWithInstcode>")
		.append("<acctWithInstName>").append(acctWithInstName).append("</acctWithInstName>")
		.append("<acctWithInstAddress1/>")
		.append("<acctWithInstAddress2/>")
		.append("<acctWithInstAddress3/>")
		.append("<detailPayment1/>")
		.append("<detailPayment2/>")
		.append("<detailCharges>OUR</detailCharges>")
		.append("</paymentOrder>")
		.append("</processPO>")
		.append("</s:Body>")
		.append("</s:Envelope>");
		
		String reply = sendRequest(sb.toString());
		
		BniPaymentResult r = new BniPaymentResult();
		r.message = getNodeValue(reply, "message");
		r.trxDate = trxDate;
		return r;
	}
	
	public BniPaymentResult paymentRTGS(String trxDate, String refNumber, int amount, String orderingName, String orderingAddress, String beneficiaryAccount, String beneficiaryName, String beneficiaryAddress, String beneficiaryPhoneNo, String acctWithInstName) throws Exception {
		String signature = sign(clientId + refNumber + trxDate + amount + beneficiaryAccount);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">")
		.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">")
		.append("<processPO xmlns=\"http://service.bni.co.id/remm\">")
		.append("<header xmlns=\"\">")
		.append("<clientId>").append(clientId).append("</clientId>")
		.append("<signature>").append(signature).append("</signature>")
		.append("</header>")
		.append("<paymentOrder xmlns=\"\">")
		.append("<refNumber>").append(refNumber).append("</refNumber>")
		.append("<serviceType>RTGS</serviceType>")
		.append("<trxDate>").append(trxDate).append("</trxDate>")
		.append("<currency>IDR</currency>")
		.append("<amount>").append(amount).append("</amount>")
		.append("<orderingName>").append(orderingName).append("</orderingName>")
		.append("<orderingAddress1>").append(Helper.isNullOrEmpty(orderingAddress) ? C.TAIWAN : orderingAddress).append("</orderingAddress1>")
		.append("<orderingAddress2/>")
		.append("<orderingPhoneNumber/>")
		.append("<beneficiaryAccount>").append(beneficiaryAccount).append("</beneficiaryAccount>")
		.append("<beneficiaryName>").append(beneficiaryName).append("</beneficiaryName>")
		.append("<beneficiaryAddress1>").append(Helper.isNullOrEmpty(beneficiaryAddress) ? C.INDONESIA : beneficiaryAddress).append("</beneficiaryAddress1>")
		.append("<beneficiaryAddress2/>")
		.append("<beneficiaryPhoneNumber>").append(beneficiaryPhoneNo).append("</beneficiaryPhoneNumber>")
		.append("<acctWithInstcode>A</acctWithInstcode>")
		.append("<acctWithInstName>").append(acctWithInstName).append("</acctWithInstName>")
		.append("<acctWithInstAddress1/>")
		.append("<acctWithInstAddress2/>")
		.append("<acctWithInstAddress3/>")
		.append("<detailPayment1/>")
		.append("<detailPayment2/>")
		.append("<detailCharges>OUR</detailCharges>")
		.append("</paymentOrder>")
		.append("</processPO>")
		.append("</s:Body>")
		.append("</s:Envelope>");
		
		String reply = sendRequest(sb.toString());
		
		BniPaymentResult r = new BniPaymentResult();
		r.message = getNodeValue(reply, "message");
		r.trxDate = trxDate;
		return r;
	}
	
	public VostroInfo vostroInquery() throws Exception {
		String signature = sign(clientId + "IDR");
		
		StringBuilder sb = new StringBuilder();
		sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">")
		.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">")
		.append("<vostroInquiry xmlns=\"http://service.bni.co.id/remm\">")
		.append("<header xmlns=\"\">")
		.append("<clientId>").append(clientId).append("</clientId>")
		.append("<signature>").append(signature).append("</signature>")
		.append("</header>")
		.append("<currency xmlns=\"\">IDR</currency>")
		.append("</vostroInquiry>")
		.append("</s:Body></s:Envelope>");
		
		String reply = sendRequest(sb.toString());
		
		VostroInfo v = new VostroInfo();
		v.accountName = getNodeValue(reply, C.accountName);
		v.accountNumber = getNodeValue(reply, C.accountNumber);
		v.balance = getNodeValue(reply, C.effectiveBalance);
		v.openingDate = getNodeValue(reply, C.openDate);
		v.accountStatus = getNodeValue(reply, C.accountStatus);
		return v;
	}
	
	public PaymentInfo paymentOrderInquiry(String refNumber, String trxDate) throws Exception {
		String signature = sign(clientId + refNumber);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<s:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:dpm=\"http://www.datapower.com/schemas/management\" xmlns:dpfunc=\"http://www.datapower.com/extensions/functions\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">")
		.append("<s:Body xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
		.append("<poInfoInquiry>")
		.append("<header>")
		.append("<clientId>").append(clientId).append("</clientId>")
		.append("<signature>").append(signature).append("</signature>")
		.append("</header>")
		.append("<paymentOrderKey>")
		.append("<refNumber>").append(refNumber).append("</refNumber>")
		.append("<clientId>").append(clientId).append("</clientId>")
		.append("<trxDate>").append(trxDate == null ? getTrxDate() : trxDate).append("</trxDate>")
		.append("</paymentOrderKey>")
		.append("</poInfoInquiry>")
		.append("</s:Body></s:Envelope>");
		
		String xml = sendRequest(sb.toString());
		
		PaymentDetail paymentDetail = new PaymentDetail();
		paymentDetail.bniReference = getNodeValue(xml, C.bniReference);
		paymentDetail.paidDate = getNodeValue(xml, C.paidDate);
		paymentDetail.paidCurrency = getNodeValue(xml, C.paidCurrency);
		paymentDetail.paidAmount = getNodeValue(xml, C.paidAmount);
		paymentDetail.chargesAmount = getNodeValue(xml, C.chargesAmount);
		paymentDetail.beneficiaryAccount = getNodeValue(xml, C.beneficiaryAccount);
		paymentDetail.beneficiaryName = getNodeValue(xml, C.beneficiaryName);
		
		PaymentInfo paymentInfo = new PaymentInfo();
		paymentInfo.status = getNodeValue(xml, C.status);
		paymentInfo.statusDescription = getNodeValue(xml, C.statusDescription);
		paymentInfo.paymentDetail = paymentDetail;
		
		return paymentInfo;
	}
	
	public static Object[] createCertificate(FunctionItem fi, String password, int validity) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(Base64.decodeBase64(MASTER_KEY), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] b = cipher.doFinal(password.getBytes());
		String encryptedPassword = Base64.encodeBase64URLSafeString(b);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date createDate = new Date(calendar.getTimeInMillis());
		
		calendar.add(Calendar.DATE, validity);
		Date expireDate = new Date(calendar.getTimeInMillis());
		
		String filename = UUID.randomUUID().toString().replaceAll("-", "");
		
		Connection conn = fi.getConnection().getConnection();
		PreparedStatement pstmt = conn.prepareStatement("insert into bni_key_store (create_date, file_name, expire_date, pwd) values (?, ?, ?, ?)");
		try {
			pstmt.setDate(1, createDate);
			pstmt.setString(2, filename);
			pstmt.setDate(3, expireDate);
			pstmt.setString(4, encryptedPassword);
			pstmt.executeUpdate();
			
			String jksPath = new File(fi.getConnection().getGlobalConfig(C.bni_h2h, C.jks_directory), filename).getAbsolutePath();
			String keytool = fi.getConnection().getGlobalConfig(C.bni_h2h, C.keytool_location);
			String aliasId = fi.getConnection().getGlobalConfig(C.bni_h2h, C.private_key_alias_id);
			String[] cmds = new String[] {
					keytool,
					"-genkey",
					"-keyalg",
					"RSA",
					"-alias",
					aliasId,
					"-keystore",
					jksPath,
					"-storepass",
					password,
					"-keypass",
					password,
					"-validity",
					String.valueOf(validity),
					"-keysize",
					"1024",
					"-sigalg",
					"SHA1withRSA",
					"-dname",
					"CN=prod.indogo.tw, OU=indogo, O=indogo, L=indogo, S=indogo, C=TW"
			};
			Process process = Runtime.getRuntime().exec(cmds);
			int exitCode = process.waitFor();
			
			if (exitCode == 0) {
				conn.commit();
				return new Object[] {new Timestamp(createDate.getTime()), new Timestamp(expireDate.getTime()), filename};
			} else {
				String errorMessage;
				try {
					errorMessage = IOUtils.toString(process.getInputStream(), "UTF-8");
				} catch (Exception ignore) {
					throw new Exception("keytool return exit code " + exitCode);
				}
				throw new Exception(errorMessage);
			}
		} catch (Exception e) {
			conn.rollback();
			throw e;
		} finally {
			pstmt.close();
		}
	}
	
	public String getAccountName(String bankCode, String account) throws Exception {
		String signature = sign(clientId + bankCode + account);
		StringBuilder sb = new StringBuilder();
		sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">")
		.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">")
		.append("<accountInfoInquiry xmlns=\"http://service.bni.co.id/remm\">")
		.append("<header xmlns=\"\">")
		.append("<clientId>").append(clientId).append("</clientId>")
		.append("<signature>").append(signature).append("</signature>")
		.append("</header>")
		.append("<bankCode xmlns=\"\">").append(bankCode).append("</bankCode>")
		.append("<accountNum xmlns=\"\">").append(account).append("</accountNum>")
		.append("</accountInfoInquiry>")
		.append("</s:Body>")
		.append("</s:Envelope>");

		String reply = sendRequest(sb.toString());
		return getNodeValue(reply, "accountName");
	}
	
	public static void exportCertificate(FunctionItem fi, String password, String filename, String outputFilename) throws Exception {
		String jksPath = new File(fi.getConnection().getGlobalConfig(C.bni_h2h, C.jks_directory), filename).getAbsolutePath();
		String keytool = fi.getConnection().getGlobalConfig(C.bni_h2h, C.keytool_location);
		String aliasId = fi.getConnection().getGlobalConfig(C.bni_h2h, C.private_key_alias_id);
		String[] cmds = new String[] {
				keytool,
				"-export",
				"-alias",
				aliasId,
				"-keystore",
				jksPath,
				"-rfc",
				"-file",
				outputFilename,
				"-storepass",
				password,
				"-keypass",
				password
		};
		Process process = Runtime.getRuntime().exec(cmds);
		int exitCode = process.waitFor();
		if (exitCode != 0) {
			String errorMessage;
			try {
				errorMessage = IOUtils.toString(process.getInputStream(), "UTF-8");
			} catch (Exception ignore) {
				throw new Exception("keytool return exit code " + exitCode);
			}
			throw new Exception(errorMessage);
		}
	}

	@Override
	public void close() throws Exception {
		httpClient.close();
	}
}
