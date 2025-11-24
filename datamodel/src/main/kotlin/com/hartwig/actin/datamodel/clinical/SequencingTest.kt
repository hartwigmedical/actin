package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import java.time.LocalDate

data class SequencedVariant(
    val gene: String,
    val hgvsCodingImpact: String? = null,
    val hgvsProteinImpact: String? = null,
    val transcript: String? = null,
    val variantAlleleFrequency: Double? = null,
    val isBiallelic: Boolean? = null,
    val exonSkippingIsConfirmed: Boolean? = null,
    val exon: Int? = null,
    val codon: Int? = null
) {
    fun hgvsCodingOrProteinImpact(): String {
        return checkNotation(hgvsCodingImpact, "c") ?: checkNotation(hgvsProteinImpact, "p") ?: throw IllegalStateException()
    }

    private fun checkNotation(impact: String?, notationPrefix: String) =
        impact?.let { if (!impact.startsWith(notationPrefix)) "$notationPrefix.$it" else it }
}

data class SequencedAmplification(val gene: String, val transcript: String? = null, val copies: Int? = null, val isPartial: Boolean? = null)

data class SequencedDeletion(val gene: String, val transcript: String? = null, val isPartial: Boolean? = null)

data class SequencedFusion(
    val geneUp: String? = null,
    val geneDown: String? = null,
    val transcriptUp: String? = null,
    val transcriptDown: String? = null,
    val exonUp: Int? = null,
    val exonDown: Int? = null
)

data class SequencedSkippedExons(val gene: String, val exonStart: Int, val exonEnd: Int, val transcript: String? = null) : Displayable {
    override fun display(): String {
        return if (exonEnd != exonStart) "$gene exons $exonStart-$exonEnd skipping" else "$gene exon $exonStart skipping"
    }
}

data class SequencedVirus(val type: VirusType, val isLowRisk: Boolean = false)

data class SequencedNegativeResult(val gene: String, val molecularTestTarget: MolecularTestTarget)

data class SequencingTest(
    val test: String,
    val date: LocalDate? = null,
    val variants: Set<SequencedVariant> = emptySet(),
    val amplifications: Set<SequencedAmplification> = emptySet(),
    val deletions: Set<SequencedDeletion> = emptySet(),
    val fusions: Set<SequencedFusion> = emptySet(),
    val skippedExons: Set<SequencedSkippedExons> = emptySet(),
    val viruses: Set<SequencedVirus> = emptySet(),
    val negativeResults: Set<SequencedNegativeResult> = emptySet(),
    val tumorMutationalBurden: Double? = null,
    val isMicrosatelliteUnstable: Boolean? = null,
    val isHomologousRecombinationDeficient: Boolean? = null,
    val knownSpecifications: Boolean = false,
    val reportHash: String? = null
)
