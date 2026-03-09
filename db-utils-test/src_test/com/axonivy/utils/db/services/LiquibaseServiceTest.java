package com.axonivy.utils.db.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axonivy.utils.db.TestBase;
import com.axonivy.utils.db.services.db.BaseDAO;
import com.axonivy.utils.db.test.dbutils.DbUtilsResolver;

import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
public class LiquibaseServiceTest extends TestBase {

	@BeforeAll
	public static void setLiquibaseVariables(AppFixture fixture) {
		fixture.var("com.axonivy.utils.db.liquibasechangelog", "/resources/liquibase/test-changelog.yaml");
	}

	@BeforeEach
	public void resetLiquibaseArtifacts() {
		var dao = BaseDAO.get(DbUtilsResolver.get());
		dao.statement(c -> {
			for (var table : new String[] {"LIQUIBASE_TEST", "DATABASECHANGELOG", "DATABASECHANGELOGLOCK"}) {
				try (var stmt = c.prepareStatement("DROP TABLE " + table + " IF EXISTS")) {
					stmt.executeUpdate();
				}
			}
			return null;
		});
	}

	@Test
	public void updateAppliesChangelog() {
		var service = LiquibaseService.get(DbUtilsResolver.get());
		service.update("");

		var dao = BaseDAO.get(DbUtilsResolver.get());
		var result = dao.statement(c -> {
			var tableExists = c.getMetaData().getTables(null, null, "LIQUIBASE_TEST", new String[] {"TABLE"}).next();
			var countStmt = c.prepareStatement("SELECT COUNT(*) FROM LIQUIBASE_TEST");
			var rs = countStmt.executeQuery();
			var rowCount = rs.next() ? rs.getInt(1) : -1;
			return new Object[] { tableExists, rowCount };
		});

		assertThat(result[0]).isEqualTo(true);
		assertThat(result[1]).isEqualTo(1);
	}
}
