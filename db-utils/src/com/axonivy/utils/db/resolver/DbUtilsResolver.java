package com.axonivy.utils.db.resolver;

import java.io.InputStream;
import java.util.List;

import org.dbunit.dataset.Column;

import com.axonivy.utils.db.services.db.Script;

/**
 * Information for the DB Utils page.
 *
 * All of these functions can be overridden to configure DB Utils.
 */
public interface DbUtilsResolver {

	/**
	 * Name of the database.
	 *
	 * @return
	 */
	String getDatabaseName();

	/**
	 * Base URL to find scripts.
	 *
	 * @return
	 */
	String getScriptsUrl();

	/**
	 * Base URL to find data.
	 *
	 * @return
	 */
	String getDataUrl();

	/**
	 * Is auto-update at start enabled?
	 *
	 * @return
	 */
	boolean isAutoupdateEnabled();

	/**
	 * Is the incremental tab enabled?
	 *
	 * @return
	 */
	boolean isIncrementalTabEnabled();

	/**
	 * Is the Sql Statement Tab enabled?
	 *
	 * @return
	 */
	boolean isSqlStatementTabEnabled();

	/**
	 * Is the Excel Tab enabled?
	 *
	 * @return
	 */
	boolean isExcelExportImportTabEnabled();

	/**
	 * Get the name to use for the script table.
	 *
	 * @return
	 */
	default String getScriptTableName() {
		return "DbUtilsScripts";
	}

	/**
	 * Get the name to use for the script name column in the script table.
	 *
	 * @return
	 */
	default String getScriptNameColumn() {
		return "name";
	}

	/**
	 * Get the name to use for the script status column in the script table.
	 *
	 * @return
	 */
	default String getScriptStatusColumn() {
		return "status";
	}

	/**
	 * Get the name to use for the script execution time column in the script table.
	 *
	 * @return
	 */
	default String getScriptExecutedAtColumn() {
		return "executedAt";
	}

	/**
	 * Get the name to use for the script error column in the script table.
	 *
	 * @return
	 */
	default String getScriptErrorColumn() {
		return "error";
	}

	/**
	 * Get the name to use for the script error cause column in the script table.
	 *
	 * @return
	 */
	default String getScriptErrorCauseColumn() {
		return "errorCause";
	}

	/**
	 * Get the name to use for the script source column in the script table.
	 *
	 * @return
	 */
	default String getScriptScriptColumn() {
		return "script";
	}

	/**
	 * DDL to create script table, should use names defined in resolver.
	 *
	 * @return
	 */
	String getScriptTableCreateStatement();

	/**
	 * DDL to drop script table, should use names defined in resolver.
	 *
	 * @return
	 */
	default String getScriptTableDropStatement() {
		return "DROP TABLE %s".formatted(getScriptTableName());
	}

	/**
	 * Get the SQL statement delimiter.
	 *
	 * @return
	 */
	String getDelimiter();

	/**
	 * Should empty lines be replaced by the delimiter?
	 *
	 * In other words: should empty lines delimit a statement?
	 * @return
	 */
	default boolean delimitEmptyLines() {
		return true;
	}

	/**
	 * Find scripts which are available to read from original sources.
	 *
	 * The script name set in this function will be shown in UIs and also be available in the {@link #readScript(Script)} function.
	 *
	 * @return list of scripts found and available
	 */
	List<Script> findAvailableScripts();

	/**
	 * Read script from original source.
	 *
	 * @param script
	 * @return content of script
	 */
	String readScript(Script script);

	/**
	 * Get the table names to exclude from Excel export.
	 *
	 * @return
	 */
	String[] getExportExcludeTableNames();

	/**
	 * Read a resource in the context (classloader) of the provided {@link DbUtilsResolver}.
	 *
	 * @param path
	 * @return
	 */
	default InputStream getResourceAsStream(String path) {
		return getClass().getResourceAsStream(path);
	}

	/**
	 * Should this column Type be Excel exported as a Lob?
	 *
	 * @param column
	 * @return
	 */
	boolean isExcelExportLob(Column column);
}
