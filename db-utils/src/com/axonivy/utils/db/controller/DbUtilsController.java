package com.axonivy.utils.db.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.axonivy.utils.db.resolver.DbUtilsResolver;
import com.axonivy.utils.db.services.DatabaseService;
import com.axonivy.utils.db.services.ScriptService;

import ch.ivyteam.ivy.db.Database;
import ch.ivyteam.ivy.db.IExternalDatabase;

/**
 * Controller for the DbUtils Dialog.
 */
public class DbUtilsController {
	private static final Logger LOG = LogManager.getLogger(new MessageFormatMessageFactory());
	private JsfLogger log = new JsfLogger();
	private DbUtilsResolver dbUtilsResolver;
	private DatabaseService databaseService;
	private ScriptService scriptService;
	private ScriptTableController availableController;
	private ScriptTableController unavailableController;
	private String sqlStatement;
	private int maxResults = 100;
	private StreamedContent exportedExcel;
	private StreamedContent exportedZip;
	private boolean cleanBeforeExcelImport;
	private boolean handleClasspathResourcesInExcelImport;

	/**
	 * Create the controller.
	 *
	 * @param dbUtilsResolver
	 */
	public DbUtilsController(DbUtilsResolver dbUtilsResolver) {
		this.dbUtilsResolver = dbUtilsResolver;
		databaseService = DatabaseService.get(dbUtilsResolver);
		scriptService = ScriptService.get(dbUtilsResolver);
		unavailableController = new ScriptTableController(false, scriptService, log);
		availableController = new ScriptTableController(true, scriptService, log);
		refreshAll(null);
	}

	/**
	 * Get basic info about database we are working with.
	 *
	 * @return
	 */
	public String getDbFootprint() {
		var url = dbUtilsResolver.getDatabaseName();
		var user = "";
		IExternalDatabase database = databaseService.getExternalDatabase();
		if(database != null) {
			Database dbConfig = database.getConfiguration();
			url = dbConfig.url();
			user = dbConfig.user();
		}
		return "user: %s database: %s".formatted(user, url);
	}

	/**
	 * Refresh all scripts.
	 *
	 * @param event
	 */
	public void refreshAll(ActionEvent event) {
		log.clearLog();
		log.info("Refreshing scripts");
		var scripts = scriptService.updateScripts();
		unavailableController.setScripts(scripts.unavailable());
		availableController.setScripts(scripts.available());
		log.info("Scripts, available: {0} unavailable: {1}", scripts.available().size(), scripts.unavailable().size());
	}

	/**
	 * Run all necessary scripts.
	 *
	 * @param event
	 */
	public void runNecessary(ActionEvent event) {
		log.clearLog();
		availableController.runNecessary();
	}

	/**
	 * Force run all necessary scripts.
	 *
	 * @param event
	 */
	public void forceNecessary(ActionEvent event) {
		log.clearLog();
		availableController.runNecessary(true);
	}

	/**
	 * Execute the SQL statement.
	 *
	 * @param event
	 */
	public void executeSqlStatement(ActionEvent event) {
		log.clearLog();
		log.info("Executing SQL statement: {0}", sqlStatement);
		log.info("");

		try (
				var connection = databaseService.getDatabaseConnection();
				var statement = connection.createStatement()) {
			statement.execute(sqlStatement);
			logResult(statement);
		} catch (SQLException e) {
			log.error("Exception while executing statement.", e);
		}
	}
	/**
	 * Export all tables to Excel
	 *
	 * @return
	 * @throws Exception
	 */
	public void prepareExport(ActionEvent event) throws Exception {
		log.clearLog();
		log.info("Exporting data...");
		try {
			InputStream stream = databaseService.exportXls();
			exportedExcel = DefaultStreamedContent.builder()
					.stream(() -> stream)
					.contentType("application/vnd.ms-excel")
					.name("export.xls").build();
		} catch(Exception e) {
			log.error("Exception while exporting excel data.", e);
		}

		log.info("Done.");
	}

	/**
	 * Export all tables to Excel and all templates from DB, which were stored as lobs
	 *
	 * @return
	 * @throws Exception
	 */
	public void prepareExportZip(ActionEvent event) throws Exception {
		log.clearLog();
		log.info("Exporting data to zip...");
		try {
			InputStream stream = databaseService.exportZip();
			exportedZip = DefaultStreamedContent.builder()
					.stream(() -> stream)
					.contentType("application/zip")
					.name("export.zip").build();
		} catch(Exception e) {
			log.error("Exception while exporting zip data.", e);
		}

		log.info("Done.");
	}

