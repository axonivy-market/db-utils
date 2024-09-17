package com.axonivy.utils.db.demo.dbutils.hsqldb;

import com.axonivy.utils.db.AbstractDbUtilsStartEventBean;

/**
 * DbUtils start event bean for HSQLDB.
 */
public class DbUtilsStartEventBean extends AbstractDbUtilsStartEventBean {

	@Override
	public DbUtilsResolver getDbUtilsResolver() {
		return DbUtilsResolver.get();
	}
}
