package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

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
