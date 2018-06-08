package com.rprescott.fileprocessor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.xml.sax.SAXException;

import com.rprescott.fileprocessor.FileProcessor;
import com.rprescott.fileprocessor.LineSplitter;
import com.rprescott.fileprocessor.events.FileCompletedProcessingEvent;
import com.rprescott.fileprocessor.events.FileStartedProcessingEvent;
import com.rprescott.fileprocessor.events.LineSplitEvent;
import com.rprescott.fileprocessor.validation.FileConfigurationLoader;
import com.rprescott.fileprocessor.validation.XMLPrinter;
import com.rprescott.fileprocessor.validation.rules.ValidationRules;

@RunWith(MockitoJUnitRunner.class)
public class FileProcessorTest {
	
	private FileProcessor classUnderTest;
	private LineSplitter lineSplitter;
	private FileConfigurationLoader fileConfigurationLoader;
	private ValidationRules validationRules;
	@Mock
	private XMLPrinter mockXmlPrinter;
	@Mock
	private ApplicationEventPublisher mockEventPublisher;
	
	@Before
	public void setup() throws Exception {
		classUnderTest = new FileProcessor();
		lineSplitter = new LineSplitter();
		fileConfigurationLoader = new FileConfigurationLoader();
		validationRules = new ValidationRules();
		validationRules.afterPropertiesSet();
		fileConfigurationLoader.setXmlPrinter(mockXmlPrinter);
		lineSplitter.setEventPublisher(mockEventPublisher);
		
		// Set up our class under test with the appropriate dependencies (Some mocked, some not);
		classUnderTest.setLineSplitter(lineSplitter);
		classUnderTest.setEventPublisher(mockEventPublisher);
		classUnderTest.setFileConfigurationLoader(fileConfigurationLoader);
		classUnderTest.setValidationRules(validationRules);
	}
	
	@Test
	public void testProcessingCommaSeparatedFile() throws IOException, ParserConfigurationException, SAXException {
	    File dataFile = new File("src/test/resources/data/Comma_5_Fields.txt");
	    File configurationFile = new File("src/test/resources/config/comma_5_fields.xml");
	    classUnderTest.readAndValidateFile(dataFile, configurationFile);
	    verifyResults(dataFile, false, 5, 0);
	}

	private void verifyResults(File testFile, boolean containsHeader, int numberOfLines, int numberOfLineMalformations) {
		int totalEvents = numberOfLines + numberOfLineMalformations + 1 + 1;
		FileStartedProcessingEvent event = new FileStartedProcessingEvent(testFile);
		// Verify we sent out the file started processing event.
		verify(mockEventPublisher).publishEvent(event);
		// Capture all the times we triggered any type of event.
		ArgumentCaptor<Object> lineSplitEventCaptor = ArgumentCaptor.forClass(Object.class);
		verify(mockEventPublisher, times(totalEvents)).publishEvent(lineSplitEventCaptor.capture());
		
		// Make sure we captured the correct number of events (# of Line splits + Number of Malformed Lines + 1 File Started Processing + 1 File Completed Processing).
		assertEquals(totalEvents, lineSplitEventCaptor.getAllValues().size());
		List<Object> capturedEvents = lineSplitEventCaptor.getAllValues();
		int numLineSplitEvents = 0;
		for (int i = 0; i < capturedEvents.size(); i++) {
			if (capturedEvents.get(i) instanceof LineSplitEvent) {
				numLineSplitEvents++;
				// For this assertion, we need to simply add 1 to the number of line splits. This is because the first line in the sample file is malformed (Extra comma).
				// This means that the first line split happens on line 2 of the file and not line 1. I thought it made more sense to do the increment here instead of initializing the
				// variable to 1.
				// The following assertion guarantees order of the line split events. This means we won't get a line split for Line 10 in a file before we get a Line split for Line 9.
				if (!containsHeader) {
					assertEquals(numLineSplitEvents, ((LineSplitEvent) capturedEvents.get(i)).getLineNumber());	
				}
				else {
					assertEquals(numLineSplitEvents + 1, ((LineSplitEvent) capturedEvents.get(i)).getLineNumber());
				}
				
			}
			else if (capturedEvents.get(i) instanceof FileCompletedProcessingEvent) {
				FileCompletedProcessingEvent fileCompletedProcessingEvent = new FileCompletedProcessingEvent(testFile, numberOfLines + numberOfLineMalformations);
				assertEquals(fileCompletedProcessingEvent, capturedEvents.get(i));
			}
		}
	}
}
