package com.rprescott.fileprocessor.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the results of record validation.
 */
public class RecordValidationResult {
	
	private List<ValidationFailure> validationErrors;
	/** The original value that was validated. */
	private String[] value;

	public RecordValidationResult(String[] input) {
		this.value = input;
		validationErrors = new ArrayList<>();
	}

	public boolean isValid() {
		return validationErrors.isEmpty();
	}
	
	public List<ValidationFailure> getValidationErrors() {
		return this.validationErrors;
	}

	public void addValidationError(ValidationFailure validationError) {
		validationErrors.add(validationError);
	}

	public String[] getValue() {
		return value;
	}
}