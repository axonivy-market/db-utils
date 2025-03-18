package com.axonivy.utils.db;

import org.hsqldb.util.DatabaseManagerSwing;
import org.junit.jupiter.api.BeforeAll;

import com.axonivy.utils.db.services.DatabaseService;
import com.axonivy.utils.db.services.ScriptService;
import com.axonivy.utils.db.test.dbutils.DbUtilsResolver;

import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
public class TestBase {

	@BeforeAll
	public static void cleanupDb() throws Exception {
		try (var stream = DbUtilsResolver.get().getResourceAsStream("/resources/dropall.sql")) {
			ScriptService.get(DbUtilsResolver.get()).runScript(stream);
		}
	}

	/**
	 * Start the database manager.
	 */
	public static void startDBManager()  {
		startDBManager("dbutilstest");
	}

	/**
	 * Start the database manager.
	 */
	public static void startDBManager(String databaseName)  {
		var database = DatabaseService.get(DbUtilsResolver.get()).getExternalDatabase();
		String url = database.getConfiguration().url();
		DatabaseManagerSwing.main(new String[] {"--url", url, "--noexit" });
	}

}
