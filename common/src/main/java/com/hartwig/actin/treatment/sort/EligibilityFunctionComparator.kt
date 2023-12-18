package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.EligibilityFunction

class EligibilityFunctionComparator() : Comparator<EligibilityFunction> {
    public override fun compare(function1: EligibilityFunction, function2: EligibilityFunction): Int {
        val ruleCompare: Int = function1.rule().toString().compareTo(function2.rule().toString())
        return if (ruleCompare != 0) ruleCompare else paramCompare(function1.parameters(), function2.parameters())
    }

    companion object {
        private val INSTANCE: Comparator<EligibilityFunction> = EligibilityFunctionComparator()
        private fun paramCompare(parameters1: List<Any>, parameters2: List<Any>): Int {
            if (parameters1.isEmpty() && parameters2.isEmpty()) {
                return 0
            } else if (parameters1.isEmpty()) {
                return 1
            } else if (parameters2.isEmpty()) {
                return -1
            }
            val sizeCompare: Int = parameters1.size - parameters2.size
            if (sizeCompare == 0) {
                for (i in parameters1.indices) {
                    val object1: Any = parameters1.get(i)
                    val object2: Any = parameters2.get(i)
                    if (object1 is EligibilityFunction && object2 is EligibilityFunction) {
                        val functionCompare: Int = INSTANCE.compare(object1, object2)
                        if (functionCompare != 0) {
                            return functionCompare
                        }
                    } else if (object1 is String && object2 is String) {
                        val stringCompare: Int = object1.compareTo((object2 as String?)!!)
                        if (stringCompare != 0) {
                            return 0
                        }
                    } else {
                        // Assume parameters can be either strings or eligibility functions
                        return if (object1 is EligibilityFunction) 1 else -1
                    }
                }
                return 0
            } else {
                return sizeCompare
            }
        }
    }
}
