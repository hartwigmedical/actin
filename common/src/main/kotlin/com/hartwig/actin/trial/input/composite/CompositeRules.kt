package com.hartwig.actin.trial.input.composite

import com.hartwig.actin.trial.input.EligibilityRule

object CompositeRules {

    val COMPOSITE_RULE_MAPPING = setOf(EligibilityRule.AND, EligibilityRule.OR, EligibilityRule.NOT, EligibilityRule.WARN_IF)

    fun isComposite(rule: EligibilityRule): Boolean {
        return COMPOSITE_RULE_MAPPING.contains(rule)
    }
}
