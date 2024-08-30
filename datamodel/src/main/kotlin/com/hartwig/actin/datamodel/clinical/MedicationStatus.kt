package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class MedicationStatus(private val display: String) : Displayable {
    ACTIVE("Active"),
    ON_HOLD("On hold"),
    CANCELLED("Cancelled"),
    UNKNOWN("Unknown");

    override fun display(): String {
        return display
    }
}
