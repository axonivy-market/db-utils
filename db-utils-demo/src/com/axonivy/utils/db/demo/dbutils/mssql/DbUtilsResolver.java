package com.axonivy.utils.db.demo.dbutils.mssql;

import com.axonivy.utils.db.resolver.MSSQL2005DbUtilsResolver;

/**
 * DbUtils resolver class for MSSQL.
 */
public class DbUtilsResolver extends MSSQL2005DbUtilsResolver {
	private static final DbUtilsResolver INSTANCE = new DbUtilsResolver();

	public static DbUtilsResolver get() {
		return INSTANCE;
	}

	@Override
	public String getDatabaseName() {
		return "comicworld_mssql";
	}

	@Override
	public String getScriptsUrl() {
		return "classpath:/resources/sql/incremental/mssql";
	}
}