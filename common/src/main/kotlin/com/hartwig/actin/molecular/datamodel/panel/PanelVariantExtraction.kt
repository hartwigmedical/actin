package com.hartwig.actin.molecular.datamodel.panel

data class PanelVariantExtraction(
    val gene: String,
    val hgvsImpact: String,
    val vaf: Double? = 0.0
) : PanelEvent {
    override fun impactsGene(gene: String) = this.gene == gene

    override fun display() = "$gene $hgvsImpact"
}