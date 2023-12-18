package com.hartwig.actin.treatment.util

import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.input.composite.CompositeRules
import java.util.*

object EligibilityFunctionDisplay {
    @JvmStatic
    fun format(function: EligibilityFunction): String {
        var value: String = function.rule().toString()
        if (CompositeRules.isComposite(function.rule())) {
            value = value + "("
            val joiner: StringJoiner = StringJoiner(", ")
            for (input: Any? in function.parameters()) {
                joiner.add(format((input as EligibilityFunction?)!!))
            }
            value = value + joiner + ")"
        } else if (!function.parameters().isEmpty()) {
            value = value + "["
            val joiner: StringJoiner = StringJoiner(", ")
            for (input: Any? in function.parameters()) {
                joiner.add(input as String?)
            }
            value = value + joiner + "]"
        }
        return value
    }
}
