package com.hartwig.actin.trial.util

import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.trial.input.composite.CompositeRules
import com.hartwig.actin.trial.input.ruleAsEnum

object EligibilityFunctionDisplay {

    fun format(function: EligibilityFunction): String {
        return function.ruleAsEnum().toString() + when {
            CompositeRules.isComposite(function.ruleAsEnum()) -> {
                "(${function.parameters.joinToString(", ") { format(it as EligibilityFunction) }})"
            }

            function.parameters.isNotEmpty() -> {
                "[${function.parameters.joinToString(", ")}]"
            }

            else -> ""
        }
    }
}
