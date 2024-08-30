package com.hartwig.actin.clinical.feed.emc.vitalfunction

import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory

object VitalFunctionExtraction {
    fun determineCategory(string: String): VitalFunctionCategory {
        return toCategory(string)
            ?: throw IllegalArgumentException("Could not determine category for vital function: $string")
    }

    fun toCategory(string: String): VitalFunctionCategory? {
        return when (string) {
            "NIBP", "NIBPLILI", "NIBPLIRE" -> VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
            "ABP" -> VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
            "HR" -> VitalFunctionCategory.HEART_RATE
            "SpO2" -> VitalFunctionCategory.SPO2
            else -> null
        }
    }
}