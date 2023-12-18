package com.hartwig.actin.treatment.input.composite

import com.hartwig.actin.treatment.datamodel.EligibilityRule

object CompositeRules {
    val COMPOSITE_RULE_MAPPING = mapOf(
        EligibilityRule.AND to CompositeInput.AT_LEAST_2,
        EligibilityRule.OR to CompositeInput.AT_LEAST_2,
        EligibilityRule.NOT to CompositeInput.EXACTLY_1,
        EligibilityRule.WARN_IF to CompositeInput.EXACTLY_1,
    )

    fun isComposite(rule: EligibilityRule): Boolean {
        return COMPOSITE_RULE_MAPPING.containsKey(rule)
    }

    fun inputsForCompositeRule(compositeRule: EligibilityRule): CompositeInput {
        return (COMPOSITE_RULE_MAPPING[compositeRule])!!
    }
}
