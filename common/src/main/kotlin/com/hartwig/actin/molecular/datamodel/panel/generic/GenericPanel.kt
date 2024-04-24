package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.panel.Panel

val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF", "KRAS")

data class GenericPanel(
    val panelName: GenericPanelType
) : Panel {

    override fun testedGenes(): Set<String> {
        return GENERIC_PANEL_ALWAYS_TESTED_GENES
    }
}