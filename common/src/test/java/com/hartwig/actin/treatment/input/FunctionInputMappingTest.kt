package com.hartwig.actin.treatment.input

import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.input.composite.CompositeRules.isComposite
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FunctionInputMappingTest {
    @Test
    fun everyRuleHasInputsConfigured() {
        for (rule in EligibilityRule.values()) {
            if (!isComposite(rule)) {
                assertThat(FunctionInputMapping.RULE_INPUT_MAP.containsKey(rule)).isTrue
            }
        }
    }
}