package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.panel.Panel
import java.time.LocalDate

private val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF")

data class GenericPanel(
    val panelName: GenericPanelType,
    val date: LocalDate? = null,
    val fusions: List<GenericFusion> = emptyList(),
) : Panel {

    override fun testedGenes(): Set<String> {
        return GENERIC_PANEL_ALWAYS_TESTED_GENES
    }
}