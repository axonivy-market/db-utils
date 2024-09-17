package com.axonivy.utils.db.services.enums;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Functions;

/**
 * Type of a path.
 */
public enum PathType {
	CLASSPATH("classpath"),
	FILE("file"),
	UNKNOWN("");

	private static final Map<String, PathType> NAME_MAP =
			Stream.of(values()).collect(Collectors.toMap(PathType::getType, Functions.identity()));

	private String type;

	PathType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public static PathType fromType(String type) {
		return NAME_MAP.getOrDefault(type, UNKNOWN);
	}
}