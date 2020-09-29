package com.lionpig.webui.http.func;

import java.util.Hashtable;

import com.lionpig.webui.http.tablepage.TablePageColumn;

public interface TablePageRowPrinter {
	void print(Hashtable<String, TablePageColumn> htColumn);
}
