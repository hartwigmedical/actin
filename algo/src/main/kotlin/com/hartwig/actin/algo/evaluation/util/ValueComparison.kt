package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.algo.datamodel.EvaluationResult

object ValueComparison {

    const val LARGER_THAN = ">"
    const val LARGER_THAN_OR_EQUAL = ">="
    const val SMALLER_THAN = "<"
    const val SMALLER_THAN_OR_EQUAL = "<="

    fun evaluateVersusMinValue(value: Double, comparator: String?, minValue: Double): EvaluationResult {
        if (!canBeDetermined(value, comparator, minValue)) {
            return EvaluationResult.UNDETERMINED
        }
        return if (value.compareTo(minValue) >= 0) EvaluationResult.PASS else EvaluationResult.FAIL
    }

    fun evaluateVersusMaxValue(value: Double, comparator: String?, maxValue: Double): EvaluationResult {
        if (!canBeDetermined(value, comparator, maxValue)) {
            return EvaluationResult.UNDETERMINED
        }
        return if (value.compareTo(maxValue) <= 0) EvaluationResult.PASS else EvaluationResult.FAIL
    }

    private fun canBeDetermined(value: Double, comparator: String?, refValue: Double): Boolean {
        return when (comparator) {
            LARGER_THAN -> value > refValue
            LARGER_THAN_OR_EQUAL -> value >= refValue
            SMALLER_THAN -> value < refValue
            SMALLER_THAN_OR_EQUAL -> value <= refValue
            else -> true
        }
    }

    fun stringCaseInsensitivelyMatchesQueryCollection(value: String, collection: Collection<String>): Boolean {
        return collection.any { termToFind: String ->
            value.lowercase().contains(termToFind.lowercase())
        }
    }
}