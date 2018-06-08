package com.rprescott.fileprocessor.validation.rules;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class ValidationRules implements InitializingBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationRules.class);
	private Map<Integer, Class<? extends AbstractInputValidationRule>> rules = new HashMap<>();
	
	public Map<Integer, Class<? extends AbstractInputValidationRule>> getRules() {
		return rules;
	}
	
	public Class<? extends AbstractInputValidationRule> get(Integer ruleId) {
		return rules.get(ruleId);
	}
	
	public AbstractInputValidationRule getInstance(Integer ruleId, Object metadata, boolean notifyImmediately) {
		AbstractInputValidationRule ret = null;
		try {
			ret = rules.get(ruleId).getDeclaredConstructor(Object.class, Boolean.TYPE).newInstance(metadata, notifyImmediately);
		}
		catch (ReflectiveOperationException ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
		return ret;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Reflections reflections = new Reflections("com.rprescott.fileprocessor");
		Set<Class<? extends AbstractInputValidationRule>> inputValidationRules = reflections.getSubTypesOf(AbstractInputValidationRule.class);
		// TODO: Why am I creating a new instance of the rule to put into the map of RuleID --> AbstractInputValidationRule. Why not just use the concrete class type
		// that should be obtained when I'm in the for loop? When ValidationRules.getInstance() is called, we construct a new instance anyway.
		for (Class<? extends AbstractInputValidationRule> rule : inputValidationRules) {
			try {
				Integer ruleId = rule.getDeclaredConstructor(Object.class).newInstance(new Object()).getRuleId();
				rules.put(ruleId, rule);
				LOGGER.info("Loaded rule with ID: {} ",
					rule.getDeclaredConstructor(Object.class).newInstance(new Object()).getRuleId());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
}
