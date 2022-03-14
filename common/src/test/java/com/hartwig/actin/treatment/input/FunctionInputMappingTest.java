package com.hartwig.actin.treatment.input;

import static org.junit.Assert.assertTrue;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.composite.CompositeRules;

import org.junit.Test;

public class FunctionInputMappingTest {

    @Test
    public void everyRuleHasInputsConfigured() {
        for (EligibilityRule rule : EligibilityRule.values()) {
            if (!CompositeRules.isComposite(rule)) {
                assertTrue(FunctionInputMapping.RULE_INPUT_MAP.containsKey(rule));
            }
        }
    }
}