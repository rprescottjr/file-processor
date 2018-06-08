package com.rprescott.fileprocessor.validation.rules;

public abstract class AbstractInputValidationRule {
	
	protected Object metadata;
	protected boolean notifyImmediately;
	
	public abstract boolean validate(String inputData);
	public abstract int getRuleId();
	public abstract String getRuleName();
	
	public AbstractInputValidationRule(Object metadata, boolean notifyImmediately) {
		this.metadata = metadata;
		this.notifyImmediately = notifyImmediately;
	}
	
	public boolean shouldNotifyImmediately() {
		return this.notifyImmediately;
	}
	
}
