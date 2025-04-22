package com.project.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSource {
	/*
	parametry do połączenia z bazą danych
	1. DB_DIR - katalog, w którym znajduje się baza danych
	2. DB_NAME - nazwa bazy danych
	3. DB_USERNAME - nazwa użytkownika bazy danych
	4. DB_USER_PASSWORD - hasło użytkownika bazy danych
	5. HSQL_ADDITIONAL_PARAMS - dodatkowe parametry do połączenia z bazą danych

	 */
	private final static String DB_DIR = "db";
	private final static String DB_NAME = "projekty";
	private final static String DB_USERNAME = "admin";
	private final static String DB_USER_PASSWORD = "admin";
	/*
	1. sql.syntax_pgs - this property, when set true, enables support for TEXT and SERIAL types.
		It also enables NEXTVAL, CURRVAL and LASTVAL syntax and also allow compatibility with some other aspects of this dialect.
	2. hsqldb.write_delay - If the property is true, the default WRITE DELAY property of the database is used, which is 500 milliseconds.
		If the property is false, the WRITE DELAY is set to 0 seconds.
	*/
	private final static String HSQL_ADDITIONAL_PARAMS = ";hsqldb.write_delay=false;sql.syntax_pgs=true";
	private final static String DB_URL = String.format("jdbc:hsqldb:file:%s/%s%s", DB_DIR, DB_NAME, HSQL_ADDITIONAL_PARAMS);


	private final static HikariDataSource ds;

	static {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(DB_URL);
			config.setUsername(DB_USERNAME);
			config.setPassword(DB_USER_PASSWORD);
			config.setMaximumPoolSize(1);
			ds = new HikariDataSource(config);
	}

	private DataSource() {}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

}