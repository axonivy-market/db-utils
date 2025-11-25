package com.axonivy.utils.db.controller;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.axonivy.utils.db.controller.DbUtilsController.JsfLogger;
import com.axonivy.utils.db.services.ScriptService;
import com.axonivy.utils.db.services.db.Script;
import com.axonivy.utils.db.services.enums.Status;

/**
 * Controller for a script table.
 */
public class ScriptTableController {
	private boolean available;
	private List<Script> scripts = new ArrayList<>();
	private ScriptService scriptService;
	private JsfLogger log;

	public ScriptTableController(boolean available, ScriptService scriptService, JsfLogger log) {
		this.available = available;
		this.scriptService = scriptService;
		this.log = log;
	}

	/**
	 * Run a single script.
	 *
	 * @param script
	 */
	public void run(Script script) {
		try {
			log.clearLog();
			log.info("Executing script: {0}", script.getName());
			scriptService.runScript(script);
		} catch (Exception e) {
			log.error("Error executing script {0}", e, script.getName());
		}
	}

	/**
	 * Run the scripts which were not yet executed successfully.
	 *
	 * @param forced force run, even if one fails
	 */
	public void runNecessary(boolean forced) {
		try {
			log.clearLog();
			log.info("Executing all necessary scripts");
			scriptService.runNecessary(scripts, forced,  s -> log.info("Executing: {0}", s.getName()));
		} catch (Exception e) {
			log.error("Error executing scripts.", e);
		}
	}

	/**
	 * Run the scripts which were not yet executed successfully.
	 */
	public void runNecessary() {
		runNecessary(false);

	}

	/**
	 * Force run the scripts which were not yet executed successfully.
	 */
	public void forceNecessary() {
		runNecessary(true);
	}

	/**
	 * Refresh a script source from it's source file.
	 *
	 * @param script
	 */
	public void refresh(Script script) {
		log.clearLog();
		log.info("Refreshing {0}", script.getName());
		scriptService.refresh(script);
	}

	/**
	 * Enable a script so it will be run automatically if necessary.
	 *
	 * @param script
	 */
	public void enable(Script script) {
		log.clearLog();
		log.warn("Enabling script: {0}", script.getName());
		script.setStatus(Status.NONE);
		scriptService.getScriptDao().updateScript(script);
	}

	/**
	 * Disable a script so it will no longer be run automatically.
	 *
	 * @param script
	 */
	public void disable(Script script) {
		log.clearLog();
		log.warn("Disabling script: {0}", script.getName());
		script.setStatus(Status.DISABLED);
		scriptService.getScriptDao().updateScript(script);
	}

	/**
	 * Delete script.
	 *
	 * @param script
	 */
	public void delete(Script script) {
		log.clearLog();
		log.warn("Deleting script: {0}", script.getName());
		scriptService.getScriptDao().deleteScript(script);
		scripts.remove(script);
	}

	/**
	 * Set script to {@link Status#DONE} manually and delete error.
	 *
	 * @param script
	 */
	public void setDone(Script script) {
		log.clearLog();
		setSingleScriptToDone(script);
	}

	public void fastForward(Script script) {
		log.clearLog();
		log.warn("Fast forward script and previous scripts status to DONE: {0}", script.getName());
		for (var listScript : scripts) {
			setSingleScriptToDone(listScript);
			if(listScript.getName().equals(script.getName())) {
				break;
			}
		}
	}

	protected void setSingleScriptToDone(Script script) {
		log.warn("Manually setting script status to DONE: {0}", script.getName());
		script.setError(null);
		script.setErrorCause(null);
		script.setStatus(Status.DONE);
		scriptService.getScriptDao().updateScript(script);
	}

	/**
	 * Is the original script source (file) available?
	 *
	 * @return
	 */
	public boolean isAvailable() {
		return available;
	}

	/**
	 * @return the scripts
	 */
	public List<Script> getScripts() {
		return scripts;
	}

	/**
	 * @param scripts the scripts to set
	 */
	public void setScripts(List<Script> scripts) {
		this.scripts = scripts;
	}

	/**
	 * Format and abbreviate a text.
	 *
	 * @param text
	 * @param max
	 * @return
	 */
	public String format(String text, int max) {
		var result = StringEscapeUtils.escapeHtml4(text);
		result = "<pre>%s</pre>".formatted(text);
		return StringUtils.abbreviateMiddle(result, "...", max);
	}

	/**
	 * Format an {@link Instant}.
	 *
	 * @param instant
	 * @return
	 */
	public String formatInstant(Instant instant) {
		return instant != null ? instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.SSS")) : null;
	}
}
