package com.hartwig.actin.molecular.datamodel.panel

data class PanelVariantExtraction(
    val gene: String,
    val hgvsCodingOrProteinImpact: String
) : PanelEvent {
    override fun impactsGene(gene: String) = this.gene == gene

    override fun display() = "$gene $hgvsCodingOrProteinImpact"
}