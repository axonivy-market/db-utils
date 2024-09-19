package com.axonivy.utils.db.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.axonivy.utils.db.TestBase;
import com.axonivy.utils.db.services.db.BaseDAO;
import com.axonivy.utils.db.services.enums.Status;
import com.axonivy.utils.db.test.dbutils.DbUtilsResolver;

import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
public class ScriptServiceTest extends TestBase {

	@Test
	public void test() throws Exception {
		var service = ScriptService.get(DbUtilsResolver.get());

		var scripts = service.updateScripts();

		assertThat(scripts).isNotNull();
		assertThat(scripts.available()).hasSize(3);
		assertThat(scripts.unavailable()).hasSize(0);

		assertThat(scripts.available().stream().filter(s -> s.getStatus() != Status.NONE).findAny()).isEmpty();

		service.runNecessary(false, null);

		scripts = service.updateScripts();
		assertThat(scripts.available().stream().filter(s -> s.getStatus() != Status.DONE).findAny()).isEmpty();

		var dao = BaseDAO.get(DbUtilsResolver.get());

		var heroes = dao.statement(c -> {
			var preparedStatement = c.prepareStatement("SELECT COUNT(*) FROM Hero");
			var resultSet = preparedStatement.executeQuery();
			return resultSet.next() ? resultSet.getInt(1) : -1;
		});

		assertThat(heroes).isEqualTo(3);
	}
}
