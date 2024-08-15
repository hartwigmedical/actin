package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.PanelAmplificationExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelSkippedExonsExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import java.time.LocalDate

val ARCHER_ALWAYS_TESTED_GENES = setOf("ALK", "ROS1", "RET", "MET", "NTRK1", "NTRK2", "NTRK3", "NRG1")

data class ArcherPanelExtraction(
    override val fusions: List<PanelFusionExtraction> = emptyList(),
    override val skippedExons: List<PanelSkippedExonsExtraction> = emptyList(),
    override val variants: List<PanelVariantExtraction> = emptyList(),
    override val amplifications: List<PanelAmplificationExtraction> = emptyList(),
    override val date: LocalDate? = null,
    override val isMicrosatelliteUnstable: Boolean? = null,
    override val tumorMutationalBurden: Double? = null,
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
        return fusions.flatMap { listOfNotNull(it.geneUp, it.geneDown) }.toSet()
    }

    override fun events() = (variants + fusions + skippedExons).toSet()

    private fun genesHavingResultsInPanel(): Set<String> {
        return genesWithVariants() + genesWithFusions() + skippedExons.map { it.gene }.toSet() + amplifications.map { it.gene }.toSet()
    }
}