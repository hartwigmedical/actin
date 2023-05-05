package com.hartwig.actin.algo.evaluation.cardiacfunction

internal enum class ECGUnit(private val symbol: String) {
    MILLISECONDS("ms");

    fun symbol(): String {
        return symbol
    }
}