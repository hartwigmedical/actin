package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class ArcherVariantExtraction(
    val gene: String,
    val hgvsCodingImpact: String
) : PanelEvent {
    override fun impactsGene(gene: String) = this.gene == gene

    override fun display() = "$gene $hgvsCodingImpact"
}