package com.hartwig.actin.clinical.datamodel.treatment.history

import com.hartwig.actin.Displayable

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