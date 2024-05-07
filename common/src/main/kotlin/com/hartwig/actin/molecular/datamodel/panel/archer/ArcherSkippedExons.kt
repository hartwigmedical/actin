package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

class ArcherSkippedExons(val gene: String, val start: Int, val end: Int) : PanelEvent {

    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun eventDisplay(): String {
        return gene
    }
}