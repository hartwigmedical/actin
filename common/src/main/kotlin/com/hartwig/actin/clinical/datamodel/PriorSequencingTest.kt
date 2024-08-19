package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.Displayable
import java.time.LocalDate

data class SequencedVariant(
    val gene: String,
    val hgvsCodingImpact: String? = null,
    val hgvsProteinImpact: String? = null,
    val transcript: String? = null,
    val exon: Int? = null,
    val codon: Int? = null
) {
    fun hgvsCodingOrProteinImpact(): String {
        return checkNotation(hgvsCodingImpact, "c") ?: checkNotation(hgvsProteinImpact, "p") ?: throw IllegalStateException()
    }

    private fun checkNotation(impact: String?, notationPrefix: String) =
        impact?.let { if (!impact.startsWith(notationPrefix)) "$notationPrefix.$it" else it }
}

data class SequencedAmplification(val gene: String)

data class SequencedSkippedExons(val gene: String, val exonStart: Int, val exonEnd: Int, val transcript: String? = null) : Displayable {
    override fun display(): String {
        return "$gene skipped exons $exonStart-$exonEnd"
    }
}

data class SequencedFusion(val geneUp: String? = null, val geneDown: String? = null) : Displayable {
    override fun display(): String {
        return when {
            geneUp != null && geneDown == null -> "$geneUp fusion"
            geneUp == null && geneDown != null -> "$geneDown fusion"
            geneUp != null && geneDown != null -> "$geneUp-$geneDown fusion"
            else -> throw IllegalStateException("Both genes in fusion are null")
        }
    }
}

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