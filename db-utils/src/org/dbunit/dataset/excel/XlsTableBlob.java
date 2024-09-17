package org.dbunit.dataset.excel;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.dbunit.dataset.DataSetException;

import com.axonivy.utils.db.resolver.DbUtilsResolver;
import com.axonivy.utils.db.services.enums.PathType;
import com.axonivy.utils.db.services.records.TypedPath;

public class XlsTableBlob extends XlsTable {
	private static final Logger LOG = LogManager.getLogger(new MessageFormatMessageFactory());
	private DbUtilsResolver dbUtilsResolver;

	public XlsTableBlob(String sheetName, Sheet sheet, DbUtilsResolver dbUtilsResolver) throws DataSetException {
		super(sheetName, sheet);
		this.dbUtilsResolver = dbUtilsResolver;
	}

	@Override
	public Object getValue(int row, String column) throws DataSetException {
		var value = super.getValue(row, column);
		if(value instanceof String) {
			var stringValue = (String)value;
			TypedPath typedPath = TypedPath.test(stringValue);
			if(typedPath != null && typedPath.hasType(PathType.CLASSPATH)) {
				try (java.io.InputStream input = dbUtilsResolver.getResourceAsStream(typedPath.path())) {
					if(input != null) {
						return input.readAllBytes();
					}
				} catch (IOException e) {
					LOG.error("Could not read classpath for row: %d column: %s classpath: %d".formatted(row, column, value), e);
				}
				return null;
			}
		}
		return value;
	}
}
