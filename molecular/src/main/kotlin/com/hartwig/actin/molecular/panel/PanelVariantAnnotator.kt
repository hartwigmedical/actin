package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.actin.molecular.orange.AminoAcid.forceSingleLetterAminoAcids
import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.PaveTranscriptImpact
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.molecular.util.ImpactDisplay.formatVariantImpact
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.range.KnownCodon
import org.apache.logging.log4j.LogManager
import com.hartwig.actin.tools.variant.Variant as TransvarVariant
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration as ServeGeneAlteration
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as ServeProteinEffect

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
) {

    private val logger = LogManager.getLogger(PanelVariantAnnotator::class.java)

    fun annotate(sequencedVariants: Set<SequencedVariant>): List<Variant> {
        val variantExtractions = indexVariantExtractionsToUniqueIds(sequencedVariants)
        val transvarVariants = resolveVariants(variantExtractions)
        val paveAnnotations = annotateWithPave(transvarVariants)

        val annotatedVariants =
            createVariants(transvarVariants, paveAnnotations, variantExtractions).map { annotateWithGeneAlterationData(it) }
        return annotateWithDriverLikelihood(annotatedVariants).map { annotateWithEvidence(it) }
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

    private fun canonicalImpact(paveImpact: PaveImpact, transvarVariant: TransvarVariant): TranscriptVariantImpact {
        val paveLiteAnnotation = paveLite.run(
            paveImpact.gene,
            paveImpact.transcript,
            transvarVariant.position()
        ) ?: throw IllegalStateException("PaveLite did not return a response for $transvarVariant")

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

    fun otherImpacts(paveResponse: PaveResponse, transvarVariant: TransvarVariant): Set<TranscriptVariantImpact> {
        return paveResponse.transcriptImpact
            .filter { it.gene == paveResponse.impact.gene && it.transcript != paveResponse.impact.transcript }
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
            isSpliceRegion = paveTranscriptImpact.spliceRegion,
            affectedExon = paveLiteAnnotation.affectedExon(),
            affectedCodon = paveLiteAnnotation.affectedCodon(),
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

    private fun annotateWithGeneAlterationData(variant: Variant): Variant {
        val criteria = variantMatchCriteria(variant)
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
        val criteria = variantMatchCriteria(variant)
        val evidence = evidenceDatabase.evidenceForVariant(criteria)
        return variant.copy(evidence = evidence)
    }

    private fun variantMatchCriteria(variant: Variant) = VariantMatchCriteria(
        gene = variant.gene,
        codingEffect = variant.canonicalImpact.codingEffect,
        type = variant.type,
        chromosome = variant.chromosome,
        position = variant.position,
        ref = variant.ref,
        alt = variant.alt,
        driverLikelihood = variant.driverLikelihood,
        isReportable = variant.isReportable
    )
}
