package com.hartwig.actin.treatment.input

import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.input.composite.CompositeRules.isComposite
import org.junit.Assert
import org.junit.Test

class FunctionInputMappingTest {
    @Test
    fun everyRuleHasInputsConfigured() {
        for (rule in EligibilityRule.values()) {
            if (!isComposite(rule)) {
                Assert.assertTrue(FunctionInputMapping.RULE_INPUT_MAP.containsKey(rule))
            }
        }
    }
}