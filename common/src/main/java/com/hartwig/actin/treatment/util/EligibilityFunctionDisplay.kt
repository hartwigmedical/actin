package com.hartwig.actin.treatment.util

import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.input.composite.CompositeRules

object EligibilityFunctionDisplay {
    fun format(function: EligibilityFunction): String {
        return function.rule.toString() + when {
            CompositeRules.isComposite(function.rule) -> {
                "(${function.parameters.joinToString(", ") { format(it as EligibilityFunction) }})"
            }

            function.parameters.isNotEmpty() -> {
                "[${function.parameters.joinToString(", ")}]"
            }

            else -> ""
        }
    }
}
