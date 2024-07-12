package com.hartwig.actin.molecular.datamodel.panel

import java.time.LocalDate

data class McgiAmplification(val gene: String, val chromosome: String) : PanelEvent {
    override fun impactsGene(gene: String): Boolean {
        return gene == this.gene
    }

    override fun display(): String {
        return "$gene chr$chromosome amplified"
    }
}

data class McgiExtraction(
    override val panelType: String,
    override val date: LocalDate?,
    override val variants: List<PanelVariantExtraction>,
    override val amplifications: List<McgiAmplification>,
    override val msi: Boolean?,
    override val tmb: Double?,
) : PanelExtraction {
    override fun testedGenes(): Set<String> {
        return (variants.map { it.gene } + amplifications.map { it.gene }).toSet()
    }

    override fun events(): Set<PanelEvent> {
        return (variants + amplifications).toSet()
    }
}
