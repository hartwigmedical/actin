package com.hartwig.actin.molecular.datamodel.panel

data class PanelSkippedExonsExtraction(val gene: String, val start: Int, val end: Int, val transcript: String? = null) : PanelEvent {
    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun display(): String {
        return "$gene skipped exons $start-$end"
    }
}