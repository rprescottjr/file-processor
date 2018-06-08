package com.rprescott.fileprocessor.events;

import com.rprescott.fileprocessor.validation.RecordValidationResult;

public class InvalidLineEvent {

	private RecordValidationResult recordValidationResult;

	public InvalidLineEvent(RecordValidationResult recordValidationResult) {
		this.recordValidationResult = recordValidationResult;
	}

	public RecordValidationResult getRecordValidationResult() {
		return recordValidationResult;
	}
}
