package com.hartwig.actin.trial.input

import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.input.composite.CompositeRules.isComposite
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FunctionInputMappingTest {

    @Test
    fun `Should have configured every rule in terms of having function input`() {
        for (rule in EligibilityRule.values()) {
            if (!isComposite(rule)) {
                assertThat(FunctionInputMapping.RULE_INPUT_MAP.containsKey(rule)).isTrue
            }
        }
    }
}