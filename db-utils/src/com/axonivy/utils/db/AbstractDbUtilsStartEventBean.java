package com.axonivy.utils.db;

import org.eclipse.core.runtime.IProgressMonitor;

import com.axonivy.utils.db.resolver.DbUtilsResolver;
import com.axonivy.utils.db.services.ScriptService;

import ch.ivyteam.ivy.process.eventstart.AbstractProcessStartEventBean;
import ch.ivyteam.ivy.process.eventstart.IProcessStartEventBeanRuntime;
import ch.ivyteam.ivy.service.ServiceException;
import ch.ivyteam.log.Logger;

public abstract class AbstractDbUtilsStartEventBean extends AbstractProcessStartEventBean {

	public AbstractDbUtilsStartEventBean() {
		super(AbstractDbUtilsStartEventBean.class.getSimpleName(), "DbUtils startup bean");
	}

	public AbstractDbUtilsStartEventBean(String _name, String _description) {
		super(_name, _description);
	}

	public abstract DbUtilsResolver	getDbUtilsResolver();

	@Override
	public void initialize(IProcessStartEventBeanRuntime eventRuntime, String configuration) {
		super.initialize(eventRuntime, configuration);
		log().debug("Initializing with configuration: {0}", configuration);
		eventRuntime.setPollTimeInterval(0);
	}

	@Override
	public void start(IProgressMonitor monitor) throws ServiceException {
		log().info("Starting Db Utils for database ''{0}''.", getDbUtilsResolver().getDatabaseName());

		try {
			if(getDbUtilsResolver().isAutoupdateEnabled()) {
				log().info("Auto-Update is enabled for scripts URL ''{0}''.", getDbUtilsResolver().getScriptsUrl());
				ScriptService.get(getDbUtilsResolver()).runNecessary(false, s -> log().info("Executing: {0}", s.getName()));
			}
			else {
				log().info("Auto-Update is disabled.");
			}
		} catch (Exception e) {
			log().error("Error executing DB update scripts.", e);
		}

		log().info("Finished Db Utils for database {0}.", getDbUtilsResolver().getDatabaseName());

		super.start(monitor);
	}

	@Override
	public void stop(IProgressMonitor monitor) throws ServiceException {
		super.stop(monitor);
		log().info("Stopped");
	}

	protected Logger log() {
		return getEventBeanRuntime().getRuntimeLogLogger();
	}
}
