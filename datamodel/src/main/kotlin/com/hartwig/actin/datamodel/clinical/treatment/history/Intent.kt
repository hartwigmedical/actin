package com.hartwig.actin.datamodel.clinical.treatment.history

import com.hartwig.actin.datamodel.Displayable

enum class Intent(private val display: String) : Displayable {
    ADJUVANT("Adjuvant"),
    NEOADJUVANT("Neoadjuvant"),
    INDUCTION("Induction"),
    CONSOLIDATION("Consolidation"),
    MAINTENANCE("Maintenance"),
    CURATIVE("Curative"),
    PALLIATIVE("Palliative");

    override fun display(): String {
        return display
    }
}