package com.hartwig.actin.molecular.datamodel.panel

data class PanelVariantExtraction(
    val gene: String,
    val hgvsCodingOrProteinImpact: String,
    val variantAlleleFrequency: Double? = null
) : PanelEvent {
    override fun impactsGene(gene: String) = this.gene == gene

    override fun display() = "$gene $hgvsCodingOrProteinImpact"
}