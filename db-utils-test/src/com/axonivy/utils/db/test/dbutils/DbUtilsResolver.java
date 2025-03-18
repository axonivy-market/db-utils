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

	@Override
	public String getLiquibaseChangelog() {
		return "resources/liquibasetest/changelog.yaml";
	}


	@Override
	public String getScriptsUrl() {
		return "classpath:/resources/sql";
	}

	@Override
	public String getDataUrl() {
		return "classpath:/resources/data";
	}

	@Override
	public boolean isAutoupdateEnabled() {
		return false;
	}
}