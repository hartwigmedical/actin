package com.hartwig.actin.trial.input.composite

import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.trial.input.composite.CompositeRules.inputsForCompositeRule
import com.hartwig.actin.trial.input.composite.CompositeRules.isComposite
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CompositeRulesTest {

    @Test
    fun `Should interpret composite rules`() {
        val firstCompositeRule = CompositeRules.COMPOSITE_RULE_MAPPING.keys.first()
        assertThat(isComposite(firstCompositeRule)).isTrue
        assertThat(inputsForCompositeRule(firstCompositeRule)).isNotNull()
        assertThat(isComposite(EligibilityRule.HAS_ACTIVE_INFECTION)).isFalse()
    }
}