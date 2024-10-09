package com.axonivy.utils.db.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.excel.XlsDataSetBlob;
import org.dbunit.dataset.excel.XlsDataSetWriter;
import org.dbunit.dataset.excel.XlsDataSetWriterBlob;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.ExcludeTableFilter;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;

import com.axonivy.utils.db.resolver.DbUtilsResolver;

import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.db.IExternalDatabase;
import ch.ivyteam.ivy.db.IExternalDatabaseManager;

/**
 * A database service.
 */
public class DatabaseService {
	private static final Logger LOG = LogManager.getLogger(new MessageFormatMessageFactory());
	private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:([^:]+):(.*)");
	private static final Map<String, Class<?>> DATATYPE_FACTORY = Map.of(
			"hsqldb", HsqldbDataTypeFactory.class,
			"sqlserver", MsSqlDataTypeFactory.class
			);
	private static final Map<DbUtilsResolver, DatabaseService> INSTANCES = new ConcurrentHashMap<>();

	protected DbUtilsResolver dbUtilsResolver;

	protected DatabaseService(DbUtilsResolver dbUtilsResolver) {
		this.dbUtilsResolver = dbUtilsResolver;
	}

	/**
	 * Get service instance.
	 *
	 * @param dbUtilsResolver
	 * @return
	 */
	public static synchronized DatabaseService get(DbUtilsResolver dbUtilsResolver) {
		var databaseService = INSTANCES.get(dbUtilsResolver);
		if(databaseService == null) {
			databaseService = new DatabaseService(dbUtilsResolver);
			INSTANCES.put(dbUtilsResolver, databaseService);
		}
		return databaseService;
	}

	/**
	 * @return the dbUtilsResolver
	 */
	public DbUtilsResolver getDbUtilsResolver() {
		return dbUtilsResolver;
	}

	/**
	 * Get a configured database from Ivy.
	 *
	 * @param name
	 * @return
	 */
	public IExternalDatabase getExternalDatabase() {
		return IExternalDatabaseManager.instance()
				.getExternalDatabaseApplicationContext(IApplication.current())
				.getExternalDatabase(dbUtilsResolver.getDatabaseName());
	}

	/**
	 * Get a connection to the specified database.
	 *
	 * @return
	 * @throws SQLException
	 */
	public Connection getDatabaseConnection() throws SQLException {
		var db = getExternalDatabase();

		var connection = db.getConnection();
		connection.setAutoCommit(true);

		return connection;
	}

