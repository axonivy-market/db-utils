package com.axonivy.utils.db.test.dbutils;

import com.axonivy.utils.db.resolver.HSQLDbUtilsResolver;

/**
 * DbUtils resolver class for HSQLDB.
 */
public class DbUtilsResolver extends HSQLDbUtilsResolver {
	private static final DbUtilsResolver INSTANCE = new DbUtilsResolver();

	public static DbUtilsResolver get() {
		return INSTANCE;
	}

	@Override
	public String getDatabaseName() {
		return "dbutilstest";
	}

	@Override
	public String getScriptTableName() {
		return "SCRIPTS";
	}
}