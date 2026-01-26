package com.hartwig.actin.molecular.panel

import com.hartwig.actin.molecular.paver.PaveResponse
import org.apache.logging.log4j.LogManager

data class ExonCodon(val exon: Int?, val codon: Int?)

object PhaseSetExonCodonResolver {

    private val LOGGER = LogManager.getLogger(PhaseSetExonCodonResolver::class.java)

    fun resolve(phaseSet: Int, responses: List<PaveResponse>): ExonCodon? {
        if (responses.isEmpty()) {
            return null
        }

        val canonicalTranscripts = responses.map { it.impact.canonicalTranscript }.distinct()
        if (canonicalTranscripts.size > 1) {
            throw IllegalStateException("Phase set $phaseSet has mismatched canonical transcripts: $canonicalTranscripts")
        }

        val canonicalImpacts = responses.map { response ->
            response.transcriptImpacts.firstOrNull { it.transcript == response.impact.canonicalTranscript }
                ?: throw IllegalStateException("Missing canonical transcript impact for phase set $phaseSet")
        }

        val exons = canonicalImpacts.map { it?.exon }
        val codons = canonicalImpacts.map { it?.codon }
        val exonMatches = exons.distinct().size <= 1
        val codonMatches = codons.distinct().size <= 1

        if (exonMatches && codonMatches) {
            return ExonCodon(exons.firstOrNull(), codons.firstOrNull())
        }

        val selectedImpact = if (codons.any { it != null }) {
            canonicalImpacts.minWith(compareBy({ it.codon ?: Int.MAX_VALUE }, { it.exon ?: Int.MAX_VALUE }))
        } else {
            canonicalImpacts.minBy { it.exon ?: Int.MAX_VALUE }
        }
        val resolvedExon = selectedImpact.exon
        val resolvedCodon = selectedImpact.codon
        LOGGER.warn(
            "Phase set {} has mismatched exon/codon across records; exons={}, codons={}, using exon={} codon={}",
            phaseSet,
            exons,
            codons,
            resolvedExon,
            resolvedCodon
        )
        return ExonCodon(resolvedExon, resolvedCodon)
    }

    fun applyToResponse(response: PaveResponse, resolved: ExonCodon): PaveResponse {
        val updatedImpacts = response.transcriptImpacts.map { impact ->
            if (impact.transcript == response.impact.canonicalTranscript) {
                impact.copy(exon = resolved.exon, codon = resolved.codon)
            } else {
                impact
            }
        }
        return response.copy(transcriptImpacts = updatedImpacts)
    }
}
