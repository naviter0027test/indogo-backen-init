package com.lionpig.webui.http.tablepage;

import java.util.ArrayList;
import java.util.List;

public class TablePageRowAttribute {
	public List<String> HtmlClass = new ArrayList<String>();
	
	public void Reset() {
		this.HtmlClass.clear();
	}
}
