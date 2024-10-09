package com.axonivy.utils.db.services.db;

import java.time.Instant;

import com.axonivy.utils.db.services.enums.Status;

public class Script {
	private String name;
	private Status status;
	private Instant executedAt;
	private String error;
	private String errorCause;
	private String script;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * @return the errorCause
	 */
	public String getErrorCause() {
		return errorCause;
	}

	/**
	 * @param errorCause the errorCause to set
	 */
	public void setErrorCause(String errorCause) {
		this.errorCause = errorCause;
	}


	/**
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * @param script the script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}

	/**
	 * @return the executedAt
	 */
	public Instant getExecutedAt() {
		return executedAt;
	}

	/**
	 * @param executedAt the executedAt to set
	 */
	public void setExecutedAt(Instant executedAt) {
		this.executedAt = executedAt;
	}

	/**
	 * Is this script enabled?
	 *
	 * @return
	 */
	public boolean isEnabled() {
		return status != Status.DISABLED;
	}

	/**
	 * Can the script be disabled?
	 *
	 * @return
	 */
	public boolean canBeDisabled() {
		return status == null || status == Status.NONE || status == Status.ERROR;
	}
}
