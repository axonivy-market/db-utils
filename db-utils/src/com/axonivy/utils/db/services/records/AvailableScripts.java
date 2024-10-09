package com.axonivy.utils.db.services.records;

import java.util.List;

import com.axonivy.utils.db.services.db.Script;

/**
 * A record containing available and unavailable scripts.
 */
public record AvailableScripts(List<Script> available, List<Script> unavailable) {}
