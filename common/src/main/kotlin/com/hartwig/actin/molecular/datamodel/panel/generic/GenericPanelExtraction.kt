package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.panel.PanelAmplificationExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import java.time.LocalDate

val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF", "KRAS")

data class GenericPanelExtraction(
    val fusions: List<GenericFusionExtraction> = emptyList(),
    val exonDeletions: List<GenericExonDeletionExtraction> = emptyList(),
    val genesWithNegativeResults: Set<String> = emptySet(),
    override val panelType: String,
    override val amplifications: List<PanelAmplificationExtraction> = emptyList(),
    override val variants: List<PanelVariantExtraction> = emptyList(),
    override val date: LocalDate? = null,
    override val tmb: Double? = null,
    override val msi: Boolean? = null
) : PanelExtraction {
    override fun testedGenes(): Set<String> {
        return genesHavingResultsInPanel() + alwaysTestedGenes() + genesWithNegativeResults
    }

    override fun events() = (variants + fusions + exonDeletions).toSet()

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
        return when {
            panelType.lowercase().contains("avl") -> GENERIC_PANEL_ALWAYS_TESTED_GENES
            else -> emptySet()
        }
    }

    fun genesHavingResultsInPanel(): Set<String> {
        return genesWithVariants() + genesWithFusions() + genesWithExonDeletions()
    }
}