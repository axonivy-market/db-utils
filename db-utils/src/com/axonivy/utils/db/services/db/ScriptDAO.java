package com.axonivy.utils.db.services.db;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;

import com.axonivy.utils.db.resolver.DbUtilsResolver;
import com.axonivy.utils.db.services.enums.Status;

/**
 * Data Access Object for script table.
 */
public class ScriptDAO extends BaseDAO {
	private static final Logger LOG = LogManager.getLogger(new MessageFormatMessageFactory());
	private static final Map<DbUtilsResolver, ScriptDAO> INSTANCES = new ConcurrentHashMap<>();

	protected ScriptDAO(DbUtilsResolver dbUtilsResolver) {
		super(dbUtilsResolver);
	}

	/**
	 * Get DAO instance.
	 *
	 * @param dbUtilsResolver
	 * @return
	 */
	public static synchronized ScriptDAO get(DbUtilsResolver dbUtilsResolver) {
		var dao = INSTANCES.get(dbUtilsResolver);
		if(dao == null) {
			dao = new ScriptDAO(dbUtilsResolver);
			INSTANCES.put(dbUtilsResolver, dao);
		}
		return dao;
	}

	/**
	 * Find all DB scripts sorted by name.
	 *
	 * @return
	 */
	public List<Script> findAllScripts() {
		var scriptList = new ArrayList<Script>();
		var sqlQuery = "SELECT * FROM %s".formatted(dbUtilsResolver.getScriptTableName());

		assertScriptTable();
		statement(c -> {
			var preparedStatement = c.prepareStatement(sqlQuery);
			LOG.info("Executing prepared statement: {0}", sqlQuery);

			var resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				var script = new Script();
				script.setName(resultSet.getString(dbUtilsResolver.getScriptNameColumn()));
				script.setStatus(Status.valueOf(resultSet.getString(dbUtilsResolver.getScriptStatusColumn())));
				script.setExecutedAt(toInstant(resultSet.getTimestamp(dbUtilsResolver.getScriptExecutedAtColumn())));
				script.setErrorCause(resultSet.getString(dbUtilsResolver.getScriptErrorCauseColumn()));
				script.setError(resultSet.getString(dbUtilsResolver.getScriptErrorColumn()));
				script.setScript(resultSet.getString(dbUtilsResolver.getScriptScriptColumn()));
				scriptList.add(script);
			}

			scriptList.sort(Comparator.comparing(Script::getName));
			return null;
		});

