package com.lionpig.webui.http.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.lionpig.webui.database.HistoryData;

public class Helper {
	public static String replaceNull(String s) throws Exception {
		if (s == null)
			return "";
		else
			return s;
	}
	
	public static String getString(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		return getString(params, key, mustBe, key);
	}
	
	public static String getString(Hashtable<String, String> params, String key, boolean mustBe, String displayName) throws Exception {
		return getString(params, key, mustBe, displayName, false);
	}
	
	public static String getString(Hashtable<String, String> params, String key, boolean mustBe, String displayName, boolean onlyShowDisplayNameAsError) throws Exception {
		String s = params.get(key);
		if (s != null && s.length() == 0)
			s = null;
		if (mustBe && s == null) {
			if (onlyShowDisplayNameAsError)
				throw new Exception (displayName);
			else
				throw new Exception ("input [" + displayName + "] cannot be empty");
		}
		return s;
	}
	
	public static int getInt(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		return getInt(params, key, mustBe, key);
	}
	
	public static Integer getIntNullable(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		return getIntNullable(params, key, mustBe, key);
	}
	
	public static Integer getIntNullable(Hashtable<String, String> params, String key, boolean mustBe, String displayName) throws Exception {
		String s = getString(params, key, mustBe, displayName);
		if (s == null || s.length() == 0)
			return null;
		else
			return Integer.parseInt(s);
	}
	
	public static int getInt(Hashtable<String, String> params, String key, boolean mustBe, String displayName) throws Exception {
		return getInt(params, key, mustBe, displayName, false);
	}
	
	public static int getInt(Hashtable<String, String> params, String key, boolean mustBe, String displayName, boolean onlyShowDisplayNameAsError) throws Exception {
		String s = getString(params, key, mustBe, displayName, onlyShowDisplayNameAsError);
		if (s == null || s.length() == 0)
			return 0;
		else
			return Integer.parseInt(s);
	}
	
	public static long getLong(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		String s = getString(params, key, mustBe);
		if (s == null || s.length() == 0)
			return 0;
		else
			return Long.parseLong(s);
	}
	
	public static Long getLongNullable(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		String s = getString(params, key, mustBe);
		if (s == null || s.length() == 0)
			return null;
		else
			return Long.parseLong(s);
	}
	
	public static double getDouble(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		String s = getString(params, key, mustBe);
		if (s == null || s.length() == 0)
			return 0;
		else
			return Double.parseDouble(s);
	}
	
	public static Double getDoubleNullable(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		String s = getString(params, key, mustBe);
		if (s == null || s.length() == 0)
			return null;
		else
			return Double.parseDouble(s);
	}
	
	public static String[] getStringArray(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		return getStringArray(params, key, mustBe, String.valueOf(C.char_31));
	}
	
	public static String[] getStringArray(Hashtable<String, String> params, String key, boolean mustBe, String delimiter) throws Exception {
		String s = params.get(key);
		if (s == null || s.length() == 0) {
			if (mustBe)
				throw new Exception ("input [" + key + "] cannot be empty");
			else
				return new String[0];
		}
		else {
			return StringUtils.splitByWholeSeparatorPreserveAllTokens(s, delimiter);
		}
	}
	
