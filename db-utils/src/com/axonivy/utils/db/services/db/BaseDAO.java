package com.axonivy.utils.db.services.db;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.axonivy.utils.db.resolver.DbUtilsResolver;
import com.axonivy.utils.db.services.DatabaseService;

public class BaseDAO {
	protected DbUtilsResolver dbUtilsResolver;
	protected DatabaseService databaseService;
	private static final Map<DbUtilsResolver, BaseDAO> INSTANCES = new ConcurrentHashMap<>();

	protected BaseDAO(DbUtilsResolver dbUtilsResolver) {
		super();
		this.dbUtilsResolver = dbUtilsResolver;
		databaseService = DatabaseService.get(dbUtilsResolver);
	}

	/**
	 * Get DAO instance.
	 *
	 * @param dbUtilsResolver
	 * @return
	 */
	public static synchronized BaseDAO get(DbUtilsResolver dbUtilsResolver) {
		var dao = INSTANCES.get(dbUtilsResolver);
		if(dao == null) {
			dao = new ScriptDAO(dbUtilsResolver);
			INSTANCES.put(dbUtilsResolver, dao);
		}
		return dao;
	}

	public interface SQLFunction<T, R> {
		R apply(T t) throws Exception;
	}

	public <R> R statement(SQLFunction<Connection, R> function) {
		try (Connection connection = databaseService.getDatabaseConnection()) {
			return function.apply(connection);
		} catch (Exception e) {
			throw new RuntimeException("Error during statement.", e);
		}
	}

	public boolean tableExists(String name) {
		var tableExists = statement(c -> {
			var meta = c.getMetaData();
			var resultSet = meta.getTables(null, null, name.toUpperCase(), new String[] {"TABLE"});
			return resultSet.next();
		});

		return tableExists;
	}
}