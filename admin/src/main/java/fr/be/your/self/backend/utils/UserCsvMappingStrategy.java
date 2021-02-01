package fr.be.your.self.backend.utils;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public class UserCsvMappingStrategy <T> extends ColumnPositionMappingStrategy<T> {
	private String[] headerColumns;
	public UserCsvMappingStrategy(String[] headerColumns) {
		this.headerColumns = headerColumns;
	}
    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
    	super.generateHeader(bean);
        return headerColumns;
    }
}