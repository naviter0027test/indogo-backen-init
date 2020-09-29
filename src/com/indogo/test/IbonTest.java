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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class IbonTest {
	
	public static void main(String[] args) {
		try {
			query();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void close() throws Exception {
		String url = "http://localhost:8080/eclipse-php-neon-1-win32-x86_64-workplace/indogo_php/ibon/close";
		
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version='1.0' encoding='Big5'?>")
		.append("<PAYMONEY>")
		.append("<SENDTIME>0</SENDTIME>")
		.append("<STOREID>123456</STOREID>")
		.append("<SHOPID>01</SHOPID>")
		.append("<DETAIL_NUM>06020171000001</DETAIL_NUM>")
		.append("<STATUS_CODE>0000</STATUS_CODE>")
		.append("<STATUS_DESC>成功</STATUS_DESC>")
		.append("<BARCODE1>11</BARCODE1>")
		.append("<BARCODE2>12</BARCODE2>")
		.append("<BARCODE3>13</BARCODE3>")
		.append("<AMOUNT>1150</AMOUNT>")
		.append("<PAYDATE>20170119123030</PAYDATE>")
		.append("<USERDATA1>20170117000004</USERDATA1>")
		.append("<USERDATA2></USERDATA2>")
		.append("<USERDATA3></USERDATA3>")
		.append("<USERDATA4></USERDATA4>")
		.append("<USERDATA5></USERDATA5>")
		.append("</PAYMONEY>");
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			List<NameValuePair> parameters = new ArrayList<>();
			parameters.add(new BasicNameValuePair("XMLData", sb.toString()));
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, "Big5");
			
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
	}

	public static void query() throws Exception {
		String url = "http://localhost:8080/eclipse-php-neon-1-win32-x86_64-workplace/indogo_php/ibon/query";
		
		StringBuilder sb = new StringBuilder();
		sb.append("<SENDDATA>")
		.append("<BUSINESS>0700QC1</BUSINESS>")
		.append("<STOREID>123456</STOREID>")
		.append("<SHOPID>01</SHOPID>")
		.append("<DETAILED_NUM>06020171000001</DETAILED_NUM>")
		.append("<PRODUCT_CODE>1100121201</PRODUCT_CODE>")
		.append("<STATUS_CODE>0000</STATUS_CODE>")
		.append("<STATUS_DESC>成功</STATUS_DESC>")
		.append("<SUB1>11</SUB1>")
		.append("<SUB2>12</SUB2>")
		.append("<SUB3>13</SUB3>")
		.append("<KEY1>0QV80NQQHD7F6D</KEY1>")
		.append("<KEY2></KEY2>")
		.append("<KEY3></KEY3>")
		.append("<KEY4></KEY4>")
		.append("<KEY5></KEY5>")
		.append("</SENDDATA>");
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			List<NameValuePair> parameters = new ArrayList<>();
			parameters.add(new BasicNameValuePair("XMLData", sb.toString()));
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
	}
	
}
