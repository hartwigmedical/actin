package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelFusion

data class ArcherFusion(
    val geneStart: String,
    val geneEnd: String,
) : PanelFusion {
    override fun event(): String {
        return "$geneStart::$geneEnd"
    }
}