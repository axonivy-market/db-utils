package com.axonivy.utils.db.services.records;

import java.util.Objects;
import java.util.regex.Pattern;

import com.axonivy.utils.db.services.enums.PathType;

/**
 * A type and a path.
 */
public record TypedPath(String type, String path) {
	private static final Pattern SCRIPT_PATH_PATTERN = Pattern.compile("(.+?):(.*)");

	public static TypedPath create(String url) {
		var tp = test(url);
		if(tp == null) {
			throw new IllegalArgumentException("Could not extract typed path from '%s'.".formatted(url));
		}
		return tp;
	}

	public static TypedPath test(String url) {
		var matcher = SCRIPT_PATH_PATTERN.matcher(url);
		return matcher.matches() ? new TypedPath(matcher.group(1), matcher.group(2)) : null;
	}

	public String build(String path) {
		return "%s:%s".formatted(type, path);
	}

	public boolean hasType(String type) {
		return Objects.equals(this.type, type);
	}

	public boolean hasType(PathType type) {
		return Objects.equals(this.type, type.getType());
	}

	public String url() {
		return "%s:%s".formatted(type, path);
	}

	public PathType pathType() {
		return PathType.fromType(type);
	}
}