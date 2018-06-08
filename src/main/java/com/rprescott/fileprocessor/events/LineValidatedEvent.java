package com.rprescott.fileprocessor.events;

public class LineValidatedEvent {

	private String[] validatedLine;
	private String originalLine;

	public LineValidatedEvent(String[] validatedLine) {
		this(null, validatedLine);
	}
	
	public LineValidatedEvent(String originalLine, String[] validatedLine) {
		this.originalLine = originalLine; 
		this.validatedLine = validatedLine;
	}

	public String[] getValidatedLine() {
		return validatedLine;
	}

	public String getOriginalLine() {
		return originalLine;
	}
}
