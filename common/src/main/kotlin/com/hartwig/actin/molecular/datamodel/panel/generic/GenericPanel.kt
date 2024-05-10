package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.panel.Panel
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent
import java.time.LocalDate

val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF", "KRAS")

data class GenericPanel(
    val panelType: GenericPanelType,
    val variants: List<GenericVariant> = emptyList(),
    val fusions: List<GenericFusion> = emptyList(),
    override val date: LocalDate? = null,
) : Panel, MolecularTest {

    override val type = ExperimentType.GENERIC_PANEL

    override fun testedGenes(): Set<String> {
        return genesHavingResultsInPanel() + alwaysTestedGenes()
    }

    private fun genesWithVariants(): Set<String> {
        return variants.map { it.gene }.toSet()
    }

    private fun genesWithFusions(): Set<String> {
        return fusions.flatMap { listOf(it.geneStart, it.geneEnd) }.toSet()
    }

    private fun alwaysTestedGenes(): Set<String> {
        return when (panelType) {
            GenericPanelType.FREE_TEXT -> emptySet()
            else -> GENERIC_PANEL_ALWAYS_TESTED_GENES
        }
    }

    override fun variants(): List<PanelEvent> {
        return variants
    }

    override fun fusions(): List<PanelEvent> {
        return fusions
    }

    private fun genesHavingResultsInPanel(): Set<String> {
        return genesWithVariants() + genesWithFusions()
    }
}