package com.axonivy.utils.db.demo.dbutils.mssql;

import com.axonivy.utils.db.AbstractLiquibaseStartEventBean;

public class LiquibaseStartEventBean extends AbstractLiquibaseStartEventBean {

	public LiquibaseStartEventBean() {
		super(DbUtilsResolver.get());
	}
}
