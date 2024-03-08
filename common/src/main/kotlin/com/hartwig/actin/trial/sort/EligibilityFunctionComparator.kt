package com.hartwig.actin.trial.sort

import com.hartwig.actin.trial.datamodel.EligibilityFunction

class EligibilityFunctionComparator : Comparator<EligibilityFunction> {

    private val comparator = Comparator.comparing { function: EligibilityFunction -> function.rule.toString() }
        .thenComparing { function: EligibilityFunction -> function.parameters.isEmpty() }
        .thenComparing { function: EligibilityFunction -> function.parameters.size }
        .thenComparing(EligibilityFunction::parameters, ::compareParameters)

    override fun compare(function1: EligibilityFunction, function2: EligibilityFunction): Int {
        return comparator.compare(function1, function2)
    }

    private fun compareParameters(parameters1: List<Any>, parameters2: List<Any>): Int {
        parameters1.zip(parameters2).map { (object1, object2) ->
            if (object1 is EligibilityFunction && object2 is EligibilityFunction) {
                val functionCompare: Int = INSTANCE.compare(object1, object2)
                if (functionCompare != 0) {
                    return functionCompare
                }
            } else if (object1 is String && object2 is String) {
                val stringCompare: Int = object1.compareTo(object2)
                if (stringCompare != 0) {
                    return stringCompare
                }
            } else {
                // Assume parameters can be either strings or eligibility functions
                return if (object1 is EligibilityFunction) 1 else -1
            }
        }
        return 0
    }

    companion object {
        private val INSTANCE: Comparator<EligibilityFunction> = EligibilityFunctionComparator()
    }
}
