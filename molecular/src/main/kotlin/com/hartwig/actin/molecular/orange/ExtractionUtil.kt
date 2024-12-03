package com.hartwig.actin.molecular.orange

internal object ExtractionUtil {

    fun keep3Digits(input: Double): Double {
        return Math.round(input * 1000) / 1000.0
    }
}