	public void loadExcelData(FileUploadEvent event) {
		log.clearLog();
		log.info("Loading excel data: {0} clean: {1}", event.getFile().getFileName(), cleanBeforeExcelImport);
		try (InputStream inputStream = event.getFile().getInputStream()) {
			databaseService.importExcelData(cleanBeforeExcelImport, handleClasspathResourcesInExcelImport, inputStream);
		} catch (Exception e) {
			log.error("Exception while loading excel data.", e);
		}
		log.info("Done.");
	}

	/**
	 * @return the availableController
	 */
	public ScriptTableController getAvailableController() {
		return availableController;
	}

	/**
	 * @return the unavailableController
	 */
	public ScriptTableController getUnavailableController() {
		return unavailableController;
	}

	/**
	 * Get the SQL statement.
	 *
	 * @return
	 */
	public String getSqlStatement() {
		return sqlStatement;
	}

	/**
	 * Set the SQL statement.
	 *
	 * @param sqlStatement
	 */
	public void setSqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

	/**
	 * Max results per ResultSet to show.
	 *
	 * @return
	 */
	public int getMaxResults() {
		return maxResults;
	}

	/**
	 * Max results per ResultSet to show.
	 *
	 * @param maxResults
	 */
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * @return the cleanBeforeExcelImport
	 */
	public boolean isCleanBeforeExcelImport() {
		return cleanBeforeExcelImport;
	}

	/**
	 * @param cleanBeforeExcelImport the cleanBeforeExcelImport to set
	 */
	public void setCleanBeforeExcelImport(boolean cleanBeforeExcelImport) {
		this.cleanBeforeExcelImport = cleanBeforeExcelImport;
	}

	/**
	 * @return the handleClasspathResourcesInExcelImport
	 */
	public boolean isHandleClasspathResourcesInExcelImport() {
		return handleClasspathResourcesInExcelImport;
	}

	/**
	 * @param handleClasspathResourcesInExcelImport the handleClasspathResourcesInExcelImport to set
	 */
	public void setHandleClasspathResourcesInExcelImport(boolean handleClasspathResourcesInExcelImport) {
		this.handleClasspathResourcesInExcelImport = handleClasspathResourcesInExcelImport;
	}

	/**
	 * @return the exportedExcel
	 */
	public StreamedContent getExportedExcel() {
		return exportedExcel;
	}

	/**
	 * @return the exportedZip
	 */
	public StreamedContent getExportedZip() {
		return exportedZip;
	}

	/**
	 * Get the current settings.
	 *
	 * @return
	 */
	public String getSettings() {
		try (var stringWriter = new StringWriter();
				var w = new PrintWriter(stringWriter)) {

			IExternalDatabase db = databaseService.getExternalDatabase();

			w.println("Settings:");
			w.println();
			w.println("Scripts:");
			w.println("  URL:           %s".formatted(dbUtilsResolver.getScriptsUrl()));
			w.println("  Table:         %s".formatted(dbUtilsResolver.getScriptTableName()));
			w.println();
			w.println("Database:");
			if(db == null) {
				w.println("  -");
			}
			else {
				var cfg = db.getConfiguration();
				w.println("  Name:          %s".formatted(dbUtilsResolver.getDatabaseName()));
				w.println("  URL:           %s".formatted(cfg.url()));
				w.println("  User:          %s".formatted(cfg.user()));
				w.println("  Properties:");

				cfg.properties().entrySet().stream().sorted(Comparator.comparing(Entry::getKey)).forEach(e -> {
					w.println("    %-20s %s".formatted(e.getKey() + ":", e.getValue()));
				});
			}

			return stringWriter.toString();
		} catch (IOException e) {
			return ExceptionUtils.getStackTrace(e);
		}
	}

