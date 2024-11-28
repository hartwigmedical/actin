package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.TranscriptImpact
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
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
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as ServeProteinEffect
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration as ServeGeneAlteration

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

class PanelVariantAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    private val variantResolver: VariantAnnotator,
    private val paver: Paver,
    private val paveLite: PaveLite,
) {

    private val logger = LogManager.getLogger(PanelVariantAnnotator::class.java)

    fun annotate(variants: Set<SequencedVariant>): List<Variant> {
        val variantExtractions = indexVariantExtractionsToUniqueIds(variants)
        val transvarVariants = resolveVariants(variantExtractions)
        val paveAnnotations = annotateWithPave(transvarVariants)
        val variantsWithEvidence = annotateWithEvidence(transvarVariants, paveAnnotations, variantExtractions)

        return annotateWithDriverLikelihood(variantsWithEvidence)
    }

    private fun indexVariantExtractionsToUniqueIds(variants: Collection<SequencedVariant>): Map<String, SequencedVariant> {
        return variants.withIndex().associate { it.index.toString() to it.value }
    }

    private fun resolveVariants(variantExtractions: Map<String, SequencedVariant>): Map<String, com.hartwig.actin.tools.variant.Variant> {
        return variantExtractions.mapValues { (_, value) -> transvarAnnotation(value) }
            .mapNotNull { if (it.value != null) it.key to it.value!! else null }
            .toMap()
    }

    private fun transvarAnnotation(panelVariantExtraction: SequencedVariant): com.hartwig.actin.tools.variant.Variant? {
        val externalVariantAnnotation =
            variantResolver.resolve(
                panelVariantExtraction.gene,
                panelVariantExtraction.transcript,
                panelVariantExtraction.hgvsCodingOrProteinImpact()
            )

        if (externalVariantAnnotation == null) {
            logger.error("Unable to resolve variant '$panelVariantExtraction' in variant annotator. See prior warnings.")
            return null
        }

        return externalVariantAnnotation
    }

    private fun annotateWithPave(transvarVariants: Map<String, com.hartwig.actin.tools.variant.Variant>): Map<String, PaveResponse> {
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

    private fun annotateWithEvidence(
        transvarVariants: Map<String, com.hartwig.actin.tools.variant.Variant>,
        paveAnnotations: Map<String, PaveResponse>,
        variantExtractions: Map<String, SequencedVariant>
    ): List<Variant> {
        return transvarVariants.map { (id, transvarAnnotation) ->
            val paveResponse = paveAnnotations[id]!!
            val extraction = variantExtractions[id]!!

            val criteria = variantMatchCriteria(extraction, transvarAnnotation, paveResponse)
            val evidence = ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForVariant(criteria))
            val serveGeneAlteration = evidenceDatabase.geneAlterationForVariant(criteria)
            val geneAlteration = GeneAlterationFactory.convertAlteration(extraction.gene, serveGeneAlteration)

            createVariantWithEvidence(
                extraction,
                evidence,
                geneAlteration,
                serveGeneAlteration,
                transvarAnnotation,
                paveResponse
            )
        }
    }


    private fun variantMatchCriteria(
        panelVariantExtraction: SequencedVariant,
        transvarVariant: com.hartwig.actin.tools.variant.Variant,
        paveResponse: PaveResponse
    ) = VariantMatchCriteria(
        isReportable = true,
        gene = panelVariantExtraction.gene,
        chromosome = transvarVariant.chromosome(),
        ref = transvarVariant.ref(),
        alt = transvarVariant.alt(),
        position = transvarVariant.position(),
        type = variantType(transvarVariant),
        codingEffect = codingEffect(paveResponse.impact.canonicalCodingEffect)
    )

    private fun createVariantWithEvidence(
        variant: SequencedVariant,
        evidence: ClinicalEvidence,
        geneAlteration: GeneAlteration,
        serveGeneAlteration: ServeGeneAlteration?,
        transcriptAnnotation: com.hartwig.actin.tools.variant.Variant,
        paveResponse: PaveResponse
    ) = Variant(
        chromosome = transcriptAnnotation.chromosome(),
        position = transcriptAnnotation.position(),
        ref = transcriptAnnotation.ref(),
        alt = transcriptAnnotation.alt(),
        type = variantType(transcriptAnnotation),
        variantAlleleFrequency = variant.variantAlleleFrequency,
        canonicalImpact = impact(paveResponse.impact, transcriptAnnotation),
        otherImpacts = otherImpacts(paveResponse, transcriptAnnotation),
        isHotspot = isHotspot(serveGeneAlteration),
        isReportable = true,
        event = "${variant.gene} ${impact(paveResponse)}",

        driverLikelihood = DriverLikelihood.LOW,
        evidence = evidence,
        gene = variant.gene,
        geneRole = geneAlteration.geneRole,
        proteinEffect = geneAlteration.proteinEffect,
        isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance
    )

    private fun impact(paveImpact: PaveImpact, transvarVariant: com.hartwig.actin.tools.variant.Variant): TranscriptImpact {

        val paveLiteAnnotation = paveLite.run(
            paveImpact.gene,
            paveImpact.transcript,
            transvarVariant.position()
        ) ?: throw IllegalStateException("PaveLite did not return a response for $transvarVariant")


        return TranscriptImpact(
            transcriptId = paveImpact.transcript,
            hgvsCodingImpact = paveImpact.hgvsCodingImpact,
            hgvsProteinImpact = forceSingleLetterAminoAcids(paveImpact.hgvsProteinImpact),
            isSpliceRegion = paveImpact.spliceRegion,
            affectedExon = paveLiteAnnotation.affectedExon(),
            affectedCodon = paveLiteAnnotation.affectedCodon(),
            codingEffect = codingEffect(paveImpact.canonicalCodingEffect),
        )
    }

    fun otherImpacts(paveResponse: PaveResponse, transvarVariant: com.hartwig.actin.tools.variant.Variant): Set<TranscriptImpact> {
        return paveResponse.transcriptImpact
            .filter { it.gene == paveResponse.impact.gene && it.transcript != paveResponse.impact.transcript }
            .map { transcriptImpact(it, transvarVariant) }
            .toSet()
    }

    private fun transcriptImpact(
        paveTranscriptImpact: PaveTranscriptImpact,
        transvarVariant: com.hartwig.actin.tools.variant.Variant
    ): TranscriptImpact {
        val paveLiteAnnotation = paveLite.run(
            paveTranscriptImpact.gene,
            paveTranscriptImpact.transcript,
            transvarVariant.position()
        ) ?: throw IllegalStateException("PaveLite did not return a response for $transvarVariant")

        return TranscriptImpact(
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

    private fun annotateWithDriverLikelihood(annotatedVariants: List<Variant>): List<Variant> {
        val variantsByGene = annotatedVariants.groupBy { it.gene }
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
}

fun impact(paveResponse: PaveResponse): String {
    return formatVariantImpact(
        paveResponse.impact.hgvsProteinImpact,
        paveResponse.impact.hgvsCodingImpact,
        paveResponse.impact.canonicalCodingEffect == PaveCodingEffect.SPLICE,
        paveResponse.impact.canonicalEffect.contains("upstream_gene_variant"),
        paveResponse.impact.canonicalEffect
    )
}

