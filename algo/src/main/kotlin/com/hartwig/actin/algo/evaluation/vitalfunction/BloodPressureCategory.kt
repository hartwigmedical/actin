package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.datamodel.Displayable

enum class BloodPressureCategory(private val display: String) : Displayable {
    SYSTOLIC("Systolic blood pressure"),
    DIASTOLIC("Diastolic blood pressure");

    override fun display(): String {
        return display
    }
}