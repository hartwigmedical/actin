package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class ArcherVariant(
    val gene: String,
    val hgvsCodingImpact: String
) : PanelEvent {
    override fun event(): String {
        return "$gene $hgvsCodingImpact"
    }
}