	/**
	 * Log an SQL result.
	 *
	 * @param statement
	 * @throws SQLException
	 */
	public void logResult(Statement statement) throws SQLException {
		var result = 0;
		// Loop over all ResultSets and update counts.
		do {
			if(result > 0) {
				log.info("");
			}

			result++;
			log.info("Result %3d".formatted(result));
			log.info("==========");
			log.info("");

			try (var resultSet = statement.getResultSet()) {
				if(resultSet != null) {
					try {
						logResultSet(resultSet);
					} catch (Exception e) {
						log.error("Exception while reading result set.", e);
					}
				} else {
					var count = statement.getUpdateCount();
					if(count >= 0) {
						log.info("Rows updated: %d".formatted(count));
					}
					else {
						log.info("Unknown SQL result type.");
					}
				}
			}
		} while(statement.getMoreResults() || statement.getUpdateCount() != -1);

	}

	/**
	 * Log a single result set.
	 *
	 * @param resultSet
	 * @throws SQLException
	 */
	protected void logResultSet(ResultSet resultSet) throws SQLException {
		log.info("Result:\n{0}", resultSetToString(resultSet));
	}

	/**
	 * Convert a result set to a String.
	 *
	 * @param rs
	 * @param sizes
	 * @return
	 * @throws SQLException
	 */
	public String resultSetToString(ResultSet rs, int...sizes) throws SQLException {
		var sw = new StringWriter();
		var md = rs.getMetaData();

		Builder<Object> builder = Stream.builder();
		int cols = md.getColumnCount();
		for(int c=1; c<=cols; c++) {
			builder.add(md.getColumnName(c));
		}

		sw.append("%s%n".formatted(objectsToString(builder.build(), sizes)));
		sw.append("%n".formatted());

		int count = 0;
		while(rs.next()) {
			count++;
			Builder<Object> rowBuilder = Stream.builder();
			for(int c=1; c<=cols; c++) {
				rowBuilder.add(rs.getObject(c));
			}
			sw.append("%s%n".formatted(objectsToString(rowBuilder.build(), sizes)));
		}

		sw.append("%n".formatted());
		sw.append("Rows: %d%n".formatted(count));
		return sw.toString();
	}

	public String objectsToString(Stream<Object> objects, int...sizes) throws SQLException {
		var sw = new StringBuilder();

		var array = objects.toArray();

		for (int i = 0; i < array.length; i++) {
			if(i > 0) {
				sw.append(" ");
			}
			var fmt = "%" + (sizes.length > i ? "-%d".formatted(sizes[i]) : "") + "s";
			sw.append(fmt.formatted(array[i]));
		}

		return sw.toString();
	}

	/**
	 * Get the DB Utils Resolver.
	 *
	 * @return
	 */
	public DbUtilsResolver getDbUtilsResolver() {
		return dbUtilsResolver;
	}

	/**
	 * Get the logger.
	 *
	 * @return
	 */
	public JsfLogger getLog() {
		return log;
	}

	/**
	 * A logger collecting logs for display in UI.
	 */
	public static class JsfLogger {
		private StringWriter stringWriter = new StringWriter();
		private PrintWriter printWriter = new PrintWriter(stringWriter);

		/**
		 * Get the collected log messages.
		 *
		 * @return
		 */
		public String getMessage() {
			return stringWriter != null ? stringWriter.toString() : "";
		}

		/**
		 * Log a formatted line.
		 *
		 * @param level
		 * @param format
		 * @param params
		 */
		public void log(Level level, String format, Object...params) {
			printWriter.print("%s: ".formatted(level));

			if(params.length > 0) {
				LOG.log(level, format, params);
				printWriter.println(MessageFormat.format(format, params));
			}
			else {
				LOG.log(level, format);
				printWriter.println(format);
			}
		}

		/**
		 * Log an informational message.
		 *
		 * @param format
		 * @param params
		 */
		public void info(String format, Object...params) {
			log(Level.INFO, format, params);
		}

		/**
		 * Log a warning.
		 *
		 * @param format
		 * @param params
		 */
		public void warn(String format, Object...params) {
			log(Level.WARN, format, params);
		}

		/**
		 * Log an error with an exception trace.
		 *
		 * @param format
		 * @param t
		 * @param params
		 */
		public void error(String format, Throwable t, Object...params) {
			log(Level.ERROR, format, params);
			log(Level.ERROR, ExceptionUtils.getStackTrace(t));
		}


		public void clearLog() {
			stringWriter = new StringWriter();
			printWriter = new PrintWriter(stringWriter);
		}

		public void clearLog(ActionEvent event) {
			clearLog();
		}
	}
}
