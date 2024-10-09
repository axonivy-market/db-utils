package com.axonivy.utils.db.demo.dbutils.mssql;

import com.axonivy.utils.db.AbstractDbUtilsStartEventBean;

/**
 * DbUtils start event bean for MSSQL.
 */
public class DbUtilsStartEventBean extends AbstractDbUtilsStartEventBean {

	public DbUtilsStartEventBean() {
		super(DbUtilsResolver.get());
	}
}
