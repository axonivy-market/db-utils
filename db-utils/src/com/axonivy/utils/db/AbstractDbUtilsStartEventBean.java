package com.axonivy.utils.db;

import com.axonivy.utils.db.resolver.DbUtilsResolver;
import com.axonivy.utils.db.services.ScriptService;

import ch.ivyteam.ivy.process.eventstart.AbstractProcessStartEventBean;
import ch.ivyteam.ivy.process.eventstart.IProcessStartEventBeanRuntime;
import ch.ivyteam.ivy.process.extension.ProgramConfig;
import ch.ivyteam.ivy.service.ServiceException;
import ch.ivyteam.log.Logger;

public abstract class AbstractDbUtilsStartEventBean extends AbstractProcessStartEventBean {

	private DbUtilsResolver dbUtilsResolver;

	/**
	 * Construct a start event bean with a unique name.
	 * 
	 * @param _name           unique name for this singleton of the start even bean
	 *                        (you could use the canonical name of your class)
	 * @param dbUtilsResolver the resolver to use
	 */
	public AbstractDbUtilsStartEventBean(String _name, DbUtilsResolver dbUtilsResolver) {
		super(_name, "DbUtils startup bean for database '%s'".formatted(dbUtilsResolver.getDatabaseName()));
		this.dbUtilsResolver = dbUtilsResolver;
	}

	/**
	 * Construct a start event bean with a unique name.
	 * 
	 * @param clazz           to identify this unique type of
	 *                        {@link AbstractDbUtilsStartEventBean}
	 * @param dbUtilsResolver the resolver to use
	 */
	public AbstractDbUtilsStartEventBean(Class<? extends AbstractDbUtilsStartEventBean> clazz,
			DbUtilsResolver dbUtilsResolver) {
		super(clazz.getCanonicalName(),
				"DbUtils startup bean for database '%s'".formatted(dbUtilsResolver.getDatabaseName()));
		this.dbUtilsResolver = dbUtilsResolver;
	}

	/**
	 * Construct a start event bean.
	 * 
	 * @param dbUtilsResolver the resolver to use
	 */
	public AbstractDbUtilsStartEventBean(DbUtilsResolver dbUtilsResolver) {
		super("DbUtilStartEventBean-%s".formatted(dbUtilsResolver.getDatabaseName()),
				"DbUtils startup bean for database '%s'".formatted(dbUtilsResolver.getDatabaseName()));
		this.dbUtilsResolver = dbUtilsResolver;
	}

	public DbUtilsResolver getDbUtilsResolver() {
		return dbUtilsResolver;
	}

	@Override
	public void initialize(IProcessStartEventBeanRuntime eventRuntime, ProgramConfig configuration) {
		super.initialize(eventRuntime, configuration);
		log().debug("Initializing with configuration: {0}", configuration);
		eventRuntime.poll().disable();
	}

	@Override
	public void start() throws ServiceException {
		log().info("Starting Db Utils for database ''{0}''.", getDbUtilsResolver().getDatabaseName());

		try {
			if (getDbUtilsResolver().isAutoupdateEnabled()) {
				log().info("Auto-Update is enabled for scripts URL ''{0}''.", getDbUtilsResolver().getScriptsUrl());
				ScriptService.get(getDbUtilsResolver()).runNecessary(false,
						s -> log().info("Executing: {0}", s.getName()));
			} else {
				log().info("Auto-Update is disabled.");
			}
		} catch (Exception e) {
			log().error("Error executing DB update scripts.", e);
		}

		log().info("Finished Db Utils for database {0}.", getDbUtilsResolver().getDatabaseName());
	}

	@Override
	public void stop() throws ServiceException {
		log().info("Stopped");
	}

	protected Logger log() {
		return getEventBeanRuntime().getRuntimeLogLogger();
	}
}
