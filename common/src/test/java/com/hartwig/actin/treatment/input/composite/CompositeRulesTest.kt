package com.hartwig.actin.treatment.input.composite

import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.input.composite.CompositeRules.inputsForCompositeRule
import com.hartwig.actin.treatment.input.composite.CompositeRules.isComposite
import org.junit.Assert
import org.junit.Test

class CompositeRulesTest {
    @Test
    fun canInterpretCompositeRules() {
        val firstCompositeRule = CompositeRules.COMPOSITE_RULE_MAPPING.keys.iterator().next()
        Assert.assertTrue(isComposite(firstCompositeRule))
        Assert.assertNotNull(inputsForCompositeRule(firstCompositeRule))
        Assert.assertFalse(isComposite(EligibilityRule.HAS_ACTIVE_INFECTION))
    }
}