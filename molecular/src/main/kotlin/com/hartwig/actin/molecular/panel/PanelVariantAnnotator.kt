package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.orange.AminoAcid.forceSingleLetterAminoAcids
import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.PaveTranscriptImpact
import com.hartwig.actin.molecular.paver.PaveVariantEffect
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.molecular.util.FormatFunctions.formatVariantImpact
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.VariantAnnotator
import org.apache.logging.log4j.LogManager
import com.hartwig.actin.tools.variant.Variant as TransvarVariant

fun eventString(paveResponse: PaveResponse): String {
    return formatVariantImpact(
        paveResponse.impact.hgvsProteinImpact,
        paveResponse.impact.hgvsCodingImpact,
        paveResponse.impact.canonicalCodingEffect == PaveCodingEffect.SPLICE,
        paveResponse.impact.canonicalEffects.contains(PaveVariantEffect.UPSTREAM_GENE),
        paveResponse.impact.canonicalEffects.joinToString("&") { it.toString() }
    )
}

class PanelVariantAnnotator(
    private val variantResolver: VariantAnnotator,
    private val paver: Paver,
    private val paveLite: PaveLite,
) {

    private val logger = LogManager.getLogger(PanelVariantAnnotator::class.java)

    fun annotate(sequencedVariants: Set<SequencedVariant>): List<Variant> {
        val variantExtractions = indexVariantExtractionsToUniqueIds(sequencedVariants)
        val transvarVariants = resolveVariants(variantExtractions)
        val paveAnnotations = annotateWithPave(transvarVariants)

        return createVariants(transvarVariants, paveAnnotations, variantExtractions)
    }

    private fun indexVariantExtractionsToUniqueIds(variants: Collection<SequencedVariant>): Map<String, SequencedVariant> {
        return variants.withIndex().associate { it.index.toString() to it.value }
    }

    private fun resolveVariants(variantExtractions: Map<String, SequencedVariant>): Map<String, TransvarVariant> {
        return variantExtractions.mapValues { (_, value) -> transvarAnnotation(value) }
            .mapNotNull { if (it.value != null) it.key to it.value!! else null }
            .toMap()
    }

    private fun transvarAnnotation(sequencedVariant: SequencedVariant): TransvarVariant? {
        val externalVariantAnnotation =
            variantResolver.resolve(
                sequencedVariant.gene,
                sequencedVariant.transcript,
                sequencedVariant.hgvsCodingOrProteinImpact()
            )

        if (externalVariantAnnotation == null) {
            logger.error("Unable to resolve variant '$sequencedVariant' in variant annotator. See prior warnings.")
            return null
        }

        return externalVariantAnnotation
    }

    private fun annotateWithPave(transvarVariants: Map<String, TransvarVariant>): Map<String, PaveResponse> {
        if (transvarVariants.isEmpty()) {
            return emptyMap()
        }

        val paveQueries = transvarVariants.map { (id, annotation) ->
            PaveQuery(
                id = id,
                chromosome = annotation.chromosome(),
                position = annotation.position(),
                ref = annotation.ref(),
                alt = annotation.alt()
            )
        }

        val paveResponses = paver.run(paveQueries).associateBy { it.id }
        if (transvarVariants.keys != paveResponses.keys) {
            throw IllegalStateException("Pave did not return a response for all queries")
        }

        return paveResponses
    }

    private fun createVariants(
        transvarVariants: Map<String, TransvarVariant>,
        paveAnnotations: Map<String, PaveResponse>,
        variantExtractions: Map<String, SequencedVariant>
    ): List<Variant> {
        return transvarVariants.map { (id, transvarAnnotation) ->
            val sequencedVariant = variantExtractions[id]!!
            val paveResponse = paveAnnotations[id]!!

            createVariant(sequencedVariant, transvarAnnotation, paveResponse)
        }
    }

    private fun createVariant(
        variant: SequencedVariant,
        transvarAnnotation: TransvarVariant,
        paveResponse: PaveResponse
    ) = Variant(
        chromosome = transvarAnnotation.chromosome(),
        position = transvarAnnotation.position(),
        ref = transvarAnnotation.ref(),
        alt = transvarAnnotation.alt(),
        type = variantType(transvarAnnotation),
        variantAlleleFrequency = variant.variantAlleleFrequency,
        canonicalImpact = canonicalImpact(paveResponse.impact, transvarAnnotation),
        otherImpacts = otherImpacts(paveResponse, transvarAnnotation),
        isCancerAssociatedVariant = false,
        isReportable = true,
        event = "${variant.gene} ${eventString(paveResponse)}",
        driverLikelihood = null,
        evidence = ClinicalEvidence(emptySet(), emptySet()),
        gene = variant.gene,
        geneRole = GeneRole.UNKNOWN,
        proteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance = null
    )

    private fun canonicalImpact(paveImpact: PaveImpact, transvarVariant: TransvarVariant): TranscriptVariantImpact {
        val paveLiteAnnotation = paveLite.run(
            paveImpact.gene,
            paveImpact.canonicalTranscript,
            transvarVariant.position()
        ) ?: throw IllegalStateException("PaveLite did not return a response for $transvarVariant")
        
        return TranscriptVariantImpact(
            transcriptId = paveImpact.canonicalTranscript,
            hgvsCodingImpact = paveImpact.hgvsCodingImpact,
            hgvsProteinImpact = forceSingleLetterAminoAcids(paveImpact.hgvsProteinImpact),
            affectedCodon = paveLiteAnnotation.affectedCodon(),
            affectedExon = paveLiteAnnotation.affectedExon(),
            inSpliceRegion = paveImpact.spliceRegion,
            effects = paveImpact.canonicalEffects.map { variantEffect(it) }.toSet(),
            codingEffect = codingEffect(paveImpact.canonicalCodingEffect),
        )
    }

    fun otherImpacts(paveResponse: PaveResponse, transvarVariant: TransvarVariant): Set<TranscriptVariantImpact> {
        return paveResponse.transcriptImpacts
            .filter { it.gene == paveResponse.impact.gene && it.transcript != paveResponse.impact.canonicalTranscript }
            .map { transcriptImpact(it, transvarVariant) }
            .toSet()
    }

    private fun transcriptImpact(paveTranscriptImpact: PaveTranscriptImpact, transvarVariant: TransvarVariant): TranscriptVariantImpact {
        val paveLiteAnnotation = paveLite.run(
            paveTranscriptImpact.gene,
            paveTranscriptImpact.transcript,
            transvarVariant.position()
        ) ?: throw IllegalStateException("PaveLite did not return a response for $transvarVariant")

        return TranscriptVariantImpact(
            transcriptId = paveTranscriptImpact.transcript,
            hgvsCodingImpact = paveTranscriptImpact.hgvsCodingImpact,
            hgvsProteinImpact = forceSingleLetterAminoAcids(paveTranscriptImpact.hgvsProteinImpact),
            affectedCodon = paveLiteAnnotation.affectedCodon(),
            affectedExon = paveLiteAnnotation.affectedExon(),
            inSpliceRegion = paveTranscriptImpact.spliceRegion,
            effects = paveTranscriptImpact.effects.map { variantEffect(it) }.toSet(),
            codingEffect = codingEffect(
                paveTranscriptImpact.effects
                    .map(PaveCodingEffect::fromPaveVariantEffect)
                    .let(PaveCodingEffect::worstCodingEffect)
            )
        )
    }

    private fun variantType(transvarVariant: com.hartwig.actin.tools.variant.Variant): VariantType {
        val ref = transvarVariant.ref()
        val alt = transvarVariant.alt()
        return if (ref.length == alt.length) {
            if (ref.length == 1) {
                VariantType.SNV
            } else {
                VariantType.MNV
            }
        } else if (ref.length > alt.length) {
            VariantType.DELETE
        } else {
            VariantType.INSERT
        }
    }

    private fun codingEffect(paveCodingEffect: PaveCodingEffect): CodingEffect {
        return when (paveCodingEffect) {
            PaveCodingEffect.NONE -> CodingEffect.NONE
            PaveCodingEffect.MISSENSE -> CodingEffect.MISSENSE
            PaveCodingEffect.NONSENSE_OR_FRAMESHIFT -> CodingEffect.NONSENSE_OR_FRAMESHIFT
            PaveCodingEffect.SPLICE -> CodingEffect.SPLICE
            PaveCodingEffect.SYNONYMOUS -> CodingEffect.SYNONYMOUS
        }
    }

    private fun variantEffect(paveVariantEffect: PaveVariantEffect): VariantEffect {
        return when (paveVariantEffect) {
            PaveVariantEffect.STOP_GAINED -> VariantEffect.STOP_GAINED
            PaveVariantEffect.STOP_LOST -> VariantEffect.STOP_LOST
            PaveVariantEffect.START_LOST -> VariantEffect.START_LOST
            PaveVariantEffect.FRAMESHIFT -> VariantEffect.FRAMESHIFT
            PaveVariantEffect.SPLICE_ACCEPTOR -> VariantEffect.SPLICE_ACCEPTOR
            PaveVariantEffect.SPLICE_DONOR -> VariantEffect.SPLICE_DONOR
            PaveVariantEffect.INFRAME_INSERTION -> VariantEffect.INFRAME_INSERTION
            PaveVariantEffect.INFRAME_DELETION -> VariantEffect.INFRAME_DELETION
            PaveVariantEffect.MISSENSE -> VariantEffect.MISSENSE
            PaveVariantEffect.PHASED_MISSENSE -> VariantEffect.PHASED_MISSENSE
            PaveVariantEffect.PHASED_INFRAME_INSERTION -> VariantEffect.PHASED_INFRAME_INSERTION
            PaveVariantEffect.PHASED_INFRAME_DELETION -> VariantEffect.PHASED_INFRAME_DELETION
            PaveVariantEffect.SYNONYMOUS -> VariantEffect.SYNONYMOUS
            PaveVariantEffect.PHASED_SYNONYMOUS -> VariantEffect.PHASED_SYNONYMOUS
            PaveVariantEffect.INTRONIC -> VariantEffect.INTRONIC
            PaveVariantEffect.FIVE_PRIME_UTR -> VariantEffect.FIVE_PRIME_UTR
            PaveVariantEffect.THREE_PRIME_UTR -> VariantEffect.THREE_PRIME_UTR
            PaveVariantEffect.UPSTREAM_GENE -> VariantEffect.UPSTREAM_GENE
            PaveVariantEffect.NON_CODING_TRANSCRIPT -> VariantEffect.NON_CODING_TRANSCRIPT
            PaveVariantEffect.OTHER -> VariantEffect.OTHER
        }
    }
}
