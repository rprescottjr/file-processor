package com.rprescott.fileprocessor.validation;

public class ValidationRule {
	
	private Integer ruleId;
	private Object metadata;
	private boolean notifyImmediately;
	
	public ValidationRule(Integer ruleId, Object metadata, boolean notifyImmediately) {
		this.ruleId = ruleId;
		this.metadata = metadata;
		this.notifyImmediately = notifyImmediately;
	}

	public Integer getRuleId() {
		return ruleId;
	}

	public void setRuleId(Integer ruleId) {
		this.ruleId = ruleId;
	}

	public Object getMetadata() {
		return metadata;
	}

	public void setMetadata(Object metadata) {
		this.metadata = metadata;
	}
	
	public boolean shouldNotifyImmediately() {
		return notifyImmediately;
	}

	public void setNotifyImmediately(boolean notifyImmediately) {
		this.notifyImmediately = notifyImmediately;
	}
}
