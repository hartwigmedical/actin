package com.hartwig.actin.clinical.datamodel

import java.time.LocalDate

data class SequencedVariant(val gene: String, val hgvsCodingImpact: String? = null, val hgvsProteinImpact: String? = null)

data class SequencedAmplification(val gene: String, val chromosome: String? = null)

data class SequencedExonSkip(val gene: String, val exonStart: Int, val exonEnd: Int)

data class SequencedFusion(val geneUp: String? = null, val geneDown: String? = null)

data class PriorSequencingTest(
    val test: String,
    val date: LocalDate? = null,
    val tumorMutationalBurden: Double? = null,
    val microSatelliteInstability: Boolean? = null,
    val testedGenes: Set<String>? = null,
    val variants: Set<SequencedVariant> = emptySet(),
    val amplifications: Set<SequencedAmplification> = emptySet(),
    val exonSkips: Set<SequencedExonSkip> = emptySet(),
    val fusions: Set<SequencedFusion> = emptySet()
)