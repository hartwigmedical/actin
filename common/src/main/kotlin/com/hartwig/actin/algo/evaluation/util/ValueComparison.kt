package com.hartwig.actin.algo.evaluation.util

object ValueComparison {

    const val LARGER_THAN = ">"
    const val LARGER_THAN_OR_EQUAL = ">="
    const val SMALLER_THAN = "<"
    const val SMALLER_THAN_OR_EQUAL = "<="

    fun evaluateVersusMinValue(value: Double, comparator: String?, minValue: Double): Boolean? {
        return when (comparator) {
            LARGER_THAN, LARGER_THAN_OR_EQUAL -> if (value >= minValue) true else null
            SMALLER_THAN -> if (value <= minValue) false else null
            SMALLER_THAN_OR_EQUAL -> if (value < minValue) false else null
            else -> if (value >= minValue) true else false
        }
    }

    fun evaluateVersusMaxValue(value: Double, comparator: String?, maxValue: Double): Boolean? {
        return when (comparator) {
            SMALLER_THAN, SMALLER_THAN_OR_EQUAL -> if (value <= maxValue) true else null
            LARGER_THAN -> if (value >= maxValue) false else null
            LARGER_THAN_OR_EQUAL -> if (value > maxValue) false else null
            else -> if (value <= maxValue) true else false
        }
    }

    fun stringCaseInsensitivelyMatchesQueryCollection(value: String, collection: Collection<String>): Boolean {
        return collection.any { termToFind: String ->
            value.lowercase().contains(termToFind.lowercase())
        }
    }
}