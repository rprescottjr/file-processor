package com.rprescott.fileprocessor.validation;

import com.rprescott.fileprocessor.validation.rules.AbstractInputValidationRule;

public class ValidationFailure {
	
	private FileField field;
	private String fieldValue;
	private long lineNumber;
	private AbstractInputValidationRule rule;
	
	public ValidationFailure(FileField field, String fieldValue, long lineNumber, AbstractInputValidationRule rule) {
		this.field = field;
		this.fieldValue = fieldValue;
		this.lineNumber = lineNumber;
		this.rule = rule;
	}
	
	public FileField getField() {
		return field;
	}

	public String getFieldValue() {
		return fieldValue;
	}
	
	public long getLineNumber() {
		return lineNumber;
	}

	public AbstractInputValidationRule getRule() {
		return rule;
	}

}
