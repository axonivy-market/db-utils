package com.axonivy.utils.db.demo.dbutils.hsqldb;

import com.axonivy.utils.db.resolver.HSQLDbUtilsResolver;

import ch.ivyteam.ivy.environment.Ivy;

import org.dbunit.dataset.Column;


/**
 * DbUtils resolver class for HSQLDB.
 */
public class DbUtilsResolver extends HSQLDbUtilsResolver {
	private static final DbUtilsResolver INSTANCE = new DbUtilsResolver();
	private static final Column COLUMN = new Column(null, null);

	public static DbUtilsResolver getInstance() {
		Ivy.log().info(COLUMN);
		return INSTANCE;
	}
}