package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.AVL_PANEL
import com.hartwig.actin.molecular.datamodel.panel.*
import java.time.LocalDate

val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF", "KRAS")

data class GenericPanelExtraction(
    override val fusions: List<PanelFusionExtraction> = emptyList(),
    override val skippedExons: List<PanelSkippedExonsExtraction> = emptyList(),
    val exonDeletions: List<GenericExonDeletionExtraction> = emptyList(),
    val genesWithNegativeResults: Set<String> = emptySet(),
    override val panelType: String,
    override val amplifications: List<PanelAmplificationExtraction> = emptyList(),
    override val variants: List<PanelVariantExtraction> = emptyList(),
    override val date: LocalDate? = null,
    override val tumorMutationalBurden: Double? = null,
    override val isMicrosatelliteUnstable: Boolean? = null,
    override val extractionClass: String = GenericPanelExtraction::class.java.simpleName
) : PanelExtraction {
    override fun testedGenes(): Set<String> {
        return genesHavingResultsInPanel() + alwaysTestedGenes() + genesWithNegativeResults
    }

    override fun events() = (variants + fusions + exonDeletions).toSet()

    private fun genesWithVariants(): Set<String> {
        return variants.map { it.gene }.toSet()
    }

    private fun genesWithFusions(): Set<String> {
        return fusions.flatMap { listOfNotNull(it.geneUp, it.geneDown) }.toSet()
    }

    private fun genesWithExonDeletions(): Set<String> {
        return exonDeletions.map { it.gene }.toSet()
    }

    private fun alwaysTestedGenes(): Set<String> {
        return if (panelType == AVL_PANEL) GENERIC_PANEL_ALWAYS_TESTED_GENES else emptySet()
    }

    fun genesHavingResultsInPanel(): Set<String> {
        return genesWithVariants() + genesWithFusions() + genesWithExonDeletions()
    }
}