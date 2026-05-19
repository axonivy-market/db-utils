package com.axonivy.utils.db.demo.dbutils.oracle;

import com.axonivy.utils.db.AbstractDbUtilsStartEventBean;

/**
 * DbUtils start event bean for Oracle.
 */
public class DbUtilsStartEventBean extends AbstractDbUtilsStartEventBean {

	public DbUtilsStartEventBean() {
		super(DbUtilsResolver.get());
	}
}
