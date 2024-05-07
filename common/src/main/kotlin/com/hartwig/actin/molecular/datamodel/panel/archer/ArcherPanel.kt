package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.Panel
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

val ARCHER_ALWAYS_TESTED_GENES = setOf("ALK", "ROS1", "RET", "MET", "NTRK1", "NTRK2", "NTRK3", "NRG1")

data class ArcherPanel(
    val variants: List<ArcherVariant> = emptyList(),
    val fusions: List<ArcherFusion> = emptyList(),
    val skippedExons: List<ArcherSkippedExons> = emptyList()
) : Panel {

    override fun testedGenes(): Set<String> {
        return genesHavingResultsInPanel() + ARCHER_ALWAYS_TESTED_GENES
    }

    override fun variants(): List<PanelEvent> {
        return variants
    }

    override fun fusions(): List<PanelEvent> {
        return fusions
    }

    override fun events(): List<PanelEvent> {
        return super.events() + skippedExons
    }

    fun genesWithVariants(): Set<String> {
        return variants.map { it.gene }.toSet()
    }

    fun genesWithFusions(): Set<String> {
        return fusions.flatMap { listOf(it.gene) }.toSet()
    }

    private fun genesHavingResultsInPanel(): Set<String> {
        return genesWithVariants() + genesWithFusions()
    }

}