package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

data class ArcherFusion(
    val gene: String
) : PanelEvent {

    override fun impactsGene(gene: String): Boolean {
        return this.gene == gene
    }

    override fun eventDisplay(): String {
        return gene
    }
}