	/**
	 * Get a connection to the specified database.
	 *
	 * @return
	 * @throws DatabaseUnitException
	 * @throws SQLException
	 */
	public AutoCloseableConnection getDbUnitConnection() throws DatabaseUnitException, SQLException {
		IExternalDatabase db = getExternalDatabase();

		Connection connection = db.getConnection();
		connection.setAutoCommit(true);

		DatabaseConnection databaseConnection = new DatabaseConnection(connection);

		DatabaseConfig config = databaseConnection.getConfig();
		config.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, false);
		config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);

		Matcher matcher = JDBC_URL_PATTERN.matcher(connection.getMetaData().getURL());

		if(matcher.matches()) {
			Class<?> clazz = DATATYPE_FACTORY.get(matcher.group(1));
			if(clazz != null) {
				LOG.info("Setting data type factory to {0}", clazz);
				try {
					config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, clazz.getConstructor().newInstance());
				} catch (Exception e) {
					LOG.error("Could not instantiate data type factory class {0}", e, clazz);
				}
			}
		}

		return new AutoCloseableConnection(databaseConnection);
	}

	/**
	 * Export all tables to Excel.
	 *
	 * @return
	 * @throws Exception
	 */
	public InputStream exportXls() throws Exception {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new byte[0]);
		try (AutoCloseableConnection databaseConnection = getDbUnitConnection()) {

			IDataSet dataSet = databaseConnection.get().createDataSet();

			dataSet = new FilteredDataSet(new ExcludeTableFilter(dbUtilsResolver.getExportExcludeTableNames()), dataSet);
			dataSet = new FilteredDataSet(new DatabaseSequenceFilter(databaseConnection.get()), dataSet);

			ByteArrayOutputStream oStream = new ByteArrayOutputStream();
			new XlsDataSetWriter().write(dataSet, oStream);

			iStream = new ByteArrayInputStream(oStream.toByteArray());
		}
		return iStream;
	}

	/**
	 * Export all tables to Excel and all templates from DB, which were stored as lobs
	 *
	 * @return
	 * @throws Exception
	 */
	public InputStream exportZip() throws Exception {
		ByteArrayInputStream iStream = new ByteArrayInputStream(new byte[0]);
		try (AutoCloseableConnection databaseConnection = getDbUnitConnection()) {

			var dataSet = databaseConnection.get().createDataSet();

			dataSet = new FilteredDataSet(new ExcludeTableFilter(dbUtilsResolver.getExportExcludeTableNames()), dataSet);
			dataSet = new FilteredDataSet(new DatabaseSequenceFilter(databaseConnection.get()), dataSet);

			var bos = new ByteArrayOutputStream();
			try (var zipOut = new ZipOutputStream(bos)) {
				var writer = new XlsDataSetWriterBlob(dbUtilsResolver);
				writer.write(dataSet, zipOut);
			}
			finally {
				bos.close();
				iStream = new ByteArrayInputStream(bos.toByteArray());
			}
		}
		return iStream;
	}

	/**
	 * Create a new {@link IDataSet} from another {@link IDataSet} and exclude some columns.
	 *
	 * @param dataSet
	 * @param excludeColumns
	 * @return
	 * @throws DataSetException
	 */
	public IDataSet columnFilteredDataSet(IDataSet dataSet, String...excludeColumns) throws DataSetException {

		DefaultDataSet filtered = new DefaultDataSet(dataSet.isCaseSensitiveTableNames());

		ITableIterator tableIt = dataSet.iterator();
		while (tableIt.next()) {
			filtered.addTable(DefaultColumnFilter.excludedColumnsTable(tableIt.getTable(), excludeColumns));
		}
		return filtered;
	}

	/**
	 * Import specified tables from an Excel data-set.
	 *
	 * @param clean
	 * @param dataSet
	 * @throws Exception
	 */
	public void importData(boolean clean, IDataSet dataSet) throws Exception {
		try(AutoCloseableConnection databaseConnection = getDbUnitConnection()) {
			DatabaseOperation dbOp = clean ? DatabaseOperation.CLEAN_INSERT : DatabaseOperation.INSERT;

			dbOp.execute(databaseConnection.get(), dataSet);
			databaseConnection.get().getConnection().commit();
		}
	}


	/**
	 * Clean all tables.
	 *
	 * @param dataSet
	 * @throws Exception
	 */
	public void deleteData(IDataSet dataSet) throws Exception {
		try(AutoCloseableConnection databaseConnection = getDbUnitConnection()) {
			DatabaseOperation.DELETE_ALL.execute(databaseConnection.get(), dataSet);
			databaseConnection.get().getConnection().commit();
		}
	}

	/**
	 * Import an Excel file.
	 *
	 * @param clean
	 * @param handleClasspathBlobs text starting with "classpath:" will be searched as resources in the classpath and inserted as blobs
	 * @param stream
	 * @throws Exception
	 */
	public void importExcelData(boolean clean, boolean handleClasspathBlobs, InputStream stream) throws Exception {
		importData(clean, handleClasspathBlobs ? new XlsDataSetBlob(stream, dbUtilsResolver) : new XlsDataSet(stream));
	}

	/**
	 * Delete all data in tables mentioned in initial data-set.
	 *
	 * @param resourcePath
	 * @throws Exception
	 */
	public void deleteAllData(String resourcePath) throws Exception {
		try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
			deleteData(new XlsDataSet(stream));
		}
	}

	public class AutoCloseableConnection implements AutoCloseable {

		private DatabaseConnection databaseConnection;

		private AutoCloseableConnection(DatabaseConnection conn) {
			this.databaseConnection = conn;
		}

		public DatabaseConnection get() {
			return databaseConnection;
		}

		@Override
		public void close() throws Exception {
			databaseConnection.close();
		}
	}
}
