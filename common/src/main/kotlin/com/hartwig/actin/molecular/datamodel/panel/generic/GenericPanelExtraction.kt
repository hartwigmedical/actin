package com.hartwig.actin.molecular.datamodel.panel.generic

import java.time.LocalDate

val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF", "KRAS")

data class GenericPanelExtraction(
    val panelType: GenericPanelType,
    val variants: List<GenericVariantExtraction> = emptyList(),
    val fusions: List<GenericFusionExtraction> = emptyList(),
    val exonDeletions: List<GenericExonDeletionExtraction> = emptyList(),
    val genesWithNegativeResults: Set<String> = emptySet(),
    val date: LocalDate? = null,
) {
    fun testedGenes(): Set<String> {
        return genesHavingResultsInPanel() + alwaysTestedGenes()
    }

    fun events() = (variants + fusions + exonDeletions).toSet()

    private fun genesWithVariants(): Set<String> {
        return variants.map { it.gene }.toSet()
    }

    private fun genesWithFusions(): Set<String> {
        return fusions.flatMap { listOf(it.geneStart, it.geneEnd) }.toSet()
    }

    private fun genesWithExonDeletions(): Set<String> {
        return exonDeletions.map { it.gene }.toSet()
    }

    private fun alwaysTestedGenes(): Set<String> {
        return when (panelType) {
            GenericPanelType.FREE_TEXT -> emptySet()
            else -> GENERIC_PANEL_ALWAYS_TESTED_GENES
        }
    }

    fun genesHavingResultsInPanel(): Set<String> {
        return genesWithVariants() + genesWithFusions() + genesWithExonDeletions() + genesWithNegativeResults
    }
}