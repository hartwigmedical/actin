package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelVariant

data class ArcherVariant(
    val gene: String,
    val hgvsCodingImpact: String
) : PanelVariant {
    override fun event(): String {
        return "$gene $hgvsCodingImpact"
    }
}