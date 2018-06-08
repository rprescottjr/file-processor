package com.rprescott.fileprocessor.validation.rules;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateFormatValidationRule extends AbstractInputValidationRule {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DateFormatValidationRule.class);
	private DateTimeFormatter expectedFormat;

	public DateFormatValidationRule(Object metadata) {
		this(metadata, true);
	}
	
	public DateFormatValidationRule(Object metadata, boolean notifyImmediately) {
		super(metadata, notifyImmediately);
		if (metadata != null) {
			if (metadata instanceof String) {
				expectedFormat = DateTimeFormatter.ofPattern((String) metadata);
			}
		}
		else {
			LOGGER.error("No metadata supplied for {}.", this.getClass().getSimpleName());
		}
	}

	@Override
	public boolean validate(String inputData) {
		boolean isValid = true;
		if (inputData != null && !inputData.isEmpty()) {
			try {
				expectedFormat.parse(inputData);
			}
			catch (DateTimeParseException ex) {
				LOGGER.error(ex.getMessage(), ex);
				isValid = false;
			}
		}
		return isValid;
	}

	@Override
	public int getRuleId() {
		return 4;
	}

	public DateTimeFormatter getExpectedFormat() {
		return expectedFormat;
	}

	@Override
	public String getRuleName() {
		return "Date Format";
	}
}
