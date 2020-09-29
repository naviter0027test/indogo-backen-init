package com.lionpig.webui.database;

import javax.servlet.ServletContext;

public class ConnectionFactory {
	private static ConnectionFactory factory = new ConnectionFactory();
	public static ConnectionFactory getInstance() {
		return factory;
	}
	
	private ConnectionFactory() {
		
	}
	
	public IConnection createConnection(ServletContext sc) throws Exception {
		String db_type = sc.getInitParameter("DB_TYPE");
		if (db_type == null)
			throw new Exception("Cannot found DB_TYPE init parameter");
		
		if (db_type.equals("MYSQL"))
			return new MySqlConnection(sc);
		else
			throw new Exception("Database type [" + db_type + "] currently not supported");
	}
}
