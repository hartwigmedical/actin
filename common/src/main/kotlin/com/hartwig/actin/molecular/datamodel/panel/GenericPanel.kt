package com.hartwig.actin.molecular.datamodel.panel

import java.time.LocalDate

val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF")

data class GenericPanel(
    val panelName: GenericPanelType,
    val date: LocalDate?,
) {
    fun testedGenes(): Set<String> {
        return GENERIC_PANEL_ALWAYS_TESTED_GENES
    }
}