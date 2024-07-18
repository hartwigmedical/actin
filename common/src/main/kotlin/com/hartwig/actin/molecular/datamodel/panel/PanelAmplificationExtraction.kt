package com.hartwig.actin.molecular.datamodel.panel

data class PanelAmplificationExtraction(val gene: String, val chromosome: String) : PanelEvent {
    override fun impactsGene(gene: String): Boolean {
        return gene == this.gene
    }

    override fun display(): String {
        return "$gene amp"
    }
}