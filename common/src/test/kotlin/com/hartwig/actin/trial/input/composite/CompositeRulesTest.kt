package com.hartwig.actin.trial.input.composite

import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.trial.input.composite.CompositeRules.isComposite
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CompositeRulesTest {

    @Test
    fun `Should identify composite rules`() {
        val firstCompositeRule = CompositeRules.COMPOSITE_RULES.first()
        assertThat(isComposite(firstCompositeRule)).isTrue
        assertThat(isComposite(EligibilityRule.HAS_ACTIVE_INFECTION)).isFalse()
    }
}
