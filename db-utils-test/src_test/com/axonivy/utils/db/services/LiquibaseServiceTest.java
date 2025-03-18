package com.axonivy.utils.db.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.axonivy.utils.db.TestBase;
import com.axonivy.utils.db.services.db.BaseDAO;
import com.axonivy.utils.db.test.dbutils.DbUtilsResolver;

import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
public class LiquibaseServiceTest extends TestBase {
	@Test
	public void updateTest() {
		var dao = BaseDAO.get(DbUtilsResolver.get());

		assertThat(dao.tableExists("DATABASECHANGELOG")).isFalse();
		assertThat(dao.tableExists("TEST1")).isFalse();
		assertThat(dao.tableExists("TEST2")).isFalse();

		LiquibaseService.get(DbUtilsResolver.get()).update();

		assertThat(dao.tableExists("DATABASECHANGELOG")).isTrue();
		assertThat(dao.tableExists("TEST1")).isTrue();
		assertThat(dao.tableExists("TEST2")).isTrue();
	}
}
