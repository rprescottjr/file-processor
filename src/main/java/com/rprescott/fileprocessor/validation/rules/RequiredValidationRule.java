package com.rprescott.fileprocessor.validation.rules;

public class RequiredValidationRule extends AbstractInputValidationRule {
	
	public RequiredValidationRule(Object metadata) {
		this(metadata, true);
	}
	
	public RequiredValidationRule(Object metadata, boolean notifyImmediately) {
		super(metadata, notifyImmediately);
	}

	@Override
	public boolean validate(String inputData) {
		return inputData != null && !inputData.isEmpty();
	}

	@Override
	public int getRuleId() {
		return 1;
	}

	@Override
	public String getRuleName() {
		return "Required";
	}

}
