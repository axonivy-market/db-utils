package com.axonivy.utils.db.demo.dbutils.oracle;

import com.axonivy.utils.db.resolver.OracleDBUtilsResolver;

/**
 * DbUtils resolver class for Oracle.
 */
public class DbUtilsResolver extends OracleDBUtilsResolver {
  private static final DbUtilsResolver INSTANCE = new DbUtilsResolver();

  public static DbUtilsResolver get() {
    return INSTANCE;
  }

  @Override
  public String getDatabaseName() {
    return "comicworld_oracle";
  }

  @Override
  public String getScriptsUrl() {
    return "classpath:/resources/sql/incremental/oracle";
  }
}
