package org.dbunit.dataset.excel;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dbunit.dataset.AbstractDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableIterator;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.OrderedTableNameMap;

import com.axonivy.utils.db.resolver.DbUtilsResolver;

public class XlsDataSetBlob extends AbstractDataSet {

	/**
	 * Logger for this class
	 */
	private static final Logger LOG = LogManager.getLogger(new MessageFormatMessageFactory());

	private OrderedTableNameMap _tables;

	/**
	 * Creates a new XlsDataSet object that loads the specified Excel document.
	 * @param dbUtilsResolver
	 */
	public XlsDataSetBlob(InputStream in, DbUtilsResolver dbUtilsResolver) throws IOException, DataSetException {
		_tables = super.createTableNameMap();

		Workbook workbook;
		try {
			workbook = WorkbookFactory.create(in);
		} catch (EncryptedDocumentException e) {
			throw new IOException(e);
		}

		int sheetCount = workbook.getNumberOfSheets();
		for (int i = 0; i < sheetCount; i++) {
			ITable table = new XlsTableBlob(workbook.getSheetName(i), workbook.getSheetAt(i), dbUtilsResolver);
			_tables.add(table.getTableMetaData().getTableName(), table);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// AbstractDataSet class

	@Override
	protected ITableIterator createIterator(boolean reversed) throws DataSetException {
		if (LOG.isDebugEnabled())
			LOG.debug("createIterator(reversed={}) - start", String.valueOf(reversed));

		ITable[] tables = (ITable[]) _tables.orderedValues().toArray(new ITable[0]);
		return new DefaultTableIterator(tables, reversed);
	}
}