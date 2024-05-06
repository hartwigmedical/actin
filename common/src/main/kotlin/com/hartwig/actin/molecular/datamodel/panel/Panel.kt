package com.hartwig.actin.molecular.datamodel.panel

interface Panel {
    fun testedGenes(): Set<String>
    fun alwaysTestedGenes(): Set<String>
    fun events(): List<PanelEvent>
    fun eventsForGene(gene: String): List<PanelEvent>
}