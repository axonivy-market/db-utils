package com.axonivy.utils.db.services.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.axonivy.utils.db.services.enums.Status;
import com.axonivy.utils.db.test.dbutils.DbUtilsResolver;

import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
@TestMethodOrder(OrderAnnotation.class)
public class ScriptDAOTest {

	@Test
	@Order(1)
	public void testAssert(){
		var dao = ScriptDAO.get(DbUtilsResolver.get());

		var exists = dao.scriptTableExists();
		assertThat(exists).isFalse();

		dao.assertScriptTable();

		exists = dao.scriptTableExists();
		assertThat(exists).isTrue();
	}

	@Test
	@Order(2)
	public void testCrud(){
		var dao = ScriptDAO.get(DbUtilsResolver.get());

		var scripts = dao.findAllScripts();
		assertThat(scripts).isEmpty();

		var script = new Script();
		script.setStatus(Status.NONE);
		script.setName("testscript");
		dao.saveScript(script);

		scripts = dao.findAllScripts();
		assertThat(scripts).hasSize(1);
		assertThat(scripts.get(0).getStatus()).isEqualTo(Status.NONE);

		scripts.get(0).setStatus(Status.DONE);
		dao.updateScript(scripts.get(0));

		scripts = dao.findAllScripts();
		assertThat(scripts).hasSize(1);
		assertThat(scripts.get(0).getStatus()).isEqualTo(Status.DONE);

		dao.deleteScript(scripts.get(0));
		scripts = dao.findAllScripts();
		assertThat(scripts).hasSize(0);
	}
}
