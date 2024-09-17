package com.axonivy.utils.db.resolver;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;

/**
 * Convenience class to define a resolver usable for MS SQL 2005 and later.
 */
public abstract class HSQLDbUtilsResolver extends AbstractDbUtilsResolver {
	@Override
	public String getScriptTableCreateStatement() {
		return "CREATE TABLE %s (%s VARCHAR(255) PRIMARY KEY, %s VARCHAR(10), %s TIMESTAMP, %s CLOB(1M), %s CLOB(1M), %s CLOB(1M))".formatted(
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

	@Override
	public String[] getExportExcludeTableNames() {
		return new String[] {getScriptTableName()};
	}

	@Override
	public boolean isExcelExportLob(Column column) {
		return column.getDataType() == DataType.VARBINARY;
	}
}
