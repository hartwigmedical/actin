package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.molecular.datamodel.panel.Panel

val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF", "KRAS")

data class GenericPanel(
    val panelType: GenericPanelType,
    val fusions: List<GenericFusion> = emptyList(),
    val variants: List<GenericVariant> = emptyList()
) : Panel {

    override fun testedGenes(): Set<String> {
        return genesWithVariants() + genesWithFusions() + alwaysTestedGenes()
    }

    fun genesWithVariants(): Set<String> {
        return variants.map { it.gene }.toSet()
    }

    fun genesWithFusions(): Set<String> {
        return fusions.flatMap { listOf(it.geneStart, it.geneEnd) }.toSet()
    }

    fun alwaysTestedGenes(): Set<String> {
        return if (panelType == GenericPanelType.FREE_TEXT) emptySet() else GENERIC_PANEL_ALWAYS_TESTED_GENES
    }
}