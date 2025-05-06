package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.actin.molecular.orange.AminoAcid.forceSingleLetterAminoAcids
import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.PaveTranscriptImpact
import com.hartwig.actin.molecular.paver.PaveVariantEffect
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.molecular.util.ImpactDisplay.formatVariantImpact
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.hmftools.common.genome.refgenome.RefGenomeInterface
import com.hartwig.hmftools.pavereverse.ReversePave
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.range.KnownCodon
import org.apache.logging.log4j.LogManager
import java.io.File
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration as ServeGeneAlteration
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as ServeProteinEffect
import com.hartwig.hmftools.pavereverse.BaseSequenceChange as BaseSequenceChange

private val SERVE_HOTSPOT_PROTEIN_EFFECTS = setOf(
    ServeProteinEffect.LOSS_OF_FUNCTION,
    ServeProteinEffect.LOSS_OF_FUNCTION_PREDICTED,
    ServeProteinEffect.GAIN_OF_FUNCTION,
    ServeProteinEffect.GAIN_OF_FUNCTION_PREDICTED
)

fun isHotspot(geneAlteration: ServeGeneAlteration?): Boolean {
    return (geneAlteration is KnownHotspot || geneAlteration is KnownCodon) &&
            geneAlteration.proteinEffect() in SERVE_HOTSPOT_PROTEIN_EFFECTS
}

fun eventString(paveResponse: PaveResponse): String {
    return formatVariantImpact(
        paveResponse.impact.hgvsProteinImpact,
        paveResponse.impact.hgvsCodingImpact,
        paveResponse.impact.canonicalCodingEffect == PaveCodingEffect.SPLICE,
        paveResponse.impact.canonicalEffect.contains("upstream_gene_variant"),
        paveResponse.impact.canonicalEffect
    )
}

class PanelVariantAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    private val variantResolver: VariantAnnotator,
    private val paver: Paver,
    private val paveLite: PaveLite,
    private val refGenome: RefGenomeInterface
) {

    private val logger = LogManager.getLogger(PanelVariantAnnotator::class.java)

    fun annotate(sequencedVariants: Set<SequencedVariant>): List<Variant> {
        val variantExtractions = indexVariantExtractionsToUniqueIds(sequencedVariants)
        val baseSequenceChanges = resolveVariants(variantExtractions)
        val paveAnnotations = annotateWithPave(baseSequenceChanges)

        val annotatedVariants =
            createVariants(baseSequenceChanges, paveAnnotations, variantExtractions).map { annotateWithGeneAlteration(it) }
        return annotateWithDriverLikelihood(annotatedVariants).map { annotateWithEvidence(it) }
    }

    private fun indexVariantExtractionsToUniqueIds(variants: Collection<SequencedVariant>): Map<String, SequencedVariant> {
        return variants.withIndex().associate { it.index.toString() to it.value }
    }

    private fun resolveVariants(variantExtractions: Map<String, SequencedVariant>): Map<String, BaseSequenceChange> {
        return variantExtractions.mapValues { (_, value) -> reversePaveAnnotation(value) }
            .mapNotNull { if (it.value != null) it.key to it.value!! else null }
            .toMap()
    }

    private fun reversePaveAnnotation(sequencedVariant: SequencedVariant): BaseSequenceChange? {
        val externalVariantAnnotation = ReversePave(
            File(paver.ensemblDataDir),
            com.hartwig.hmftools.common.genome.refgenome.RefGenomeVersion.valueOf(paver.refGenomeVersion.toString()),
            refGenome
        ).calculateDnaVariant(
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

    private fun annotateWithPave(baseSequenceChanges: Map<String, BaseSequenceChange>): Map<String, PaveResponse> {
        if (baseSequenceChanges.isEmpty()) {
            return emptyMap()
        }

        val paveQueries = baseSequenceChanges.map { (id, annotation) ->
            PaveQuery(
                id = id,
                chromosome = annotation.Chromosome,
                position = annotation.Position,
                ref = annotation.Ref,
                alt = annotation.Alt
            )
        }

        val paveResponses = paver.run(paveQueries).associateBy { it.id }
        if (baseSequenceChanges.keys != paveResponses.keys) {
            throw IllegalStateException("Pave did not return a response for all queries")
        }

        return paveResponses
    }

    private fun createVariants(
        baseSequenceChanges: Map<String, BaseSequenceChange>,
        paveAnnotations: Map<String, PaveResponse>,
        variantExtractions: Map<String, SequencedVariant>
    ): List<Variant> {
        return baseSequenceChanges.map { (id, reversePaveAnnotation) ->
            val sequencedVariant = variantExtractions[id]!!
            val paveResponse = paveAnnotations[id]!!

            createVariant(sequencedVariant, reversePaveAnnotation, paveResponse)
        }
    }

    private fun createVariant(
        variant: SequencedVariant,
        reversePaveAnnotation: BaseSequenceChange,
        paveResponse: PaveResponse
    ) = Variant(
        chromosome = reversePaveAnnotation.Chromosome,
        position = reversePaveAnnotation.Position,
        ref = reversePaveAnnotation.Ref,
        alt = reversePaveAnnotation.Alt,
        type = variantType(reversePaveAnnotation),
        variantAlleleFrequency = variant.variantAlleleFrequency,
        canonicalImpact = canonicalImpact(paveResponse.impact, reversePaveAnnotation),
        otherImpacts = otherImpacts(paveResponse, reversePaveAnnotation),
        isHotspot = false,

        isReportable = true,
        event = "${variant.gene} ${eventString(paveResponse)}",
        driverLikelihood = null,
        evidence = ClinicalEvidence(emptySet(), emptySet()),
        gene = variant.gene,
        geneRole = GeneRole.UNKNOWN,
        proteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance = null
    )

    private fun canonicalImpact(paveImpact: PaveImpact, baseSequenceChange: BaseSequenceChange): TranscriptVariantImpact {
        val paveLiteAnnotation = paveLite.run(
            paveImpact.gene,
            paveImpact.transcript,
            baseSequenceChange.Position
        ) ?: throw IllegalStateException("PaveLite did not return a response for $baseSequenceChange")

        return TranscriptVariantImpact(
            transcriptId = paveImpact.transcript,
            hgvsCodingImpact = paveImpact.hgvsCodingImpact,
            hgvsProteinImpact = forceSingleLetterAminoAcids(paveImpact.hgvsProteinImpact),
            isSpliceRegion = paveImpact.spliceRegion,
            affectedExon = paveLiteAnnotation.affectedExon(),
            affectedCodon = paveLiteAnnotation.affectedCodon(),
            codingEffect = codingEffect(paveImpact.canonicalCodingEffect),
        )
    }

    fun otherImpacts(paveResponse: PaveResponse, baseSequenceChange: BaseSequenceChange): Set<TranscriptVariantImpact> {
        return paveResponse.transcriptImpact
            .filter { it.gene == paveResponse.impact.gene && it.transcript != paveResponse.impact.transcript }
            .map { transcriptImpact(it, baseSequenceChange) }
            .toSet()
    }

    private fun transcriptImpact(paveTranscriptImpact: PaveTranscriptImpact, baseSequenceChange: BaseSequenceChange): TranscriptVariantImpact {
        val paveLiteAnnotation = paveLite.run(
            paveTranscriptImpact.gene,
            paveTranscriptImpact.transcript,
            baseSequenceChange.Position
        ) ?: throw IllegalStateException("PaveLite did not return a response for $baseSequenceChange")

        return TranscriptVariantImpact(
            transcriptId = paveTranscriptImpact.transcript,
            hgvsCodingImpact = paveTranscriptImpact.hgvsCodingImpact,
            hgvsProteinImpact = forceSingleLetterAminoAcids(paveTranscriptImpact.hgvsProteinImpact),
            isSpliceRegion = paveTranscriptImpact.spliceRegion,
            affectedExon = paveLiteAnnotation.affectedExon(),
            affectedCodon = paveLiteAnnotation.affectedCodon(),
            effects = paveTranscriptImpact.effects.map { variantEffect(it) }.toSet(),
            codingEffect = codingEffect(
                paveTranscriptImpact.effects
                    .map(PaveCodingEffect::fromPaveVariantEffect)
                    .let(PaveCodingEffect::worstCodingEffect)
            )
        )
    }

    private fun variantType(baseSequenceChange: BaseSequenceChange): VariantType {
        val ref = baseSequenceChange.Ref
        val alt = baseSequenceChange.Alt
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

    private fun annotateWithGeneAlteration(variant: Variant): Variant {
        val criteria = MatchingCriteriaFunctions.createVariantCriteria(variant)
        val serveGeneAlteration = evidenceDatabase.geneAlterationForVariant(criteria)
        val geneAlteration = GeneAlterationFactory.convertAlteration(variant.gene, serveGeneAlteration)

        return variant.copy(
            isHotspot = isHotspot(serveGeneAlteration),
            geneRole = geneAlteration.geneRole,
            proteinEffect = geneAlteration.proteinEffect,
            isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance
        )
    }

    private fun annotateWithDriverLikelihood(variants: List<Variant>): List<Variant> {
        val variantsByGene = variants.groupBy { it.gene }
        return variantsByGene.map {
            val geneRole = it.value.map { variant -> variant.geneRole }.first()
            val likelihood = geneDriverLikelihoodModel.evaluate(it.key, geneRole, it.value)
            likelihood to it.value
        }.flatMap {
            it.second.map { variant ->
                variant.copy(
                    driverLikelihood = DriverLikelihood.from(it.first)
                )
            }
        }
    }

    private fun annotateWithEvidence(variant: Variant): Variant {
        val criteria = MatchingCriteriaFunctions.createVariantCriteria(variant)
        val evidence = evidenceDatabase.evidenceForVariant(criteria)
        return variant.copy(evidence = evidence)
    }
}
