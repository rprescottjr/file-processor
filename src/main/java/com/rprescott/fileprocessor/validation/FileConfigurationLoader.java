package com.rprescott.fileprocessor.validation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Component
public class FileConfigurationLoader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileConfigurationLoader.class);
	private XMLPrinter xmlPrinter;
	
	public FileConfiguration loadConfigurationFile(File configFile) throws ParserConfigurationException, SAXException, IOException {
		FileConfiguration config = new FileConfiguration();
		List<FileField> fileFields = new ArrayList<>();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(configFile);
		document.getDocumentElement().normalize();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(xmlPrinter.toPrettyString(document, 4));
		}
		config.setDelimiter(document.getElementsByTagName("delimiter").item(0).getTextContent());
		config.setContainsHeader(Boolean.valueOf(document.getElementsByTagName("containsHeader").item(0).getTextContent()));
		NodeList fields = document.getElementsByTagName("field");
		for (int i = 0; i < fields.getLength(); i++) {
			FileField fileField = new FileField();
			Element field = (Element) fields.item(i);
			fileField.setPosition(i + 1);
			String name = field.getElementsByTagName("name").item(0).getTextContent();
			fileField.setName(name);
			fileField.setDescription(field.getElementsByTagName("description").item(0).getTextContent());
			NodeList outputMapping = field.getElementsByTagName("outputMapping");
			if (outputMapping.getLength() > 0) {
				fileField.setOutputMapping(outputMapping.item(0).getTextContent());
			}
			fileField.setDescription(field.getElementsByTagName("description").item(0).getTextContent());
			NodeList validationRules = field.getElementsByTagName("validationRule");
			for (int j = 0; j < validationRules.getLength(); j++) {
				Element validationRule = (Element) validationRules.item(j);
				NodeList metadataNodeList = validationRule.getElementsByTagName("metadata");
				NodeList disableImmediateNotificationNode = validationRule.getElementsByTagName("disableImmediateNotification");
				boolean notifyImmediately = true;
				if (disableImmediateNotificationNode.getLength() > 0) {
					// We don't care about any metadata in this field, just that one exists.
					notifyImmediately = false;
				}
				Object metadata = null;
				if (metadataNodeList != null && metadataNodeList.getLength() > 0) {
					metadata = metadataNodeList.item(0).getTextContent();
				}
				try {
					fileField.addValidationRule(new ValidationRule(
						Integer.valueOf(validationRule.getElementsByTagName("id").item(0).getTextContent()),
						metadata,
						notifyImmediately));					
				}
				catch (NumberFormatException ex) {
					LOGGER.error("Malformed Validation Rule ID Detected. Ignoring this field.");
				}
			}
			fileFields.add(fileField);
		}
		// Sort all the fields so that they are in positional order.
		Collections.sort(fileFields, new FileFieldComparator());
		if (LOGGER.isTraceEnabled()) {
			logFieldsLoaded(fileFields);
		}
		config.setFileFields(fileFields);
		return config;
	}
	
	private void logFieldsLoaded(List<FileField> fileFields) {
		for (FileField fileField : fileFields) {
			LOGGER.trace("Loaded File Field: {}", fileField);
		}
	}

	@Autowired
	public void setXmlPrinter(XMLPrinter xmlPrinter) {
		this.xmlPrinter = xmlPrinter;
	}
}
