package org.dbunit.dataset.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import com.axonivy.utils.db.resolver.DbUtilsResolver;
import com.axonivy.utils.db.services.enums.PathType;
import com.axonivy.utils.db.services.records.TypedPath;

public class XlsDataSetWriterBlob extends XlsDataSetWriter {
	private static final Logger LOG = LogManager.getLogger(new MessageFormatMessageFactory());
	private static final Pattern PATH_PATTERN = Pattern.compile("/*(.*?)/*");
	private CellStyle dateCellStyle;
	private DbUtilsResolver dbUtilsResolver;

	public XlsDataSetWriterBlob(DbUtilsResolver dbUtilsResolver) {
		this.dbUtilsResolver = dbUtilsResolver;
	}

	/**
	 * Write the specified dataset and it's blobs to the specified ZIP file.
	 *
	 * @param dataSet
	 * @param zipOut
	 * @return
	 * @throws IOException
	 * @throws DataSetException
	 */
	public void write(IDataSet dataSet, ZipOutputStream zipOut) throws IOException, DataSetException {
		LOG.debug("write(dataSet={0}, out={1}) - start", dataSet, zipOut);

		TypedPath dataPath = TypedPath.create(dbUtilsResolver.getDataUrl());

		if(!dataPath.hasType(PathType.CLASSPATH)) {
			throw new DataSetException("URL '%s' is not supported by Excel Blob Export".formatted(dbUtilsResolver.getDataUrl()));
		}

		String relPath = makeRel(dataPath.path());

		Workbook workbook = createWorkbook();

		int index = 0;
		ITableIterator iterator = dataSet.iterator();
		while (iterator.next()) {
			// create the table i.e. sheet
			ITable table = iterator.getTable();
			ITableMetaData metaData = table.getTableMetaData();
			Sheet sheet = workbook.createSheet(metaData.getTableName());

			// write table metadata i.e. first row in sheet
			workbook.setSheetName(index, metaData.getTableName());

			Row headerRow = sheet.createRow(0);
			Column[] columns = metaData.getColumns();
			for (int j = 0; j < columns.length; j++) {
				Column column = columns[j];
				Cell cell = headerRow.createCell(j);
				cell.setCellValue(column.getColumnName());
			}

			for (int j = 0; j < table.getRowCount(); j++) {
				Row row = sheet.createRow(j + 1);
				for (int k = 0; k < columns.length; k++) {
					Column column = columns[k];
					Object value = table.getValue(j, column.getColumnName());

					if (value != null) {
						Cell cell = row.createCell(k);
						if (value instanceof Date) {
							setDateCell(cell, (Date) value, workbook);
						} else if (value instanceof BigDecimal) {
							setNumericCell(cell, (BigDecimal) value, workbook);
						} else if (value instanceof Long) {
							setDateCell(cell, new Date(((Long) value).longValue()), workbook);
						} else if (dbUtilsResolver.isExcelExportLob(column)) {
							final var r = j;
							// Use concatenation of primary keys for doc name.
							String id = Stream.of(metaData.getPrimaryKeys())
									.map(c -> rowCol(table, r, c))
									.collect(Collectors.joining(":"));

							// Path to write into cell.
							String path = "%s:/%s/lob/%s/%s/%s".formatted(dataPath.type(), relPath, metaData.getTableName(), column.getColumnName(), id);
							cell.setCellValue(path);

							// Relative path in zip file.
							String zipPath = "%s/lob/%s/%s/%s".formatted(relPath, metaData.getTableName(), column.getColumnName(), id);
							ZipEntry entry = new ZipEntry(zipPath);
							zipOut.putNextEntry(entry);
							zipOut.write((byte[]) value);
							zipOut.closeEntry();
						} else {
							cell.setCellValue(DataType.asString(value).length() > 32767 ? DataType.asString("") : DataType.asString(value));
						}
					}
				}
			}

			index++;
		}

		// write xls document
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			workbook.write(bos);
			bos.flush();
			ZipEntry entry = new ZipEntry("%s/export.xls".formatted(relPath));
			zipOut.putNextEntry(entry);
			zipOut.write(bos.toByteArray());
			zipOut.closeEntry();
		}
	}

	protected String rowCol(ITable table, int j, Column c) {
		try {
			return "" + table.getValue(j, c.getColumnName());
		} catch (DataSetException e) {
			throw new RuntimeException(e);
		}
	}

	protected String makeRel(String path) {
		String cleaned = path.replaceAll("//+", "/");
		Matcher matcher = PATH_PATTERN.matcher(cleaned);
		if(matcher.matches()) {
			cleaned = matcher.group(1);
		}
		return cleaned;
	}

	@Override
	protected void setDateCell(Cell cell, Date value, Workbook workbook) {
		if(dateCellStyle == null) {
			dateCellStyle = createDateCellStyle(workbook);
		}
		long timeMillis = value.getTime();
		cell.setCellValue( timeMillis );
		cell.setCellStyle(dateCellStyle);
	}
}
