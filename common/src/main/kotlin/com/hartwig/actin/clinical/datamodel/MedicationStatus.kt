package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.Displayable

enum class MedicationStatus(private val display: String) : Displayable {
    ACTIVE("Active"),
    ON_HOLD("On hold"),
    CANCELLED("Cancelled"),
    UNKNOWN("Unknown");

    override fun display(): String {
        return display
    }
}
