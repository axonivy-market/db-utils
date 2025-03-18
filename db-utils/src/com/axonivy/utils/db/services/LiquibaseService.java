package com.axonivy.utils.db.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;

import com.axonivy.utils.db.resolver.DbUtilsResolver;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.logging.core.JavaLogService;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * The Liquibase service.
 */
public class LiquibaseService {
	private static final Logger LOG = LogManager.getLogger(new MessageFormatMessageFactory());
	private static final Map<DbUtilsResolver, LiquibaseService> INSTANCES = new ConcurrentHashMap<>();

	protected DbUtilsResolver dbUtilsResolver;
	protected DatabaseService databaseService;

	protected LiquibaseService(DbUtilsResolver dbUtilsResolver) {
		this.dbUtilsResolver = dbUtilsResolver;
		this.databaseService = DatabaseService.get(dbUtilsResolver);
	}

	/**
	 * Get service instance.
	 *
	 * @param dbUtilsResolver
	 * @return
	 */
	public static synchronized LiquibaseService get(DbUtilsResolver dbUtilsResolver) {
		var liquibaseService = INSTANCES.get(dbUtilsResolver);
		if(liquibaseService == null) {
			liquibaseService = new LiquibaseService(dbUtilsResolver);
			INSTANCES.put(dbUtilsResolver, liquibaseService);
		}
		return liquibaseService;
	}


	/**
	 * Perform Liquibase update.
	 * 
	 * @param contexts
	 */
	public void update(String contexts) {
		var changelog = dbUtilsResolver.getLiquibaseChangelog();
		execute(() -> {
			return execute(dbUtilsResolver.getClass(), () -> {
				try (var connection = databaseService.getDatabaseConnection();
						var liqCon = new JdbcConnection(connection);
						var liquibase = new Liquibase(changelog, new ClassLoaderResourceAccessor(), liqCon);
						) {
					liquibase.update("");

				} catch (Exception e) {
					throw new RuntimeException("Exception while executing Liquibase update for changelog '%s'.".formatted(changelog), e);
				}
				return null;
			});
		});
	}

	/**
	 * Perform Liquibase update.
	 * 
	 */
	public void update() {
		update("");
	}

	/**
	 * Execute a function in the classloader context of Liquibase.
	 * 
	 * @param <R>
	 * @param function
	 * @return
	 */
	public <R> R execute(Supplier<R> function) {
		return execute(JavaLogService.class, function);
	}

	/**
	 * Execute a function in the classloader context of the given class.
	 * 
	 * @param <R>
	 * @param clazz
	 * @param function
	 * @return
	 */
	public <R> R execute(Class<?> clazz, Supplier<R> function) {
		var thread = Thread.currentThread();
		var contextClassLoader = thread.getContextClassLoader();

		try {
			thread.setContextClassLoader(clazz.getClassLoader());
			return function.get();
		}
		finally {
			thread.setContextClassLoader(contextClassLoader);
		}
	}
}
