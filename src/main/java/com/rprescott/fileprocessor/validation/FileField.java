package com.rprescott.fileprocessor.validation;

import java.util.ArrayList;
import java.util.List;

public class FileField {
	
	private int position;
	private String name;
	private String description;
	private String outputMapping;
	private List<ValidationRule> validationRules = new ArrayList<>();

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOutputMapping() {
		return outputMapping;
	}

	public void setOutputMapping(String outputMapping) {
		this.outputMapping = outputMapping;
	}

	public List<ValidationRule> getValidationRules() {
		return validationRules;
	}
	
	public void addValidationRule(ValidationRule validationRule) {
		this.validationRules.add(validationRule);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Field Position: ");
		builder.append(this.position);
		builder.append(". Name: " );
		builder.append(this.name);
		builder.append(". Description: ");
		builder.append(this.description);
		builder.append(". Output Mapping: ");
		builder.append(this.outputMapping == null ? "None" : this.outputMapping);
		return builder.toString();
	}

}
