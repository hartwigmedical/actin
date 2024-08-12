package com.hartwig.actin.clinical.datamodel

import java.time.LocalDate

data class SequencedVariant(
    val gene: String,
    val hgvsCodingImpact: String? = null,
    val hgvsProteinImpact: String? = null,
    val transcript: String? = null,
    val exon: Int? = null,
    val codon: Int? = null,
    val variantAlleleFrequency: Double? = null
)

data class SequencedAmplification(val gene: String)

data class SequencedSkippedExons(val gene: String, val exonStart: Int, val exonEnd: Int)

data class SequencedFusion(val geneUp: String? = null, val geneDown: String? = null)

data class SequencedDeletedGene(val gene: String)

data class PriorSequencingTest(
    val test: String,
    val date: LocalDate? = null,
    val tumorMutationalBurden: Double? = null,
    val isMicrosatelliteUnstable: Boolean? = null,
    val testedGenes: Set<String>? = null,
    val variants: Set<SequencedVariant> = emptySet(),
    val amplifications: Set<SequencedAmplification> = emptySet(),
    val skippedExons: Set<SequencedSkippedExons> = emptySet(),
    val fusions: Set<SequencedFusion> = emptySet(),
    val deletedGenes: Set<SequencedDeletedGene> = emptySet()
)