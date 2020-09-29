package com.indogo.test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
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

public class BniTest {

	public static void main(String[] args) {
		try {
			KeyStore keyStore = KeyStore.getInstance("jks");
			FileInputStream stream = new FileInputStream("c:\\work\\indosuara_dev.jks");
			keyStore.load(stream, "password".toCharArray());
			
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] certs, String arg1) throws CertificateException {
					System.out.println("isTrusted: " + certs[0].getPublicKey() + ":" + arg1);
					return true;
				}
			}).build();
			
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			
			String url = "https://remitapi.bni.co.id:55001/remittance/incoming/indosuara";
			
			Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).build();
			HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);
			
			CloseableHttpClient httpClient = HttpClients.custom()
			        .setConnectionManager(cm)
			        .build();
			try {
				
				String signatureString = "INDOSUARA009113183203";
				Key privateKey = keyStore.getKey("indosuara_bni_h2h_dev", "password".toCharArray());
				Signature instance = Signature.getInstance("SHA1withRSA");
				instance.initSign((PrivateKey)privateKey);
				instance.update(signatureString.getBytes());
				byte[] signature = instance.sign();

				StringBuilder sb = new StringBuilder();
				sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">")
				.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">")
				.append("<accountInfoInquiry xmlns=\"http://service.bni.co.id/remm\">")
				.append("<header xmlns=\"\">")
				.append("<clientId>INDOSUARA</clientId>")
				.append("<signature>").append(Base64.encodeBase64String(signature, false)).append("</signature>")
				.append("</header>")
				.append("<bankCode xmlns=\"\">009</bankCode>")
				.append("<accountNum xmlns=\"\">113183203</accountNum>")
				.append("</accountInfoInquiry>")
				.append("</s:Body>")
				.append("</s:Envelope>");
				
				StringEntity stringEntity = new StringEntity(sb.toString());
				
				HttpPost httpPost = new HttpPost(url);
				httpPost.addHeader("Content-Type", "text/xml;charset=utf-8");
				httpPost.addHeader("SOAPAction", "");
				httpPost.setEntity(stringEntity);
				CloseableHttpResponse response = httpClient.execute(httpPost);
				try {
					HttpEntity entity = response.getEntity();
					System.out.println("status_line = " + response.getStatusLine());
					InputStream inputStream = entity.getContent();
					System.out.println(IOUtils.toString(inputStream, "UTF-8"));
					EntityUtils.consume(entity);
				} finally {
					response.close();
				}
			} finally {
				httpClient.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
