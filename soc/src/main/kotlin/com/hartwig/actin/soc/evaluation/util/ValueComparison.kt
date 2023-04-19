package com.hartwig.actin.soc.evaluation.util

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.util.ApplicationConfig
import java.util.Locale

object ValueComparison {
    const val LARGER_THAN = ">"
    const val LARGER_THAN_OR_EQUAL = ">="
    const val SMALLER_THAN = "<"
    const val SMALLER_THAN_OR_EQUAL = "<="
    fun evaluateVersusMinValue(value: Double, comparator: String?, minValue: Double): EvaluationResult {
        if (!canBeDetermined(value, comparator, minValue)) {
            return EvaluationResult.UNDETERMINED
        }
        return if (java.lang.Double.compare(value, minValue) >= 0) EvaluationResult.PASS else EvaluationResult.FAIL
    }

    fun evaluateVersusMaxValue(value: Double, comparator: String?, maxValue: Double): EvaluationResult {
        if (!canBeDetermined(value, comparator, maxValue)) {
            return EvaluationResult.UNDETERMINED
        }
        return if (java.lang.Double.compare(value, maxValue) <= 0) EvaluationResult.PASS else EvaluationResult.FAIL
    }

    private fun canBeDetermined(value: Double, comparator: String?, refValue: Double): Boolean {
        return if (comparator == null) {
            true
        } else when (comparator) {
            LARGER_THAN -> {
                value > refValue
            }

            LARGER_THAN_OR_EQUAL -> {
                value >= refValue
            }

            SMALLER_THAN -> {
                value < refValue
            }

            SMALLER_THAN_OR_EQUAL -> {
                value <= refValue
            }

            else -> {
                true
            }
        }
    }

    fun stringCaseInsensitivelyMatchesQueryCollection(value: String, collection: Collection<String>): Boolean {
        return collection.any { termToFind: String ->
            value.lowercase(ApplicationConfig.LOCALE).contains(termToFind.lowercase(ApplicationConfig.LOCALE))
        }
    }
}