package com.axonivy.utils.db.resolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;

import com.axonivy.utils.db.services.db.Script;
import com.axonivy.utils.db.services.enums.Status;
import com.axonivy.utils.db.services.records.TypedPath;

import ch.ivyteam.ivy.environment.Ivy;

/**
 * Helper to simplify configuration of resolvers.
 * 
 * <p>
 * The helper gets some of the configuration from global variables (if they are set).
 * This behavior can be overridden in inherited classes.
 * </p>
 * 
 * <p>
 * @apiNote Resources like SQL scriptts will be loaded in the context (classloader) of the
 *          class which is extending this class! This means, that you must
 *          <b>override the class in the project which defines your resources!</b>
 *          
 * </p>
 */
public abstract class AbstractDbUtilsResolver implements DbUtilsResolver {
	private static final Logger LOG = LogManager.getLogger(new MessageFormatMessageFactory());
	private static final String VAR_BASE = "com.axonivy.utils.db";
	private static final String DELIMITER = ";";
	private static final Pattern EMPTY_LINE_PATTERN = Pattern.compile("^\\s*$");

	@Override
	public String getDatabaseName() {
		return getVar("database");
	}

	@Override
	public String getScriptsUrl() {
		return getVar("scriptsurl");
	}

	@Override
	public String getDataUrl() {
		return getVar("dataurl");
	}

	@Override
	public String getLiquibaseChangelog() {
		return getVar("liquibasechangelog");
	}

	@Override
	public boolean isAutoupdateEnabled() {
		return getBoolean("autoupdate", true);
	}

	@Override
	public boolean isIncrementalTabEnabled() {
		return getBoolean("incrementaltab", true);
	}

	@Override
	public boolean isLiquibaseTabEnabled() {
		return getBoolean("liquibasetab", true);
	}

	@Override
	public boolean isSqlStatementTabEnabled() {
		return getBoolean("sqlstatementtab", true);
	}

	@Override
	public boolean isExcelExportImportTabEnabled() {
		return getBoolean("excelexportimporttab", true);
	}

	@Override
	public String getDelimiter() {
		return DELIMITER;
	}

	@Override
	public boolean isExcelExportLob(Column column) {
		return column.getDataType() == DataType.VARBINARY;
	}

	@Override
	public String[] getExportExcludeTableNames() {
		return new String[] {getScriptTableName()};
	}

	/**
	 * Find available scripts (where script source file is available).
	 *
	 * @return
	 */
	@Override
	public List<Script> findAvailableScripts() {
		List<Script> scripts = List.of();

		var typedPath = TypedPath.create(getScriptsUrl());

		switch(typedPath.pathType()) {
		case CLASSPATH:
			scripts = findClasspathScripts(typedPath);
			break;
		case FILE:
			scripts = findFileScripts(typedPath);
			break;
		default:
			throw new RuntimeException("Cannot work with protocol '%s'.".formatted(typedPath.url()));
		}

		return scripts;

	}

	@Override
	public String readScript(Script script) {
		String result = null;
		var typedPath = TypedPath.create(script.getName());
		try {
			switch(typedPath.pathType()) {
			case CLASSPATH:
				try (var resourceStream = getClass().getResourceAsStream(typedPath.path())) {
					result = prepareScript(resourceStream);
				}
				break;
			case FILE:
				try(var stream = new FileInputStream(Paths.get(new URI(script.getName())).toFile())) {
					result = prepareScript(stream);
				}
				break;
			default:
				throw new RuntimeException("Typed path '%s' cannot be loaded.".formatted(typedPath.url()));
			}
		} catch(Exception e) {
			throw new RuntimeException("Could not read script '%s'".formatted(script.getName()), e);
		}
		return result;
	}

	/**
	 * Find scripts in a classpath directory.
	 *
	 * @param typedPath
	 *
	 * @return
	 */
	public List<Script> findClasspathScripts(TypedPath typedPath) {
		var names = findScriptNamesInClasspath(typedPath.path());
		names = names.stream().map(n -> typedPath.build(n)).collect(Collectors.toCollection(ArrayList::new));
		var scripts = names.stream().map(n -> createScript(n)).collect(Collectors.toCollection(ArrayList::new));
		return scripts;
	}

