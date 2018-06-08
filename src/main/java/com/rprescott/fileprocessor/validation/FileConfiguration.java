package com.rprescott.fileprocessor.validation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class FileConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileConfiguration.class);
	private String delimiter;
	private boolean containsHeader;
	private int expectedNumberOfFields;
	private List<FileField> fileFields;
	
	public String getDelimiter() {
		return delimiter;
	}
	
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	public boolean isContainsHeader() {
		return containsHeader;
	}

	public void setContainsHeader(boolean containsHeader) {
		this.containsHeader = containsHeader;
	}

	public List<FileField> getFileFields() {
		return fileFields;
	}
	
	public void setFileFields(List<FileField> fileFields) {
		this.fileFields = fileFields;
	}
	
	public int getExpectedNumberOfFields() {
		if (expectedNumberOfFields != 0) {
			if (!CollectionUtils.isEmpty(fileFields)) {
				LOGGER.warn("Both FileFields and expectedNumberOfFields are defined. Using expectedNumberOfFields as the expected value.");
			}
			return this.expectedNumberOfFields;
		}
		else {
			return this.fileFields.size();
		}
		
	}

	public void setExpectedNumberOfFields(int expectedNumberOfFields) {
		this.expectedNumberOfFields = expectedNumberOfFields;
	}
}
