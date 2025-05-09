package com.axonivy.utils.db.resolver;

/**
 * Convenience class to define a resolver usable for DB2 and later.
 */
public abstract class DB2DbUtilsResolver extends AbstractDbUtilsResolver {

	@Override
	public String getScriptTableCreateStatement() {
		return "CREATE TABLE %s (%s VARCHAR(255) NOT NULL PRIMARY KEY, %s VARCHAR(10), %s TIMESTAMP, %s CLOB(1M), %s CLOB(1M), %s CLOB(1M))".formatted(
				getScriptTableName(),
				getScriptNameColumn(),
				getScriptStatusColumn(),
				getScriptExecutedAtColumn(),
				getScriptErrorColumn(),
				getScriptErrorCauseColumn(),
				getScriptScriptColumn(),
				getScriptNameColumn()
				);
	}
}
