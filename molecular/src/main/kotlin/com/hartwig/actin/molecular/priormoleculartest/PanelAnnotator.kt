package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionableEvidenceFactory
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.orange.interpretation.GeneAlterationFactory
import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.PaveTranscriptImpact
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.serve.datamodel.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.range.KnownCodon
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.hartwig.actin.tools.pave.VariantTranscriptImpact as PaveLiteAnnotation
import com.hartwig.actin.tools.variant.Variant as TransvarVariant
import com.hartwig.actin.tools.variant.VariantAnnotator as VariantResolver

class PanelAnnotator(
    private val experimentType: ExperimentType,
    private val evidenceDatabase: EvidenceDatabase,
    private val geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    private val variantResolver: VariantResolver,
    private val paver: Paver,
    private val paveLite: PaveLite
) :
    MolecularAnnotator<PanelExtraction, PanelRecord> {

    override fun annotate(input: PanelExtraction): PanelRecord {
        val variantExtractions = indexVariantExtractionsToUniqueIds(input.variants)
        val transvarVariants = resolveVariants(variantExtractions)
        val paveAnnotations = annotateWithPave(transvarVariants)
        val variantsWithEvidence = annotateWithEvidence(transvarVariants, paveAnnotations, variantExtractions)
        val variantsWithDriverLikelihoodModel = annotateWithDriverLikelihood(variantsWithEvidence)

        return PanelRecord(
            panelExtraction = input,
            type = experimentType,
            date = input.date,
            drivers = Drivers(variants = variantsWithDriverLikelihoodModel.toSet()),
            characteristics = MolecularCharacteristics(),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display()
        )
    }

    private fun indexVariantExtractionsToUniqueIds(variants: List<PanelVariantExtraction>): Map<String, PanelVariantExtraction> {
        return variants.withIndex().associate { it.index.toString() to it.value }
    }

    private fun resolveVariants(indexedToVariantExtractions: Map<String, PanelVariantExtraction>): Map<String, TransvarVariant> {
        return indexedToVariantExtractions.mapValues { (_, value) -> externalAnnotation(value) }
            .mapNotNull { if (it.value != null) it.key to it.value!! else null }
            .toMap()
    }

    private fun annotateWithPave(transvarAnnotationsMap: Map<String, TransvarVariant>): Map<String, PaveResponse> {
        val paveQueries = transvarAnnotationsMap.map { (id, annotation) ->
            PaveQuery(
                id = id,
                chromosome = annotation.chromosome(),
                position = annotation.position(),
                ref = annotation.ref(),
                alt = annotation.alt()
            )
        }

        val paveResponses = paver.run(paveQueries).associateBy { it.id }
        if (transvarAnnotationsMap.keys != paveResponses.keys) {
            throw IllegalStateException("Pave did not return a response for all queries")
        }

        return paveResponses
    }

    private fun annotateWithPaveLite(indexToTransvarVariant: Map<String, TransvarVariant>,
                                     indexToPaveResponse: Map<String, PaveResponse>): Map<String, PaveLiteAnnotation> {
        return indexToTransvarVariant.mapValues { (id, transvarAnnotation) ->
            val paveResponse = indexToPaveResponse[id]!!
            paveLite.run(
                paveResponse.impact.gene,
                paveResponse.impact.transcript,
                transvarAnnotation.position()
            ) ?: throw IllegalStateException("PaveLite did not return a response for query $transvarAnnotation")
        }
    }

    private fun annotateWithEvidence(indexToTransvarVariant: Map<String, TransvarVariant>,
                                     indexToPaveResponse: Map<String, PaveResponse>,
                                     indexedToVariantExtractions: Map<String, PanelVariantExtraction>): List<Variant> {
        return indexToTransvarVariant.map { (id, transvarAnnotation) ->
            val paveResponse = indexToPaveResponse[id]!!
            val extraction = indexedToVariantExtractions[id]!!
            val (evidence, geneAlteration) = serveEvidence(extraction, transvarAnnotation)
            createVariantWithEvidence(
                extraction,
                evidence,
                geneAlteration,
                transvarAnnotation,
                paveResponse
            )
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

    private fun externalAnnotation(it: PanelVariantExtraction): TransvarVariant? {
        val externalVariantAnnotation = variantResolver.resolve(it.gene, null, it.hgvsCodingImpact)

        if (externalVariantAnnotation == null) {
            LOGGER.error("Unable to resolve variant '$it' in variant annotator. See prior warnings.")
            return null
        }

        return externalVariantAnnotation
    }

    private fun serveEvidence(
        it: PanelVariantExtraction,
        transcriptPositionAndVariationAnnotation: TransvarVariant
    ): Pair<ActionableEvidence, GeneAlteration> {
        val criteria = VariantMatchCriteria(
            isReportable = true,
            gene = it.gene,
            chromosome = transcriptPositionAndVariationAnnotation.chromosome(),
            ref = transcriptPositionAndVariationAnnotation.ref(),
            alt = transcriptPositionAndVariationAnnotation.alt(),
            position = transcriptPositionAndVariationAnnotation.position()
        )
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVariant(criteria))
        val geneAlteration = GeneAlterationFactory.convertAlteration(
            it.gene, evidenceDatabase.geneAlterationForVariant(criteria)
        )
        return Pair(evidence, geneAlteration)
    }

    private fun createVariantWithEvidence(
        it: PanelVariantExtraction,
        evidence: ActionableEvidence,
        geneAlteration: GeneAlteration,
        transcriptAnnotation: TransvarVariant,
        paveResponse: PaveResponse
    ) = Variant(
        isReportable = true,
        event = "${it.gene} ${it.hgvsCodingImpact}",
        driverLikelihood = DriverLikelihood.LOW,
        evidence = evidence,
        gene = it.gene,
        geneRole = geneAlteration.geneRole,
        proteinEffect = when (geneAlteration.proteinEffect) {
            ProteinEffect.LOSS_OF_FUNCTION,
            ProteinEffect.LOSS_OF_FUNCTION_PREDICTED,
            ProteinEffect.GAIN_OF_FUNCTION,
            ProteinEffect.GAIN_OF_FUNCTION_PREDICTED -> geneAlteration.proteinEffect

            else -> ProteinEffect.NO_EFFECT
        },
        isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance,
        isHotspot = geneAlteration is KnownHotspot || geneAlteration is KnownCodon,
        ref = transcriptAnnotation.ref(),
        alt = transcriptAnnotation.alt(),


        canonicalImpact = impact(paveResponse.impact, transcriptAnnotation),
        otherImpacts = otherImpacts(paveResponse, transcriptAnnotation),
        chromosome = transcriptAnnotation.chromosome(),
        position = transcriptAnnotation.position(),
        type = variantType(transcriptAnnotation)
    )

    private fun impact(impact: PaveImpact, transvarVariant: TransvarVariant): TranscriptImpact {

        val paveLiteAnnotation = paveLite.run(
            impact.gene,
            impact.transcript,
            transvarVariant.position()
        )

        return TranscriptImpact(
            transcriptId = impact.transcript,
            hgvsCodingImpact = impact.hgvsCodingImpact,
            hgvsProteinImpact = impact.hgvsProteinImpact,
            isSpliceRegion = impact.spliceRegion,
            affectedExon = paveLiteAnnotation?.affectedExon(),
            affectedCodon = paveLiteAnnotation?.affectedCodon(),
            codingEffect = codingEffect(impact.canonicalCodingEffect),
        )
    }

    private fun otherImpacts(paveResponse: PaveResponse, transvarVariant: TransvarVariant): Set<TranscriptImpact> {
        return paveResponse.transcriptImpact
            .filter { it.gene == paveResponse.impact.gene && it.transcript != paveResponse.impact.transcript }
            .map { transcriptImpact(it, transvarVariant) }
            .toSet()
    }

    private fun transcriptImpact(impact: PaveTranscriptImpact, transvarVariant: TransvarVariant): TranscriptImpact {
        val paveLiteAnnotation = paveLite.run(
            impact.gene,
            impact.transcript,
            transvarVariant.position()
        )

        return TranscriptImpact(
            transcriptId = impact.transcript,
            hgvsCodingImpact = impact.hgvsCodingImpact,
            hgvsProteinImpact = impact.hgvsProteinImpact,
            isSpliceRegion = impact.spliceRegion,
            affectedExon = paveLiteAnnotation?.affectedExon(),
            affectedCodon = paveLiteAnnotation?.affectedCodon(),
            codingEffect = codingEffect(
                impact.effects
                    .map(PaveCodingEffect::fromPaveVariantEffect)
                    .let(PaveCodingEffect::worstCodingEffect)
            )
        )
    }

    private fun variantType(transcriptAnnotation: TransvarVariant): VariantType {
        val ref = transcriptAnnotation.ref()
        val alt = transcriptAnnotation.alt()
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

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PanelAnnotator::class.java)
    }
}