package com.axonivy.utils.db.resolver;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;

/**
 * Convenience class to define a resolver usable for MS SQL 2005 and later.
 */
public abstract class MSSQL2005DbUtilsResolver extends AbstractDbUtilsResolver {
	@Override
	public String getScriptTableCreateStatement() {
		return "CREATE TABLE %s (%s varchar(255), %s varchar(10), %s datetime2, %s varchar(MAX), %s varchar(MAX), %s varchar(MAX) PRIMARY KEY (%s))".formatted(
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
		return new String[] {"trace_*", getScriptTableName()};
	}

	@Override
	public boolean isExcelExportLob(Column column) {
		return column.getDataType() == DataType.VARBINARY;
	}
}
