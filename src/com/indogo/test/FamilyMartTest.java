package com.indogo.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class FamilyMartTest {

	public static void main(String[] args) {
		closeTransaction();
	}
	
	public static void closeTransaction() {
		try {
			String url = "http://localhost:8080/eclipse-php-neon-1-win32-x86_64-workplace/indogo_php/ectest/receive";
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version='1.0' encoding='UTF-8'?>")
			.append("<OLTP>")
				.append("<HEADER>")
					.append("<VER>05.05</VER>")
					.append("<FROM>99027</FROM>")
					.append("<TERMINO>009913010000010147</TERMINO>")
					.append("<TO>27294437</TO>")
					.append("<BUSINESS>B000001</BUSINESS>")
					.append("<DATE>20170106</DATE>")
					.append("<TIME>200048</TIME>")
					.append("<STATCODE>0000</STATCODE>")
					.append("<STATDESC></STATDESC>")
				.append("</HEADER>")
				.append("<AP>")
					.append("<OL_OI_NO>KK1</OL_OI_NO>")
					.append("<ORDER_NO>20170106000001</ORDER_NO>")
					.append("<ACCOUNT>02150</ACCOUNT>")
					.append("<PIN_CODE>2040A7265G8594</PIN_CODE>")
					.append("<OL_Code_1>060106KK1</OL_Code_1>")
					.append("<OL_Code_2>002040A7265G8594</OL_Code_2>")
					.append("<OL_Code_3>195900000002150</OL_Code_3>")
					.append("<STORE_DESC>測試店</STORE_DESC>")
					.append("<STATUS>S</STATUS>")
					.append("<DESC></DESC>")
				.append("</AP>")
			.append("</OLTP>");

			CloseableHttpClient httpClient = HttpClients.createDefault();
			try {
				List<NameValuePair> parameters = new ArrayList<>();
				parameters.add(new BasicNameValuePair("d", sb.toString()));
				UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, "UTF-8");
				
				HttpPost httpPost = new HttpPost(url);
				httpPost.setEntity(formEntity);
				CloseableHttpResponse response = httpClient.execute(httpPost);
				try {
					HttpEntity entity = response.getEntity();
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
	
	public static void printReceipt() {
		try {
			String url = "http://test.familynet.com.tw/familyec/barcode_guide2.aspx";
			String familyAccount = "indogotest";
			String txnId = "20170106000001";
			String paymentInfo = "2040A7265G8594";
			CloseableHttpClient httpClient = HttpClients.createDefault();
			try {
				List<NameValuePair> parameters = new ArrayList<>();
				parameters.add(new BasicNameValuePair("VD_ACCOUNT", familyAccount));
				parameters.add(new BasicNameValuePair("VD_ORDERNO", txnId));
				parameters.add(new BasicNameValuePair("VD_PINCODE", paymentInfo));
				UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, "UTF-8");
				
				HttpPost httpPost = new HttpPost(url);
				httpPost.setEntity(formEntity);
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
	
	public static void createTransaction() {
		try {
			String url = "http://test.familynet.com.tw/pin/webec.asmx";
			
			String m_taxID = "27294437";       //您的統編
			String m_termino = "KK1ZKK1";     //廠商代碼+代收代號(共7碼)
			String m_date = "20161217";        //日期 YYYYMMDD
			String m_time = "100430";      //時間 HHMMSS
			String m_orderNo = "20161217000001";     //您的訂單編號
			String m_amount = "5000";      //金額
			String m_pinCode = "";    //PIN Code
			String m_endDate = "20161218";     //繳款截止日期 YYYYMMDD
			String m_endTime = "235959";     //繳款截止時間 HHMMSS
			String m_payType = "cash";     //繳款類別
			String m_prdDesc = "remit";    //商品簡述
			String m_payCompany = "IndoGO"; //付款廠商
			String m_tradeType = "1";     //1:要號, 3:二次列印
			String m_desc1 = "yosua baru";      //備註1
			String m_desc2 = "Bank Code: 009";      //備註2
			String m_desc3 = "Bank Name: BNI";      //備註3
			String m_desc4 = "Acc: baru";      //備註4
			String m_accountNo = "indogotest";  //您的帳號
			String m_password = "2qd3rf4g5j";   //您的密碼

			StringBuilder sb = new StringBuilder(100);
			sb.append("<?xml version='1.0' encoding='UTF-8'?>");
			sb.append("<soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>");
				sb.append("<soap:Body>");
					sb.append("<NewOrder xmlns='http://tempuri.org/'>");
						sb.append("<TX_WEB>");
							sb.append("<HEADER>");
								sb.append("<XML_VER>05.01</XML_VER>");
								sb.append("<XML_FROM>").append(m_taxID).append("</XML_FROM>");
								sb.append("<TERMINO>").append(m_termino).append("</TERMINO>");
								sb.append("<XML_TO>99027</XML_TO>");
								sb.append("<BUSINESS>B000001</BUSINESS>");
								sb.append("<XML_DATE>").append(m_date).append("</XML_DATE>");
								sb.append("<XML_TIME>").append(m_time).append("</XML_TIME>");
								sb.append("<STATCODE>0000</STATCODE>");
								sb.append("<STATDESC></STATDESC>");
							sb.append("</HEADER>");
							sb.append("<AP>"); 
								sb.append("<ORDER_NO>").append(m_orderNo).append("</ORDER_NO>");
								sb.append("<ACCOUNT>").append(m_amount).append("</ACCOUNT>");
								sb.append("<PIN_CODE>").append(m_pinCode).append("</PIN_CODE>");
								sb.append("<END_DATE>").append(m_endDate).append("</END_DATE>");
								sb.append("<END_TIME>").append(m_endTime).append("</END_TIME>");
								sb.append("<PAY_TYPE>").append(m_payType).append("</PAY_TYPE>");
								sb.append("<PRD_DESC>").append(m_prdDesc).append("</PRD_DESC>");
								sb.append("<PAY_COMP>").append(m_payCompany).append("</PAY_COMP>");
								sb.append("<TRADE_TYPE>").append(m_tradeType).append("</TRADE_TYPE>");
								sb.append("<DESC1>").append(m_desc1).append("</DESC1>");
								sb.append("<DESC2>").append(m_desc2).append("</DESC2>");
								sb.append("<DESC3>").append(m_desc3).append("</DESC3>");
								sb.append("<DESC4>").append(m_desc4).append("</DESC4>");
								sb.append("<STATUS>S</STATUS>");
								sb.append("<DESC></DESC>");
							sb.append("</AP>");
						sb.append("</TX_WEB>");
						sb.append("<ACCOUNT_NO>").append(m_accountNo).append("</ACCOUNT_NO>");
						sb.append("<PASSWORD>").append(m_password).append("</PASSWORD>");
					sb.append("</NewOrder>");
				sb.append("</soap:Body>");
			sb.append("</soap:Envelope>");

			CloseableHttpClient httpClient = HttpClients.createDefault();
			try {
				StringEntity stringEntity = new StringEntity(sb.toString());
				
				HttpPost httpPost = new HttpPost(url);
				httpPost.addHeader("Content-Type", "text/xml;charset=utf-8");
				httpPost.addHeader("SOAPAction", "http://tempuri.org/NewOrder");
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