	/**
	 * Find scripts in the filesystem.
	 *
	 * @param typedPath
	 * @return
	 */
	public List<Script> findFileScripts(TypedPath typedPath) {
		var result = new ArrayList<Script>();
		try {
			var url = URI.create(typedPath.url()).toURL();
			var baseDir = new File(url.getPath());
			if(!baseDir.isDirectory()) {
				throw new RuntimeException("Script URL is not a directory: '%s'".formatted(url.getPath()));
			}
			var path = baseDir.toPath();
			try (var stream = Files.newDirectoryStream(path, "*.sql")) {
				StreamSupport.stream(stream.spliterator(), false)
				.map(p -> toFilePath(typedPath, p))
				.sorted()
				.distinct()
				.forEach(n -> result.add(createScript(n)));
			};
		} catch (IOException e) {
			throw new RuntimeException("Error while trying to find SQL scripts in path: '%s'".formatted(typedPath.url()), e);		}
		return result;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getDatabaseName());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return Objects.equals(getDatabaseName(), ((AbstractDbUtilsResolver) obj).getDatabaseName());
	}

	protected String getVar(String name) {
		var value = Ivy.var().get("%s.%s".formatted(VAR_BASE, name));
		return StringUtils.isEmpty(value) ? null : value.trim();
	}

	protected boolean getBoolean(String name, boolean def) {
		var value = getVar(name);
		return value == null ? def : BooleanUtils.toBoolean(value);
	}

	/**
	 * Create a script by it's name, reading it's file.
	 *
	 * @param name
	 * @return
	 */
	protected Script createScript(String name) {
		var script = new Script();
		script.setName(name);
		script.setStatus(Status.NONE);
		script.setScript(readScript(script));
		return script;
	}

	/**
	 * Read and prepare a script from a stream.
	 *
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected String prepareScript(InputStream stream) throws IOException {
		String result = null;
		try (var in = new BufferedReader(new InputStreamReader(stream))) {
			var lines = in.lines();
			if(delimitEmptyLines()) {
				// add empty line
				lines = Stream.concat(lines, Stream.of(""));
			}
			result = lines
					.map(l -> delimitEmptyLines() && EMPTY_LINE_PATTERN.matcher(l).matches() ? DELIMITER : l)
					.collect(Collectors.joining(System.lineSeparator()));
		}
		return result;
	}

	/**
	 * Find names of all scripts in a classpath directory.
	 *
	 * @param scriptsUrl
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	protected List<String> findScriptNamesInClasspathDirectory(String scriptsUrl, String filePath) throws IOException {
		var result = new ArrayList<String>();
		var baseDir = new File(filePath);
		if(!baseDir.isDirectory()) {
			throw new RuntimeException("Script path is not a directory: '%s'".formatted(filePath));
		}
		var path = baseDir.toPath();
		try (var stream = Files.newDirectoryStream(path, "*.sql")) {
			StreamSupport.stream(stream.spliterator(), false)
			.map(p -> toClassPath(scriptsUrl, p))
			.sorted()
			.distinct()
			.forEach(n -> result.add(n));
		};
		return result;
	}

	/**
	 * @param scriptsUrl
	 * @param urlPath
	 * @return
	 * @throws IOException
	 */
	protected List<String> findScriptNamesInClasspathZip(String scriptsUrl, String urlPath) throws IOException {
		var result = new ArrayList<String>();
		var iarUrl = URI.create(urlPath).toURL();
		var iarResourcePath = iarUrl.getPath();
		LOG.info("iar resource: ''{0}''", iarResourcePath);
		var parts = iarResourcePath.split("!", 2);
		var iarPath = parts[0];
		var zipPath = parts[1];
		LOG.info("iar: ''{0}'' zip: ''{1}''", iarPath, zipPath);

		try (var zipFile = new ZipFile(iarPath)) {
			zipFile.stream()
			.map(e -> e.getName())
			.filter(n -> n.endsWith(".sql"))
			.map(n -> Paths.get(n))
			.filter(p -> p.toString().endsWith(Paths.get(scriptsUrl, p.getFileName().toString()).toString()))
			.map(p -> toClassPath(scriptsUrl, p))
			.sorted()
			.distinct()
			.forEach(n -> result.add(n));
		}
		return result;
	}

	/**
	 * Find scripts in the classpath.
	 *
	 * If the resource is a jar file, search in the jar zip, otherwise search in file-system.
	 *
	 * @param scriptsUrl
	 * @return
	 */
	protected List<String> findScriptNamesInClasspath(String scriptsUrl) {
		List<String> result = null;
		try {
			//		file:/C:/dev/fwf/ws/db-utils-test/target/classes/sql/incremental
			//		jar:file:///usr/lib/axonivy-engine/applications/DbUtilsTest/db-utils-test/1.iar!/target/classes/sql/incremental
			LOG.info("SqlBase: {0}", scriptsUrl);
			var resource = getClass().getResource(scriptsUrl);
			if(resource == null) {
				throw new RuntimeException("Could not find sql base directory '%s'".formatted(scriptsUrl));
			}
			LOG.info("Finding list of resources");
			switch(resource.getProtocol()) {
			case "file":
				result = findScriptNamesInClasspathDirectory(scriptsUrl, resource.getPath());
				break;
			case "jar":
				result = findScriptNamesInClasspathZip(scriptsUrl, resource.getPath());
				break;
			default:
				LOG.error("Unhandled protocol: {0}", resource.getProtocol());
			}
		} catch (IOException e) {
			throw new RuntimeException("Error while trying to find SQL scripts in resource base path: '%s'".formatted(scriptsUrl), e);
		}
		result = result != null ? result : new ArrayList<>();

		// make sure, we only have working resources
		result = result.stream()
				.filter(n -> getClass().getResource(n) != null)
				.collect(Collectors.toCollection(ArrayList::new));

		return result;
	}

	protected String toFilePath(TypedPath typedPath, Path path) {
		try {
			var builder = new URIBuilder(typedPath.url());
			var segments = builder.getPathSegments();
			segments.add(path.getFileName().toString());
			builder.setPathSegments(segments);
			var result = builder.build().toString();
			return result;
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not build file URL for typedPath %s and Path %s.".formatted(typedPath, path));
		}
	}

	protected String toClassPath(String basePath, Path path) {
		return (basePath + "/" + path.getFileName().toString()).replaceAll("//+", "/");
	}
}