	public static List<String> getStringArrayByReadLine(Hashtable<String, String> params, String key) throws Exception {
		String s = getString(params, key, false);
		if (s == null || s.length() == 0) {
			return new ArrayList<>();
		} else {
			try (BufferedReader br = new BufferedReader(new StringReader(s))) {
				String line = null;
				List<String> list = new ArrayList<>();
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0) {
						list.add(line);
					}
				}
				return list;
			}
		}
	}
	
	public static int[] getIntArray(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		String[] s = getStringArray(params, key, mustBe);
		int[] r = new int[s.length];
		for (int i = 0; i < r.length; i++) {
			r[i] = Integer.parseInt(s[i]);
		}
		return r;
	}
	
	public static long[] getLongArray(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		String[] s = getStringArray(params, key, mustBe);
		long[] r = new long[s.length];
		for (int i = 0; i < r.length; i++) {
			r[i] = Long.parseLong(s[i]);
		}
		return r;
	}
	
	public static Long[] getLongNullableArray(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		String[] s = getStringArray(params, key, mustBe);
		Long[] r = new Long[s.length];
		for (int i = 0; i < r.length; i++) {
			if (s[i].length() > 0)
				r[i] = Long.parseLong(s[i]);
		}
		return r;
	}
	
	public static Timestamp getTimestamp(Hashtable<String, String> params, String key, boolean mustBe) throws Exception {
		return getTimestamp(params, key, mustBe, key);
	}
	
	public static Timestamp getTimestamp(Hashtable<String, String> params, String key, boolean mustBe, String displayName) throws Exception {
		return getTimestamp(params, key, mustBe, displayName, false);
	}
	
	public static Timestamp getTimestamp(Hashtable<String, String> params, String key, boolean mustBe, String displayName, boolean onlyShowDisplayNameAsError) throws Exception {
		String v = getString(params, key, mustBe, displayName, onlyShowDisplayNameAsError);
		if (v == null)
			return null;
		else {
			DateFormat f = DateFormat.getInstance();
			return new Timestamp(f.parse(v).getTime());
		}
	}
	
	public static boolean isNullOrEmpty(String s) {
		if (s == null)
			return true;
		else if (s.length() == 0)
			return true;
		else
			return false;
	}
	
	private static NumberFormat nf = NumberFormat.getInstance();
	
	public static String getCellValueAsString(Row row, int i) throws Exception {
		Cell c = row.getCell(i);
		if (c == null)
			return "";
		int cellType = c.getCellType();
		switch (cellType) {
			case Cell.CELL_TYPE_STRING:
				return c.getStringCellValue();
			case Cell.CELL_TYPE_NUMERIC:
				return nf.formatWithoutScientificForm(c.getNumericCellValue());
			case Cell.CELL_TYPE_BOOLEAN:
				return String.valueOf(c.getBooleanCellValue());
			case Cell.CELL_TYPE_BLANK:
				return "";
			default:
				throw new Exception("Unsupported cell type [" + cellType + "]");
		}
	}

	public static double getCellValueAsDouble(Row row, int i) throws Exception {
		Cell c = row.getCell(i);
		if (c == null)
			return 0;
		int cellType = c.getCellType();
		switch (cellType) {
			case Cell.CELL_TYPE_STRING:
				return Double.parseDouble(c.getStringCellValue());
			case Cell.CELL_TYPE_NUMERIC:
				return c.getNumericCellValue();
			case Cell.CELL_TYPE_BLANK:
				return 0;
			default:
				throw new Exception("Unsupported cell type [" + cellType + "]");
		}
	}
	
	public static String toNullString(Timestamp t) {
		if (t == null)
			return null;
		else
			return DateFormat.getInstance().format(t);
	}
	
	public static boolean isEquals(Timestamp t1, Timestamp t2) {
		if (t1 == null && t2 == null)
			return true;
		else if (t1 == null && t2 != null)
			return false;
		else if (t1 != null && t2 == null)
			return false;
		else
			return t1.equals(t2);
	}
	
	public static boolean isEquals(String s1, String s2) {
		String s3;
		if (s1 == null)
			s3 = null;
		else if (s1.length() == 0)
			s3 = null;
		else
			s3 = s1;
		
		String s4;
		if (s2 == null)
			s4 = null;
		else if (s2.length() == 0)
			s4 = null;
		else
			s4 = s2;
		
		if (s3 == null && s4 == null)
			return true;
		else if (s3 != null && s4 == null)
			return false;
		else if (s3 == null && s4 != null)
			return false;
		else
			return s3.equals(s4);
	}
	
	public static HistoryData[] toHistoryDataArray(List<HistoryData> list) {
		HistoryData[] datas = new HistoryData[list.size()];
		for (int i = 0; i < datas.length; i++)
			datas[i] = list.get(i);
		return datas;
	}
	
	private static final SecureRandom numberGenerator = new SecureRandom();
	
	public static long randomSeq() {
		byte[] randomBytes = new byte[8];
		numberGenerator.nextBytes(randomBytes);
		long logId = 0;
		for (int i = 0; i < 8; i++)
			logId = (logId << 8) | (randomBytes[i] & 0xff);
		return logId;
	}
}
