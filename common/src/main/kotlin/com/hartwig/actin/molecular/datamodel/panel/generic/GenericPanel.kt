package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.panel.Panel
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent

val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF", "KRAS")

data class GenericPanel(
    val panelName: GenericPanelType,
    val fusions: List<GenericFusion> = emptyList(),
    val variants: List<GenericVariant> = emptyList()
) : Panel {

    override fun testedGenes(): Set<String> {
        return genesHavingResultsInPanel() + alwaysTestedGenes()
    }

    fun genesWithVariants(): Set<String> {
        return variants.map { it.gene }.toSet()
    }

    fun genesWithFusions(): Set<String> {
        return fusions.flatMap { listOf(it.geneStart, it.geneEnd) }.toSet()
    }

    override fun alwaysTestedGenes(): Set<String> {
        return when (panelName) {
            GenericPanelType.FREE_TEXT -> emptySet()
            else -> GENERIC_PANEL_ALWAYS_TESTED_GENES
        }
    }

    override fun events(): List<PanelEvent> {
        return fusions // TODO add variants implemented in ACTIN-890
    }

    override fun eventsForGene(gene: String): List<PanelEvent> {
        // TODO add variants implemented in ACTIN-890
        return fusions.filter { it.geneStart == gene || it.geneEnd == gene }
    }

    private fun genesHavingResultsInPanel(): Set<String> {
        return genesWithVariants() + genesWithFusions()
    }
}