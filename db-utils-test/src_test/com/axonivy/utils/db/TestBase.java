package com.axonivy.utils.db;

import org.junit.jupiter.api.BeforeAll;

import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
public class TestBase {

	@BeforeAll
	public static void setVariables(AppFixture fixture) {
		fixture.var("com.axonivy.utils.db.database", "dbutilstest");
		fixture.var("com.axonivy.utils.db.scriptsurl", "classpath:/resources/sql");
		fixture.var("com.axonivy.utils.db.dataurl", "classpath:/resources/data");
		fixture.var("com.axonivy.utils.db.autoupdate", "false");
	}
}
