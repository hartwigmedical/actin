package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory

object VitalFunctionCategoryResolver {
    fun determineCategory(string: String): VitalFunctionCategory {
        return toCategory(string)
            ?: throw IllegalArgumentException("Could not determine category for vital function: $string")
    }

    fun toCategory(string: String): VitalFunctionCategory? {
        return when (string) {
            "NIBP", "NIBPLILI", "NIBPLIRE", "NIBP LI", "NIBP ST 1m", "NIBP ST 3m", "NIBP ST 5m", "NIBP ST no" -> VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
            "ABP" -> VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
            "HR" -> VitalFunctionCategory.HEART_RATE
            "SpO2" -> VitalFunctionCategory.SPO2
            else -> null
        }
    }
}