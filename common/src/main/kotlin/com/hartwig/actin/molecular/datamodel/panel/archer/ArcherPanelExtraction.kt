package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelAmplificationExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import java.time.LocalDate

val ARCHER_ALWAYS_TESTED_GENES = setOf("ALK", "ROS1", "RET", "MET", "NTRK1", "NTRK2", "NTRK3", "NRG1")

data class ArcherPanelExtraction(
    val fusions: List<ArcherFusionExtraction> = emptyList(),
    val skippedExons: List<ArcherSkippedExonsExtraction> = emptyList(),
    override val variants: List<PanelVariantExtraction> = emptyList(),
    override val amplifications: List<PanelAmplificationExtraction> = emptyList(),
    override val date: LocalDate? = null,
    override val msi: Boolean? = null,
    override val tmb: Double? = null,
    override val extractionClass: String = ArcherPanelExtraction::class.java.simpleName
) : PanelExtraction {

    override val panelType = "Archer"

    override fun testedGenes(): Set<String> {
        return genesHavingResultsInPanel() + ARCHER_ALWAYS_TESTED_GENES
    }

    fun genesWithVariants(): Set<String> {
        return variants.map { it.gene }.toSet()
    }

    fun genesWithFusions(): Set<String> {
        return fusions.flatMap { listOf(it.gene) }.toSet()
    }

    override fun events() = (variants + fusions + skippedExons).toSet()

    private fun genesHavingResultsInPanel(): Set<String> {
        return genesWithVariants() + genesWithFusions() + skippedExons.map { it.gene }.toSet()
    }
}