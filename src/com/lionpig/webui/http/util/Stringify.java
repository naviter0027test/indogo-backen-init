package com.lionpig.webui.http.util;

import java.sql.Timestamp;
import java.util.Date;

public class Stringify {
	
	public static String concat(String...strings) {
		StringBuilder sb = new StringBuilder();
		for (String s : strings) {
			sb.append(s);
		}
		return sb.toString();
	}

	public static String getString(String s) {
		if (s == null)
			return C.emptyString;
		else
			return s;
	}
	
	public static String getDate(Timestamp t) {
		if (t == null)
			return C.emptyString;
		else
			return DateFormat.getInstance().formatShort(t);
	}
	
	public static String getDate(Date t) {
		if (t == null)
			return C.emptyString;
		else
			return DateFormat.getInstance().formatShort(t);
	}
	
	public static String getTimestamp(Date t) {
		return getTimestamp(t, C.emptyString);
	}
	
	public static String getTimestamp(Date t, String defaultValue) {
		if (t == null)
			return defaultValue;
		else
			return DateFormat.getInstance().format(t);
	}
	
	public static String getTimestamp(Timestamp t) {
		return getTimestamp(t, C.emptyString);
	}
	
	public static String getTimestamp(Timestamp t, String defaultValue) {
		if (t == null)
			return defaultValue;
		else
			return DateFormat.getInstance().format(t);
	}
	
	public static String getCurrency(Integer i) {
		if (i == null)
			return C.emptyString;
		else
			return getCurrency(i.intValue());
	}
	
	public static String getCurrency(int i) {
		return String.format("%,d", i);
	}
	
	public static String getCurrency(Long l) {
		if (l == null)
			return C.emptyString;
		else
			return getCurrency(l.longValue());
	}
	
	public static String getCurrency(long i) {
		return String.format("%,d", i);
	}
	
	public static String getCurrency(Double d) {
		if (d == null)
			return C.emptyString;
		else
			return getCurrency(d.doubleValue());
	}
	
	public static String getCurrency(double d) {
		String s = String.format("%,f", d);
		return s.contains(".") ? s.replaceAll("0*$", "").replaceAll("\\.$", "") : s;
	}
	
	public static String getString(Integer i) {
		if (i == null)
			return C.emptyString;
		else
			return getString(i.intValue());
	}
	
	public static String getString(int i) {
		return String.valueOf(i);
	}
	
	public static String getString(Long i) {
		if (i == null)
			return C.emptyString;
		else
			return getString(i.longValue());
	}
	
	public static String getString(long i) {
		return String.valueOf(i);
	}
	
	public static String getString(Double d) {
		if (d == null)
			return C.emptyString;
		else
			return getString(d.doubleValue());
	}
	
	public static String getString(double d) {
		String s = String.format("%f", d);
		return s.contains(".") ? s.replaceAll("0*$", "").replaceAll("\\.$", "") : s;
	}
}
