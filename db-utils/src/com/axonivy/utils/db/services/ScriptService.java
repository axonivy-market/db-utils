package com.axonivy.utils.db.services;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;

import com.axonivy.utils.db.resolver.DbUtilsResolver;
import com.axonivy.utils.db.services.db.Script;
import com.axonivy.utils.db.services.db.ScriptDAO;
import com.axonivy.utils.db.services.enums.Status;
import com.axonivy.utils.db.services.records.AvailableScripts;
import com.google.common.base.Objects;

/**
 * Service to work with DB SQL scripts.
 */
public class ScriptService {
	private static final Logger LOG = LogManager.getLogger(new MessageFormatMessageFactory());
	private static final Map<DbUtilsResolver, ScriptService> INSTANCES = new ConcurrentHashMap<>();

	private DbUtilsResolver dbUtilsResolver;
	private DatabaseService databaseService;
	private ScriptDAO scriptDao;

	private ScriptService(DbUtilsResolver dbUtilsResolver) {
		this.dbUtilsResolver = dbUtilsResolver;
		this.databaseService = DatabaseService.get(dbUtilsResolver);
		this.scriptDao = ScriptDAO.get(dbUtilsResolver);
	}

	/**
	 * Get service instance.
	 *
	 * @param dbUtilsResolver
	 * @return
	 */
	public static synchronized ScriptService get(DbUtilsResolver dbUtilsResolver) {
		var scriptService = INSTANCES.get(dbUtilsResolver);
		if(scriptService == null) {
			scriptService = new ScriptService(dbUtilsResolver);
			INSTANCES.put(dbUtilsResolver, scriptService);
		}
		return scriptService;
	}

	/**
	 * @return the dbUtilsResolver
	 */
	public DbUtilsResolver getDbUtilsResolver() {
		return dbUtilsResolver;
	}

	/**
	 * @return the scriptDao
	 */
	public ScriptDAO getScriptDao() {
		return scriptDao;
	}

	/**
	 * Find available and unavailable files.
	 *
	 * @return
	 */
	public AvailableScripts updateScripts() {
		var availableScripts = dbUtilsResolver.findAvailableScripts();
		var dbScripts = addNewToDbScripts(availableScripts);
		var unavailableScripts = diff(dbScripts, availableScripts);
		availableScripts = diff(dbScripts, unavailableScripts);
		return new AvailableScripts(availableScripts, unavailableScripts);
	}

	/**
	 * Find scripts contained in the database.
	 *
	 * @return
	 */
	public List<Script> findDbScripts() {
		return getScriptDao().findAllScripts();
	}

	/**
	 * Add new available scripts to DB and return updated DB scripts.
	 *
	 * @param available
	 */
	public List<Script> addNewToDbScripts(List<Script> available) {
		var dbScripts = findDbScripts();
		var addDb = diff(available, dbScripts);
		addDb.forEach(s -> getScriptDao().saveScript(s));

		return Stream.concat(dbScripts.stream(), addDb.stream())
				.sorted(Comparator.comparing(Script::getName))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Get a list of elements contained in first, but not in second.
	 *
	 * @param first
	 * @param second
	 * @return
	 */
	public List<Script> diff(List<Script> first, List<Script> second) {
		var secondNames = second.stream()
				.map(Script::getName)
				.collect(Collectors.toSet());
		return first.stream()
				.filter(s -> !secondNames.contains(s.getName()))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Run a single script.
	 *
	 * @param script
	 * @throws Exception
	 */
	public void runScript(Script script) throws Exception {
		try (var connection = databaseService.getDatabaseConnection()) {
			try {
				script.setScript(dbUtilsResolver.readScript(script));
				script.setExecutedAt(Instant.now());

				var scriptRunner = createScriptRunner(connection);
				scriptRunner.runScript(new StringReader(script.getScript()));
				connection.commit();

				script.setErrorCause(null);
				script.setError(null);
				script.setStatus(Status.DONE);
			} catch (Exception e) {
				LOG.error("Error while executing script", e);
				connection.rollback();
				script.setErrorCause(ExceptionUtils.getRootCauseMessage(e));
				script.setError(ExceptionUtils.getStackTrace(e));
				script.setStatus(Status.ERROR);
				throw e;
			} finally {
				try {
					getScriptDao().updateScript(script);
				} catch (Exception e) {
					LOG.error("Error while saving script result", e);
				}
			}
		}
	}

	/**
	 * Run a single script.
	 * 
	 * @param stream
	 * @throws Exception
	 */
	public void runScript(InputStream stream) throws Exception {
		try (var connection = databaseService.getDatabaseConnection()) {
			var scriptRunner = createScriptRunner(connection);
			scriptRunner.runScript(new InputStreamReader(stream));
			connection.commit();
		}
	}

	protected ScriptRunner createScriptRunner(Connection connection) throws SQLException {
		var scriptRunner = new ScriptRunner(connection);
		scriptRunner.setAutoCommit(false);
		scriptRunner.setStopOnError(true);
		scriptRunner.setSendFullScript(false);
		scriptRunner.setLogWriter(null);
		scriptRunner.setDelimiter(dbUtilsResolver.getDelimiter());
		scriptRunner.setFullLineDelimiter(false);

		return scriptRunner;
	}

	/**
	 * Run all scripts which are not done.
	 *
	 * @param scripts
	 * @param forced
	 * @param scriptConsumer perform some action for every script (e.g. logging)
	 * @throws Exception
	 */
	public void runNecessary(List<Script> scripts, boolean forced, Consumer<Script> scriptConsumer) throws Exception {
		for (var script : scripts) {
			if(script.getStatus() != Status.DONE && script.getStatus() != Status.DISABLED) {
				if(scriptConsumer != null) {
					scriptConsumer.accept(script);
				}
				try {
					runScript(script);
				} catch (Exception e) {
					if(forced) {
						LOG.info("Forcing continuation of scripts even on errors.");
					}
					else {
						throw e;
					}
				}
			}
		}
	}

	/**
	 * Run all scripts which are available and not done.
	 *
	 * @param forced
	 * @param scriptConsumer perform some action for every script (e.g. logging)
	 * @throws Exception
	 */
	public void runNecessary(boolean forced, Consumer<Script> scriptConsumer) throws Exception {
		var scripts = updateScripts();
		runNecessary(scripts.available(), forced, scriptConsumer);
	}

	/**
	 * Refresh script text from file and update status to NONE if not disabled.
	 *
	 * @param script
	 */
	public void refresh(Script script) {
		var current = dbUtilsResolver.readScript(script);
		if(!Objects.equal(script.getScript(), current)) {
			script.setScript(current);
			if(script.getStatus() != Status.DISABLED) {
				script.setStatus(Status.NONE);
			}
			getScriptDao().updateScript(script);
		}
	}
}
