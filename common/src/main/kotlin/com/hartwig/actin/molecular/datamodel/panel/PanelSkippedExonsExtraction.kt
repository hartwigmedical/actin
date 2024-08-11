package com.hartwig.actin.molecular.datamodel.panel

// TODO note that we have exons here without knowing the transcript!
data class PanelSkippedExonsExtraction(val gene: String, val start: Int, val end: Int) : PanelEvent {
    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun display(): String {
        return "$gene skipped exons $start-$end"
    }
}