package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.panel.Panel

val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF", "KRAS")

data class GenericPanel(
    val panelName: GenericPanelType,
    val fusions: List<GenericFusion> = emptyList(),
) : Panel {

    override fun testedGenes(): Set<String> {
        return fusions.map { it.geneStart }.toSet() + fusions.map { it.geneEnd }.toSet() +
                if (panelName == GenericPanelType.FREE_TEXT) emptySet() else GENERIC_PANEL_ALWAYS_TESTED_GENES
    }
}