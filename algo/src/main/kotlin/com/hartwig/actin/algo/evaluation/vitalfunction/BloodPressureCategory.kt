package com.hartwig.actin.algo.evaluation.vitalfunction

enum class BloodPressureCategory(private val display: String) {
    SYSTOLIC("Systolic blood pressure"), DIASTOLIC("Diastolic blood pressure");

    fun display(): String {
        return display
    }
}