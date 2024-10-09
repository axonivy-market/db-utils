package com.axonivy.utils.db.demo.dbutils.hsqldb;

import com.axonivy.utils.db.AbstractDbUtilsStartEventBean;

/**
 * DbUtils start event bean for HSQLDB.
 */
public class DbUtilsStartEventBean extends AbstractDbUtilsStartEventBean {

	public DbUtilsStartEventBean() {
		super(DbUtilsResolver.get());
	}
}
