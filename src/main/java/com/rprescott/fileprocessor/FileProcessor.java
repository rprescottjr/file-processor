package com.rprescott.fileprocessor;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.rprescott.fileprocessor.events.FileCompletedProcessingEvent;
import com.rprescott.fileprocessor.events.FileStartedProcessingEvent;
import com.rprescott.fileprocessor.events.InvalidLineEvent;
import com.rprescott.fileprocessor.events.LineSplitEvent;
import com.rprescott.fileprocessor.events.LineValidatedEvent;
import com.rprescott.fileprocessor.events.MalformedLineEvent;
import com.rprescott.fileprocessor.exceptions.BufferExceededException;
import com.rprescott.fileprocessor.validation.FileConfiguration;
import com.rprescott.fileprocessor.validation.FileConfigurationLoader;
import com.rprescott.fileprocessor.validation.FileField;
import com.rprescott.fileprocessor.validation.RecordValidationResult;
import com.rprescott.fileprocessor.validation.ValidationFailure;
import com.rprescott.fileprocessor.validation.ValidationRule;
import com.rprescott.fileprocessor.validation.rules.AbstractInputValidationRule;
import com.rprescott.fileprocessor.validation.rules.ValidationRules;

/**
 * 
 * @author rprescott
 *
 */
@Component
public class FileProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessor.class);
	
    private int bufferSize = 4096;
    /** 25MB Max Buffer Capacity */
    private int maxBufferCapacity = 26214400;
    private String lineSeparator = "\n";
	private ApplicationEventPublisher eventPublisher;
	private LineSplitter lineSplitter;
	private FileConfigurationLoader fileConfigurationLoader;
	private ValidationRules validationRules;
	
	/**
	 * Processes the specified file. For each line that is read, a {@link LineSplitEvent} is broadcasted. Any line
	 * that does not meet the specified criteria (Delimiter and Expected Number of Fields) has a {@link MalformedLineEvent} broadcasted.
	 * This method assumes there is no file header present.
	 * 
	 * <br><br> 
	 * 
	 * This method does not perform any sort of validation on any of the lines of the file.
	 * 
	 * <br><br>
	 * 
	 * This processor reads in data into a 4KB buffer. If no EOR marker ('\n') is detected in the buffer (i.e. A record is larger than 4096 bytes),
	 * then the buffer size will automatically double recursively until an EOR marker is detected.
	 * 
	 * <br><br>
	 * 
	 * <b>This processor will throw a {@link BufferExceededException} if the buffer size exceeds 25MB, as this most likely indicates a malformed file.</b>
	 * 
	 * @param inputFile
	 * 		The input file to process.
	 * @param delimiter
	 * 		The expected delimiter of the file.
	 * @param expectedNumberOfFields
	 * 		The expected number of fields for each record in the file.
	 * @throws IOException
	 */
	public void readFile(File inputFile, String delimiter, int expectedNumberOfFields) throws IOException {
		FileConfiguration configuration = new FileConfiguration();
		configuration.setDelimiter(delimiter);
		configuration.setExpectedNumberOfFields(expectedNumberOfFields);
		configuration.setContainsHeader(false);
		readFile(inputFile, configuration, false);
	}
	
	/**
	 * Processes the specified file. For each line that is read, a {@link LineSplitEvent} is broadcasted. Any line
	 * that does not meet the specified criteria (Delimiter and Expected Number of Fields) has a {@link MalformedLineEvent} broadcasted.
	 * This method assumes there is no file header present.
	 * 
	 * <br><br> 
	 * 
	 * This method does not perform any sort of validation on any of the lines of the file.
	 * 
	 * <br><br>
	 * 
	 * This processor reads in data into a 4KB buffer. If no EOR marker ('\n') is detected in the buffer (i.e. A record is larger than 4096 bytes),
	 * then the buffer size will automatically double recursively until an EOR marker is detected.
	 * 
	 * <br><br>
	 * 
	 * <b>This processor will throw a {@link BufferExceededException} if the buffer size exceeds 25MB, as this most likely indicates a malformed file.</b>
	 * 
	 * @param inputFile
	 * 		The input file to process.
	 * @param delimiter
	 * 		The expected delimiter of the file.
	 * @param expectedNumberOfFields
	 * 		The expected number of fields for each record in the file.
	 * @throws IOException
	 */
	public void readFile(File inputFile, String delimiter, int expectedNumberOfFields, boolean containsHeader) throws IOException {
		FileConfiguration configuration = new FileConfiguration();
		configuration.setDelimiter(delimiter);
		configuration.setExpectedNumberOfFields(expectedNumberOfFields);
		configuration.setContainsHeader(containsHeader);
		readFile(inputFile, configuration, false);
	}
	
	/**
	 * Processes the specified file. For each line that is read, a {@link LineSplitEvent} is broadcasted. Any line
	 * that does not meet the specified criteria (Delimiter and Expected Number of Fields) has a {@link MalformedLineEvent} broadcasted.
	 * 
	 * <br><br>
	 * 
	 * This processor reads in data into a 4KB buffer. If no EOR marker ('\n') is detected in the buffer (i.e. A record is larger than 4096 bytes),
	 * then the buffer size will automatically double recursively until an EOR marker is detected.
	 * 
	 * <br><br>
	 * 
	 * <b>This processor will throw a {@link BufferExceededException} if the buffer size exceeds 25MB, as this most likely indicates a malformed file.</b>
	 * 
	 * @param inputFile
	 * 		The input file to process.
	 * @param delimiter
	 * 		The expected delimiter of the file.
	 * @param expectedNumberOfFields
	 * 		The expected number of fields for each record in the file.
	 * @param containsHeader
	 * 		True if the file contains a header line. False otherwise. 
	 * @throws IOException
	 */
    public void readFile(File inputFile, FileConfiguration fileConfiguration, boolean performValidation) throws IOException {
    	// We will skip line read events if we are performing validation.
    	boolean skipLineReadEvents = performValidation;
    	Stopwatch fileProcessingClock = Stopwatch.createStarted();
    	LOGGER.info("Started reading file: " + inputFile.getAbsolutePath());
    	char delimiterChar = fileConfiguration.getDelimiter().charAt(0);
    	eventPublisher.publishEvent(new FileStartedProcessingEvent(inputFile));
        ByteBuffer buf = ByteBuffer.allocate(bufferSize);
        long recordsRead = 0;

        try (FileChannel channel  = FileChannel.open(inputFile.toPath())) {
            // Read in the first block of data into the buffer.
            int bytesRead = channel.read(buf);
            buf.flip();
            String lineEnding = "\r\n";
            // Continue while we have read some bytes in from the file.
            while (bytesRead != -1) {
                ByteBuffer workBuf = buf;
                // While there are still bytes remaining in the work buffer...
                while (workBuf.hasRemaining()) {
                	// If we hit a new line character...
                    if (workBuf.get() == '\n') {
                    	// Create a new buffer with the same data backed array.
                        ByteBuffer lineBuf = workBuf.duplicate();
                        // If if the previous byte was a carriage return because we're on windows
                        // Then set the line buffer's position to be two back. This will yield the entire entry
                        // without the CRLF.
                        if (workBuf.position() > 1 && '\r' == workBuf.get(workBuf.position() - 2)) {
                            lineBuf.position(lineBuf.position() - lineSeparator.length() - 1).flip();
                        }
                        // Otherwise, just move the position back one to handle UNIX LF. This will yield the entire entry
                        // without the LF.
                        else {
                        	lineEnding = "\n";
                            lineBuf.position(lineBuf.position() - lineSeparator.length()).flip();
                        }
                        recordsRead++;
                        // If the file contains a header and this is the first record we are reading, then skip it because we aren't concerned about headers.
                        if (fileConfiguration.isContainsHeader() && recordsRead == 1) {
                        	// Skip this line!
                        }
                        else {
                        	CharBuffer decodedCurrentLineBuffer = Charsets.US_ASCII.decode(lineBuf);
                        	String originalLine = decodedCurrentLineBuffer.toString();
                        	Optional<String[]> splitLine = lineSplitter.splitLine(inputFile, decodedCurrentLineBuffer, delimiterChar, fileConfiguration.getExpectedNumberOfFields(), recordsRead, skipLineReadEvents);
                        	if (performValidation) {
                        		if (splitLine.isPresent()) {
                            		if (validateLine(splitLine.get(), recordsRead, fileConfiguration.getFileFields())) {
                            			// Let all listeners know of a successfully validated line.
                            			eventPublisher.publishEvent(new LineValidatedEvent(originalLine + lineEnding, splitLine.get()));
                            		}
                        		}
                        	}
                        }
                    	workBuf = workBuf.slice();
                    }
                }
                buf.position(workBuf.arrayOffset());
                buf.compact();
                bytesRead = channel.read(buf);
                if (bytesRead == 0) {
                	LOGGER.warn("There are currently {} bytes in the allocated buffer and we have not hit an EOR marking.", buf.capacity());
                	LOGGER.warn("Doubling buffer size and reading in to expand search. Typically, encountering this means you have a severely malformed file.");
                	ByteBuffer bigBoyBuffer = ByteBuffer.allocate(buf.capacity() * 2);
                	if (bigBoyBuffer.capacity() > maxBufferCapacity) {
                		throw new BufferExceededException();
                	}
                	bigBoyBuffer.put(buf);
                	bigBoyBuffer.position(buf.position());
                	bytesRead = channel.read(bigBoyBuffer);
                	buf = bigBoyBuffer;
                }
                buf.flip();
            }
            // If the file did not end in a LF or CRLF, then we would not have processed any bytes from the previously found LF until now.
            // This info is **probably** a malformed record, but we'll go ahead and process it anyway.
            if (buf.limit() != 0) {
            	LOGGER.info("Detected EOF with an additional {} bytes left over. EOR marker not detected. Will attempt to process the unprocessed bytes, but this will most likely end up as a malformed line...", buf.limit());
            	recordsRead++;
            	lineSplitter.splitLine(inputFile, Charsets.US_ASCII.decode(buf), delimiterChar, fileConfiguration.getExpectedNumberOfFields(), recordsRead, skipLineReadEvents);
            }
            channel.close();
        }
        fileProcessingClock.stop();
        // If the file contains a header, remove one of the records read so we don't keep it in our totals.
        if (fileConfiguration.isContainsHeader()) {
        	recordsRead--;
        }
        eventPublisher.publishEvent(new FileCompletedProcessingEvent(inputFile, recordsRead));
    }
    
	/**
	 * Processes the specified file. For each line that is read, validation is done according to the provided configuration file. Below is an example of a configuration file with 1 field. 
	 * 
	 * <br><br>
	 * <pre>
	 * {@code
	 * <?xml version="1.0"?>
	 * <ps>
     *    <delimiter>,</delimiter>
     *    <containsHeader>true</containsHeader>
     *    <field>
     *        <name>eDocJobID</name>
     *        <description>Mail.dat Job ID of the eDoc that the mailpiece is associated to. Job IDs are user managed, but must remain unique within one User License Code. Mail.dat Field Code: SEG-1001</description>
     *        <validationRule>
     *            <id>3</id>
     *            <metadata>8</metadata>
     *        </validationRule>
     *    </field>
     * </ps>
	 * }
	 * </pre>
	 * 
	 * Any line that does not meet the specified criteria (Delimiter and Expected Number of Fields) has a {@link MalformedLineEvent} broadcasted. <br>
	 * For each line that is read and successfully validated, a {@link LineValidatedEvent} is broadcasted. <br>
	 * For each line that is read and fails validation, an {@link InvalidLineEvent} is broadcasted.
	 * 
	 * <br><br>
	 * 
	 * This processor reads in data into a 4KB buffer. If no EOR marker ('\n') is detected in the buffer (i.e. A record is larger than 4096 bytes),
	 * then the buffer size will automatically double recursively until an EOR marker is detected.
	 * 
	 * <br><br>
	 * 
	 * <b>This processor will throw a {@link BufferExceededException} if the buffer size exceeds 25MB, as this most likely indicates a malformed file.</b>
	 * 
	 * @param inputFile
	 * 		The input file to process.
	 * @param configurationFile
	 * 		The configuration file containing information about the file to process.
	 * @throws IOException
	 */
    public void readAndValidateFile(File inputFile, File configurationFile) throws IOException, ParserConfigurationException, SAXException {
    	FileConfiguration config = fileConfigurationLoader.loadConfigurationFile(configurationFile);
    	readFile(inputFile, config, true);
    }
    
	private boolean validateLine(String[] splitLine, long lineNumber, List<FileField> fileFields) {
		boolean isLineValid = true;
        RecordValidationResult recordValidationResult = performValidation(splitLine, lineNumber, fileFields);
        if (!recordValidationResult.isValid()) {
        	isLineValid = false;
        	for (ValidationFailure validationFailure : recordValidationResult.getValidationErrors()) {
        		if (LOGGER.isErrorEnabled()) {
            		LOGGER.error("Field Position {} ({}) of Record {} failed validation on Rule ID: {} with supplied value \"{}\"",
            			validationFailure.getField().getPosition(),
            			validationFailure.getField().getName(),
            			Arrays.toString(recordValidationResult.getValue()),
        				validationFailure.getRule().getRuleId(),
        				validationFailure.getFieldValue());
        		}
        	}
        	eventPublisher.publishEvent(new InvalidLineEvent(recordValidationResult));
        }
        return isLineValid;
	}
    
	protected RecordValidationResult performValidation(String[] input, long lineNumber, List<FileField> fileFields) {
		RecordValidationResult validationResult = new RecordValidationResult(input);
		for (int i = 0; i < input.length; i++) {
			FileField field = fileFields.get(i);
			for (ValidationRule validationRule : field.getValidationRules()) {
				Class<? extends AbstractInputValidationRule> validationRuleClass = validationRules.get(validationRule.getRuleId());
				if (validationRuleClass != null) {
					LOGGER.trace("Validating field: {} against Validation Rule ID: {} with Metadata: {}",
						input[i],
						validationRule.getRuleId(),
						validationRule.getMetadata());
					AbstractInputValidationRule ruleToValidateAgainst = validationRules.getInstance(
						validationRule.getRuleId(),
						validationRule.getMetadata(),
						validationRule.shouldNotifyImmediately());
					if (!ruleToValidateAgainst.validate(input[i])) {
						validationResult.addValidationError(new ValidationFailure(field, input[i], lineNumber, ruleToValidateAgainst));
					}
				}
				else {
					LOGGER.warn("Validation rule not found for ID: {}", validationRule.getRuleId());
				}
			}
		}
		return validationResult;
	}
    
    @Autowired
	public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
    
    @Autowired
    public void setLineSplitter(LineSplitter lineSplitter) {
    	this.lineSplitter = lineSplitter;
    }
    
    @Autowired
    public void setFileConfigurationLoader(FileConfigurationLoader fileConfigurationLoader) {
    	this.fileConfigurationLoader = fileConfigurationLoader;
    }

    @Autowired
	public void setValidationRules(ValidationRules validationRules) {
		this.validationRules = validationRules;
	}
}
