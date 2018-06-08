package com.rprescott.fileprocessor.validation.rules;

import org.apache.commons.lang3.StringUtils;

public class NumericValidationRule extends AbstractInputValidationRule {
	
	public NumericValidationRule(Object metadata) {
		this(metadata, true);
	}
	
	public NumericValidationRule(Object metadata, boolean notifyImmediately) {
		super(metadata, notifyImmediately);
	}

	@Override
	public boolean validate(String inputData) {
		boolean isValid = true;
		if (inputData != null && !inputData.isEmpty()) {
			StringUtils.isWhitespace(inputData);
			isValid = StringUtils.isWhitespace(inputData) || StringUtils.isNumeric(inputData.trim());
		}
		return isValid;
	}

	@Override
	public int getRuleId() {
		return 5;
	}

	@Override
	public String getRuleName() {
		return "Numeric";
	}

}
