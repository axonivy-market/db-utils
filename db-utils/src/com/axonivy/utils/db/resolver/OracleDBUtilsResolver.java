package com.axonivy.utils.db.resolver;

public abstract class OracleDBUtilsResolver extends AbstractDbUtilsResolver{
  
  @Override
  public String getScriptTableCreateStatement() {
    return "CREATE TABLE employees (id NUMBER PRIMARY KEY, name VARCHAR2(100))";
  }
}
