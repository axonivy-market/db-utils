package com.axonivy.utils.db.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;

import com.axonivy.utils.db.resolver.DbUtilsResolver;

import liquibase.logging.core.JavaLogService;

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
