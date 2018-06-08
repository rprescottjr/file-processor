package com.rprescott.fileprocessor.validation.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaxLengthValidationRule extends AbstractInputValidationRule {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MaxLengthValidationRule.class);
	private int maxLength;

	public MaxLengthValidationRule(Object metadata) {
		this(metadata, true);
	}
	
	public MaxLengthValidationRule(Object metadata, boolean notifyImmediately) {
		super(metadata, notifyImmediately);
		if (!notifyImmediately) {
			System.err.println("MaxLengthValidationRule constructed with Metadata " + metadata.toString() + " and DO NOT notify immediately.");
		}
		
		if (metadata != null) {
			if (metadata instanceof String) {
				try {
					maxLength = Integer.valueOf(((String) metadata).trim());
				}
				catch (NumberFormatException ex) {
					// Set the length to an invalid length.
					LOGGER.error("Invalid metadata received for {}. Supplied Metadata: {}", this.getClass().getSimpleName(), (String) metadata);
					maxLength = -1;
				}
			}
			else if (metadata instanceof Number) {
				maxLength = ((Number) metadata).intValue();
			}
			else {
				LOGGER.error("Invalid metadata type supplied for {}. Supplied Metadata: {}", this.getClass().getSimpleName(), metadata);
			}
		}
		else {
			LOGGER.error("No metadata supplied for {}.", this.getClass().getSimpleName());
			maxLength = -1;
		}
	}

	@Override
	public boolean validate(String inputData) {
		boolean isValid = false;
		if (maxLength != -1 && (inputData == null || inputData.length() == 0 || inputData.trim().length() <= maxLength)) {
			isValid = true;
		}
		return isValid;
	}

	@Override
	public int getRuleId() {
		return 3;
	}

	@Override
	public String getRuleName() {
		return "Max Length";
	}
}
