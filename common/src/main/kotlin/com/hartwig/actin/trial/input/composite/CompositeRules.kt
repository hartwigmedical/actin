package com.hartwig.actin.trial.input.composite

import com.hartwig.actin.trial.input.EligibilityRule

object CompositeRules {

    val COMPOSITE_RULES = setOf(EligibilityRule.AND, EligibilityRule.OR, EligibilityRule.NOT, EligibilityRule.WARN_IF)

    fun isComposite(rule: EligibilityRule): Boolean {
        return COMPOSITE_RULES.contains(rule)
    }
}
