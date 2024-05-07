package com.hartwig.actin.molecular.datamodel.panel

interface PanelEvent {

    fun impactsGene(gene: String): Boolean

    fun eventDisplay(): String
}