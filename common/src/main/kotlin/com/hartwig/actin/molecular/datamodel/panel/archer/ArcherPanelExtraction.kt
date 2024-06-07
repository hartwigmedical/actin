package com.hartwig.actin.molecular.datamodel.panel.archer

import java.time.LocalDate

val ARCHER_ALWAYS_TESTED_GENES = setOf("ALK", "ROS1", "RET", "MET", "NTRK1", "NTRK2", "NTRK3", "NRG1")

data class ArcherPanelExtraction(
    val variants: List<ArcherVariantExtraction> = emptyList(),
    val fusions: List<ArcherFusionExtraction> = emptyList(),
    val skippedExons: List<ArcherSkippedExonsExtraction> = emptyList(),
    val date: LocalDate? = null
) {
    fun testedGenes(): Set<String> {
        return genesHavingResultsInPanel() + ARCHER_ALWAYS_TESTED_GENES
    }

    fun genesWithVariants(): Set<String> {
        return variants.map { it.gene }.toSet()
    }

    fun genesWithFusions(): Set<String> {
        return fusions.flatMap { listOf(it.gene) }.toSet()
    }

    fun events() = (variants + fusions + skippedExons).toSet()

    private fun genesHavingResultsInPanel(): Set<String> {
        return genesWithVariants() + genesWithFusions()
    }
}