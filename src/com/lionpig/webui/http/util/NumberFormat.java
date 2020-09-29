package com.lionpig.webui.http.util;

import java.text.DecimalFormat;
import java.util.Locale;

public class NumberFormat {
	private static NumberFormat instance = new NumberFormat();
	public static NumberFormat getInstance() {
		return instance;
	}
	
	private DecimalFormat decimalFormat;
	
	private NumberFormat() {
		decimalFormat = new DecimalFormat("#");
	}
	
	public String formatCurrencyNT(int n) {
		java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(Locale.TAIWAN);
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(0);
		return nf.format(n);
	}
	
	public String formatCurrencyRP(int n) {
		return formatCurrencyNT(n).replaceAll("NT", "RP");
	}
	
	public String formatWithoutScientificForm(double d) {
		return decimalFormat.format(d);
	}
}
