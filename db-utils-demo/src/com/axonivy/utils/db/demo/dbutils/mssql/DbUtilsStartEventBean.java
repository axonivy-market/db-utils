package com.axonivy.utils.db.demo.dbutils.mssql;

import com.axonivy.utils.db.AbstractDbUtilsStartEventBean;

/**
 * DbUtils start event bean for MSSQL.
 */
public class DbUtilsStartEventBean extends AbstractDbUtilsStartEventBean {

	@Override
	public DbUtilsResolver getDbUtilsResolver() {
		return DbUtilsResolver.get();
	}
}