		return scriptList;
	}

	/**
	 * Save a single script.
	 *
	 * @param script
	 */
	public void saveScript(Script script) {
		var sqlQuery = "INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES(?, ?, ?, ?, ?, ?)".formatted(
				dbUtilsResolver.getScriptTableName(),
				dbUtilsResolver.getScriptNameColumn(),
				dbUtilsResolver.getScriptStatusColumn(),
				dbUtilsResolver.getScriptExecutedAtColumn(),
				dbUtilsResolver.getScriptErrorCauseColumn(),
				dbUtilsResolver.getScriptErrorColumn(),
				dbUtilsResolver.getScriptScriptColumn()
				);
		LOG.info("Executing prepared statement: {0}, values: (''{1}'', ''{2}'', ''{3}'', ''{4}'')",
				sqlQuery,
				script.getName(),
				script.getStatus(),
				script.getExecutedAt(),
				script.getErrorCause()
				);

		assertScriptTable();
		statement(c -> {
			var stmt = c.prepareStatement(sqlQuery);
			stmt.setString(1, script.getName());
			stmt.setString(2, script.getStatus().name());
			stmt.setTimestamp(3,  toTimestamp(script.getExecutedAt()));
			setClobOrNull(stmt, 4, script.getErrorCause());
			setClobOrNull(stmt, 5, script.getError());
			setClobOrNull(stmt, 6, script.getScript());
			stmt.executeUpdate();
			return null;
		});
	}

	/**
	 * Update a single script.
	 *
	 * @param script
	 */
	public void updateScript(Script script) {
		var sqlQuery = "UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ?".formatted(
				dbUtilsResolver.getScriptTableName(),
				dbUtilsResolver.getScriptStatusColumn(),
				dbUtilsResolver.getScriptExecutedAtColumn(),
				dbUtilsResolver.getScriptErrorCauseColumn(),
				dbUtilsResolver.getScriptErrorColumn(),
				dbUtilsResolver.getScriptScriptColumn(),
				dbUtilsResolver.getScriptNameColumn()

				);
		LOG.info("Executing prepared statement: {0}, values: (''{1}'', ''{2}'', ''{3}'', ''{4}'')",
				sqlQuery,
				script.getStatus(),
				script.getExecutedAt(),
				script.getErrorCause(),
				script.getName()
				);

		assertScriptTable();
		statement(c -> {
			var stmt = c.prepareStatement(sqlQuery);
			stmt.setString(1, script.getStatus().name());
			stmt.setTimestamp(2, toTimestamp(script.getExecutedAt()));
			setClobOrNull(stmt, 3, script.getErrorCause());
			setClobOrNull(stmt, 4, script.getError());
			setClobOrNull(stmt, 5, script.getScript());
			stmt.setString(6, script.getName());
			stmt.executeUpdate();
			return null;
		});
	}

	/**
	 * Delete a single script.
	 *
	 * @param script
	 */
	public void deleteScript(Script script) {
		var sqlQuery = "DELETE FROM %s WHERE %s = ?".formatted(
				dbUtilsResolver.getScriptTableName(),
				dbUtilsResolver.getScriptNameColumn()

				);
		LOG.info("Executing prepared statement: {0}, values: (''{1}'')",
				sqlQuery,
				script.getName()
				);

		assertScriptTable();
		statement(c -> {
			var preparedStatement = c.prepareStatement(sqlQuery);
			preparedStatement.setString(1, script.getName());
			preparedStatement.executeUpdate();
			return null;
		});
	}

	protected Timestamp toTimestamp(Instant instant) {
		return instant != null ? Timestamp.from(instant) : null;
	}

	protected Instant toInstant(Timestamp timestamp) {
		return timestamp != null ? timestamp.toInstant() : null;
	}

	protected InputStream toInputStream(String string) {
		return string != null ? new ByteArrayInputStream(string.getBytes()) : null;
	}

	protected StringReader toReader(String string) {
		return string != null ? new StringReader(string) : null;
	}

	protected void setClobOrNull(PreparedStatement stmt, int index, String value) throws SQLException {
		if(value == null) {
			stmt.setNull(index, java.sql.Types.CLOB);
		}
		else {
			stmt.setClob(index, new StringReader(value));
		}
	}

	protected void assertScriptTable() {
		if (!scriptTableExists()) {
			createScriptTable();
			if(!scriptTableExists()) {
				throw new RuntimeException("Could not create script table.");
			}
		}
	}

	protected boolean scriptTableExists() {
		var tableExists = tableExists(dbUtilsResolver.getScriptTableName());

		if (tableExists) {
			LOG.debug("Table {0} exists.", dbUtilsResolver.getScriptTableName());
		} else {
			LOG.info("Table {0} does not exist.", dbUtilsResolver.getScriptTableName());
		}

		return tableExists;
	}

	protected void createScriptTable() {
		statement(c -> {
			var sqlQuery = dbUtilsResolver.getScriptTableCreateStatement();
			var preparedStatement = c.prepareStatement(sqlQuery);
			LOG.info("Executing prepared statement: {0}", sqlQuery);
			preparedStatement.executeUpdate();
			return null;
		});
	}

	protected void dropScriptTable() {
		statement(c -> {
			var sqlQuery = dbUtilsResolver.getScriptTableDropStatement();
			var preparedStatement = c.prepareStatement(sqlQuery);
			LOG.info("Executing prepared statement: {0}", sqlQuery);
			preparedStatement.executeUpdate();
			return null;
		});
	}
}
