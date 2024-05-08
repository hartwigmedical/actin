package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class ArcherSkippedExons(val gene: String, val start: Int, val end: Int) : PanelEvent {

    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun display(): String {
        return gene
    }
}