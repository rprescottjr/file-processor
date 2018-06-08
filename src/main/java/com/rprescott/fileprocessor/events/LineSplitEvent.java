package com.rprescott.fileprocessor.events;

import java.io.File;
import java.util.Optional;

/**
 * @author rprescott
 */
public class LineSplitEvent {
	
	private File fileBeingProcessed;
	private Optional<String[]> splitLine;
	private long lineNumber;
	
	public LineSplitEvent(File fileBeingProcessed, Optional<String[]> splitLine, long lineNumber) {
		this.fileBeingProcessed = fileBeingProcessed;
		this.splitLine = splitLine;
		this.lineNumber = lineNumber;
	}

	public File getFileBeingProcessed() {
		return fileBeingProcessed;
	}

	public Optional<String[]> getSplitLine() {
		return splitLine;
	}

	public long getLineNumber() {
		return lineNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileBeingProcessed == null) ? 0 : fileBeingProcessed.hashCode());
		result = prime * result + (int) (lineNumber ^ (lineNumber >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineSplitEvent other = (LineSplitEvent) obj;
		if (fileBeingProcessed == null) {
			if (other.fileBeingProcessed != null)
				return false;
		} else if (!fileBeingProcessed.equals(other.fileBeingProcessed))
			return false;
		if (lineNumber != other.lineNumber)
			return false;
		return true;
	}

}
