package com.hartwig.actin.molecular.datamodel.panel.archer

import com.hartwig.actin.molecular.datamodel.panel.Panel

private val ARCHER_ALWAYS_TESTED_GENES = setOf("ALK", "ROS1", "RET", "MET", "NTRK", "NRG1")

data class ArcherPanel(
    val variants: List<ArcherVariant>,
    val fusions: List<ArcherFusion>,
) : Panel {

    override fun testedGenes(): Set<String> {
        return genesWithVariants() + genesWithFusions() + ARCHER_ALWAYS_TESTED_GENES
    }

    fun genesWithVariants(): Set<String> {
        return variants.map { it.gene }.toSet() + fusions.flatMap { listOf(it.geneStart, it.geneEnd) }.toSet()
    }

    fun genesWithFusions(): Set<String> {
        return fusions.flatMap { listOf(it.geneStart, it.geneEnd) }.toSet()
    }
}