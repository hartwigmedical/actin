package com.hartwig.actin.treatment.input.composite

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Maps
import com.hartwig.actin.treatment.datamodel.EligibilityRule

object CompositeRules {
    @JvmField
    @VisibleForTesting
    val COMPOSITE_RULE_MAPPING: Map<EligibilityRule, CompositeInput> = Maps.newHashMap()

    init {
        COMPOSITE_RULE_MAPPING.put(EligibilityRule.AND, CompositeInput.AT_LEAST_2)
        COMPOSITE_RULE_MAPPING.put(EligibilityRule.OR, CompositeInput.AT_LEAST_2)
        COMPOSITE_RULE_MAPPING.put(EligibilityRule.NOT, CompositeInput.EXACTLY_1)
        COMPOSITE_RULE_MAPPING.put(EligibilityRule.WARN_IF, CompositeInput.EXACTLY_1)
    }

    @JvmStatic
    fun isComposite(rule: EligibilityRule): Boolean {
        return COMPOSITE_RULE_MAPPING.containsKey(rule)
    }

    @JvmStatic
    fun inputsForCompositeRule(compositeRule: EligibilityRule): CompositeInput {
        return (COMPOSITE_RULE_MAPPING.get(compositeRule))!!
    }
}
