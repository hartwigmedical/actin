package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.algo.datamodel.EvaluationResult

object ValueComparison {

    const val LARGER_THAN = ">"
    const val LARGER_THAN_OR_EQUAL = ">="
    const val SMALLER_THAN = "<"
    const val SMALLER_THAN_OR_EQUAL = "<="

    fun evaluateVersusMinValue(value: Double, comparator: String?, minValue: Double): EvaluationResult {
        val canBeDetermined = when (comparator) {
            LARGER_THAN -> value >= minValue
            LARGER_THAN_OR_EQUAL -> value >= minValue
            SMALLER_THAN -> value < minValue
            SMALLER_THAN_OR_EQUAL -> value < minValue
            else -> true
        }

        if (!canBeDetermined) {
            return EvaluationResult.UNDETERMINED
        }
        return if (value.compareTo(minValue) >= 0) EvaluationResult.PASS else EvaluationResult.FAIL
    }

    fun evaluateVersusMaxValue(value: Double, comparator: String?, maxValue: Double): EvaluationResult {
        val canBeDetermined = when (comparator) {
            LARGER_THAN -> value > maxValue
            LARGER_THAN_OR_EQUAL -> value > maxValue
            SMALLER_THAN -> value <= maxValue
            SMALLER_THAN_OR_EQUAL -> value <= maxValue
            else -> true
        }

        if (!canBeDetermined) {
            return EvaluationResult.UNDETERMINED
        }
        return if (value.compareTo(maxValue) <= 0) EvaluationResult.PASS else EvaluationResult.FAIL
    }

    fun stringCaseInsensitivelyMatchesQueryCollection(value: String, collection: Collection<String>): Boolean {
        return collection.any { termToFind: String ->
            value.lowercase().contains(termToFind.lowercase())
        }
    }
}