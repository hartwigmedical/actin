package com.hartwig.actin.molecular.datamodel.archer

import java.time.LocalDate

val ARCHER_ALWAYS_TESTED_GENES = setOf("ALK", "ROS1", "RET", "MET", "NTRK", "NRG1")

data class ArcherPanel(
    val date: LocalDate?,
    val variants: List<ArcherVariant>,
    val fusions: List<ArcherFusion>,
) {

    fun genesWithNoImpact(): Set<String> {
        val genes = variants.map { it.gene }.toSet() + fusions.flatMap { listOf(it.geneStart, it.geneEnd) }.toSet()
        return ARCHER_ALWAYS_TESTED_GENES - genes
    }
}