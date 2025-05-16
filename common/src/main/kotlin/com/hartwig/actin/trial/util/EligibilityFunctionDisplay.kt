package com.hartwig.actin.trial.util

import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.trial.input.composite.CompositeRules

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
