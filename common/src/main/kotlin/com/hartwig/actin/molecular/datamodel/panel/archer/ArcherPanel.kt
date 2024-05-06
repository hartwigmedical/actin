package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.Panel
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

val ARCHER_ALWAYS_TESTED_GENES = setOf("ALK", "ROS1", "RET", "MET", "NTRK1", "NTRK2", "NTRK3", "NRG1")

data class ArcherPanel(
    val variants: List<ArcherVariant>,
    val fusions: List<ArcherFusion>,
    val skippedExons: List<ArcherSkippedExons>
) : Panel {

    override fun testedGenes(): Set<String> {
        return genesHavingResultsInPanel() + alwaysTestedGenes()
    }

    override fun alwaysTestedGenes(): Set<String> {
        return ARCHER_ALWAYS_TESTED_GENES
    }

    override fun events(): List<PanelEvent> {
        return variants + fusions
    }

    override fun eventsForGene(gene: String): List<PanelEvent> {
        return variants.filter { it.gene == gene } + fusions.filter { it.gene == gene }
    }

    fun genesWithVariants(): Set<String> {
        return variants.map { it.gene }.toSet()
    }

    private fun genesWithFusions(): Set<String> {
        return fusions.flatMap { listOf(it.gene) }.toSet()
    }

    private fun genesHavingResultsInPanel(): Set<String> {
        return genesWithVariants() + genesWithFusions()
    }

}