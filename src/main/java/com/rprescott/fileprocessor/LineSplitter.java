package com.rprescott.fileprocessor;

import java.io.File;
import java.nio.CharBuffer;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.rprescott.fileprocessor.events.LineSplitEvent;
import com.rprescott.fileprocessor.events.MalformedLineEvent;

@Service
public class LineSplitter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LineSplitter.class);
	private ApplicationEventPublisher eventPublisher;

	public Optional<String[]> splitLine(File inputFile, CharBuffer lineBuf, char delimiter, int expectedNumberOfFields, long lineNumber, boolean skipLineReadEvents) {
        String[] record = new String[expectedNumberOfFields];
        boolean lineEndsWithDelimiter = false;
        int idx = 0;
        while (lineBuf.hasRemaining()) {
            if (idx >= expectedNumberOfFields) {
            	LOGGER.error("Expected {} fields in record, we are currently attempting to process field number {}.", expectedNumberOfFields, idx);
            	eventPublisher.publishEvent(new MalformedLineEvent(inputFile, lineNumber));
                return Optional.empty();
            }

            // Skip the buffer forward until we hit our delimiter of interest.
            while (lineBuf.get() != delimiter && lineBuf.hasRemaining()) {
            	// Empty block here to appease SonarQube. lineBuf.get() advances the pointer one byte, so it IS doing work.
            }
        	if (!lineBuf.hasRemaining() && lineBuf.get(lineBuf.position() - 1) == delimiter) {
        		lineEndsWithDelimiter = true;
        	}
            CharBuffer slice = lineBuf.duplicate();
            slice.flip();
            if (lineBuf.hasRemaining() || lineEndsWithDelimiter) {
            	// Remove the delimiter.
                slice.limit(slice.limit() - 1);
            }
            record[idx] = slice.toString();
            idx++;

            lineBuf = lineBuf.slice();
        }
        if (lineEndsWithDelimiter) {
        	if (idx == expectedNumberOfFields) {
        		eventPublisher.publishEvent(new MalformedLineEvent(inputFile, lineNumber));
        		return Optional.empty();
        	}
        	else {
        		record[idx] = "";
        	}
        }
        int recordLength = getLength(record);
        if (getLength(record) != expectedNumberOfFields) {
        	LOGGER.error("Expected {} fields in record, but only {} were present.", expectedNumberOfFields, recordLength);
        	eventPublisher.publishEvent(new MalformedLineEvent(inputFile, lineNumber));
            return Optional.empty();
        }
        if (!skipLineReadEvents) {
        	eventPublisher.publishEvent(new LineSplitEvent(inputFile, Optional.of(record), lineNumber));
        }
        return Optional.of(record);
	}
	
	public <T> int getLength(T[] arr) {
	    int count = 0;
	    for (T el : arr) {
	        if (el != null) {
	            ++count;
	        }
	    }
	    return count;
	}
	
	@Autowired
	public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
}
