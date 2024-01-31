package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.Displayable

enum class VitalFunctionCategory(private val display: String) : Displayable {
    NON_INVASIVE_BLOOD_PRESSURE("Non-invasive blood pressure"),
    ARTERIAL_BLOOD_PRESSURE("Arterial blood pressure"),
    HEART_RATE("Heart rate"),
    SPO2("SpO2"),
    OTHER("Other");

    override fun display(): String {
        return display
    }
}
