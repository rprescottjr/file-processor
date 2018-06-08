package com.rprescott.fileprocessor.events;

import java.io.File;

public class MalformedLineEvent {
	
	private File file;
	private long lineNumber;

	public MalformedLineEvent(File inputFile, long lineNumber) {
		this.file = inputFile;
		this.lineNumber = lineNumber;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public long getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(long lineNumber) {
		this.lineNumber = lineNumber;
	}

}
