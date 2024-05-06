package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class ArcherFusion(
    val gene: String
) : PanelEvent {
    override fun event(): String {
        return gene
    }
}