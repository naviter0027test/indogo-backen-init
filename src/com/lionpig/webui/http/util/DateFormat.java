package com.lionpig.webui.http.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormat {
	private SimpleDateFormat format;
	private SimpleDateFormat formatShort;
	private SimpleDateFormat format2;
	private SimpleDateFormat formatShort2;
	
	private DateFormat() {
		format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		formatShort = new SimpleDateFormat("yyyyMMdd");
		format2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		formatShort2 = new SimpleDateFormat("yyyy/MM/dd");
	}
	
	private static DateFormat instance = new DateFormat();
	public static DateFormat getInstance() {
		return instance;
	}
	
	public Date parse(String source) throws ParseException {
		try {
			return format.parse(source);
		}
		catch (Exception ignore) {}
		try {
			return format2.parse(source);
		}
		catch (Exception ignore) {}
		try {
			return formatShort.parse(source);
		}
		catch (Exception ignore) {}
		return formatShort2.parse(source);
	}
	
	public String format(Date date) {
		if (date == null)
			return "";
		return format2.format(date);
	}
	
	public String formatShort(Date date) {
		if (date == null)
			return "";
		return formatShort2.format(date);
	}
}
