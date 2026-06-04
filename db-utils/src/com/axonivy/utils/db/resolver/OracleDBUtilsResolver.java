package com.axonivy.utils.db.resolver;

public abstract class OracleDBUtilsResolver extends AbstractDbUtilsResolver {
  
  @Override
  public String getScriptTableCreateStatement() {
      return """
          CREATE TABLE %s (
              %s VARCHAR2(255) PRIMARY KEY,
              %s VARCHAR2(10),
              %s TIMESTAMP,
              %s CLOB,
              %s CLOB,
              %s CLOB
          )
          """.formatted(
              getScriptTableName(),
              getScriptNameColumn(),
              getScriptStatusColumn(),
              getScriptExecutedAtColumn(),
              getScriptErrorColumn(),
              getScriptErrorCauseColumn(),
              getScriptScriptColumn()
          );
  }
}
