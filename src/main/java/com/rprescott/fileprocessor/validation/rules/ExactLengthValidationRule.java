package com.rprescott.fileprocessor.validation.rules;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExactLengthValidationRule extends AbstractInputValidationRule {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExactLengthValidationRule.class);
	private List<Integer> validLengths;

	public ExactLengthValidationRule(Object metadata) {
		this(metadata, true);
	}
	
	public ExactLengthValidationRule(Object metadata, boolean notifyImmediately) {
		super(metadata, notifyImmediately);
		if (metadata != null) {
			validLengths = new ArrayList<>();
			if (metadata instanceof String) {
				try {
					String[] lengths = ((String) metadata).split(",");
					for (String length : lengths) {
						validLengths.add(Integer.valueOf((length).trim()));
					}
				}
				catch (NumberFormatException ex) {
					// Set the length to an invalid length.
					LOGGER.error("Invalid metadata received for {}. Supplied Metadata: {}", this.getClass().getSimpleName(), (String) metadata);
				}
			}
			else if (metadata instanceof Number) {
				validLengths.add(((Number) metadata).intValue());
			}
			else {
				LOGGER.error("Invalid metadata type supplied for {}. Supplied Metadata: {}", this.getClass().getSimpleName(), metadata);
			}
		}
		else {
			LOGGER.error("No metadata supplied for {}.", this.getClass().getSimpleName());
		}
	}

	@Override
	public boolean validate(String inputData) {
		boolean isValid = false;
		if (!validLengths.isEmpty() && (inputData == null || inputData.length() == 0 || validLengths.contains(inputData.trim().length()))) {
			isValid = true;
		}
		return isValid;
	}

	@Override
	public int getRuleId() {
		return 2;
	}

	@Override
	public String getRuleName() {
		return "Exact Length";
	}
}
