package com.catas.wicked;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.condition.OperatingSystem;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

/**
 * junit5
 * check test condition
 */
public class ConditionalTestExecutionExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (checkDefaultCondition(context) || checkOsCondition(context)) {
            return ConditionEvaluationResult.enabled("Enabled test: " + context.getDisplayName());
        } else {
            return ConditionEvaluationResult.disabled("Skip unqualified test: " + context.getDisplayName());
        }
    }

    private boolean checkDefaultCondition(ExtensionContext context) {
        String condition = context.getElement()
                .map(el -> el.getAnnotation(ConditionalTest.class))
                .map(ConditionalTest::value)
                .orElse("false");

        return "true".equalsIgnoreCase(condition);
    }

    private boolean checkOsCondition(ExtensionContext context) {
        Requires.Family[] condition = context.getElement()
                .map(el -> el.getAnnotation(ConditionalTest.class))
                .map(ConditionalTest::os)
                .orElse(new Requires.Family[]{});

        Requires.Family os = OperatingSystem.getCurrent().getFamily();
        return Arrays.asList(condition).contains(os);
    }
}
