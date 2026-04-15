package com.hartwig.actin.algo.evaluation.util

import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.EvaluationResult.FAIL
import com.hartwig.actin.datamodel.algo.EvaluationResult.PASS
import com.hartwig.actin.datamodel.algo.EvaluationResult.UNDETERMINED

object ValueComparison {

    const val LARGER_THAN = ">"
    const val LARGER_THAN_OR_EQUAL = ">="
    const val SMALLER_THAN = "<"
    const val SMALLER_THAN_OR_EQUAL = "<="

    fun evaluateVersusMinValue(value: Double, comparator: String?, minValue: Double): EvaluationResult {
        return when (comparator) {
            LARGER_THAN, LARGER_THAN_OR_EQUAL -> if (value >= minValue) PASS else UNDETERMINED
            SMALLER_THAN -> if (value <= minValue) FAIL else UNDETERMINED
            SMALLER_THAN_OR_EQUAL -> if (value < minValue) FAIL else UNDETERMINED
            else -> if (value >= minValue) PASS else FAIL
        }
    }

    fun evaluateVersusMaxValue(value: Double, comparator: String?, maxValue: Double): EvaluationResult {
        return when (comparator) {
            SMALLER_THAN, SMALLER_THAN_OR_EQUAL -> if (value <= maxValue) PASS else UNDETERMINED
            LARGER_THAN -> if (value >= maxValue) FAIL else UNDETERMINED
            LARGER_THAN_OR_EQUAL -> if (value > maxValue) FAIL else UNDETERMINED
            else -> if (value <= maxValue) PASS else FAIL
        }
    }

    fun evaluateBoundsVersusMinValue(lowerBound: Double?, upperBound: Double?, minValue: Double, isUpperInclusive: Boolean?): EvaluationResult =
        lowerBound?.takeIf { it >= minValue }?.let { PASS }
            ?: upperBound?.takeIf { if (isUpperInclusive ?: true) it < minValue else it <= minValue }?.let { FAIL }
            ?: UNDETERMINED

    fun evaluateBoundsVersusMaxValue(lowerBound: Double?, upperBound: Double?, maxValue: Double, isLowerInclusive: Boolean?): EvaluationResult =
        upperBound?.takeIf { it <= maxValue }?.let { PASS }
            ?: lowerBound?.takeIf { if (isLowerInclusive ?: true) it > maxValue else it >= maxValue }?.let { FAIL }
            ?: UNDETERMINED

    fun stringCaseInsensitivelyMatchesQueryCollection(value: String, collection: Collection<String>): Boolean {
        return collection.any { termToFind: String ->
            value.lowercase().contains(termToFind.lowercase())
        }
    }
}