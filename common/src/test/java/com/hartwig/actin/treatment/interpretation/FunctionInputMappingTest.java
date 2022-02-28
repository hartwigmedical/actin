package com.hartwig.actin.treatment.interpretation;

import static org.junit.Assert.assertTrue;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.composite.CompositeRules;

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