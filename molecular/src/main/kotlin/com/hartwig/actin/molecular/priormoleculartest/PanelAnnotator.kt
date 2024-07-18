package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
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
import com.hartwig.actin.tools.pave.VariantTranscriptImpact
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.serve.datamodel.hotspot.KnownHotspot
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PanelAnnotator(
    private val experimentType: ExperimentType,
    private val evidenceDatabase: EvidenceDatabase,
    private val geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    private val transcriptAnnotator: VariantAnnotator,
    private val paver: Paver,
    private val paveLite: PaveLite
) :
    MolecularAnnotator<PanelExtraction, PanelRecord> {

    override fun annotate(input: PanelExtraction): PanelRecord {
        val inputMap = input.variants.withIndex().associate { it.index.toString() to it.value }
        val transvarAnnotationsMap = inputMap.mapValues { (_, value) -> externalAnnotation(value) }
            .mapNotNull { if (it.value != null) it.key to it.value!! else null }
            .toMap()

        val paveQueries = transvarAnnotationsMap.map { (id, annotation) ->
            PaveQuery(
                id = id,
                chromosome = annotation.chromosome(),
                position = annotation.position(),
                ref = annotation.ref(),
                alt = annotation.alt()
            )
        }

        val paveResponses = paver.run(paveQueries)
        val responseMap = paveResponses.associateBy { it.id }

        val annotatedVariants = transvarAnnotationsMap.map { (id, transvarAnnotation) ->
            if (id !in responseMap) {
                LOGGER.warn("Pave did not return a response for query $transvarAnnotation")
                null
            } else {
                val paveResponse = responseMap[id]!!
                val extraction = inputMap[id]!!

                val (evidence, geneAlteration) = serveEvidence(extraction, transvarAnnotation)

                val paveLiteAnnotations = paveLite.run(
                    paveResponse.impact.gene,
                    paveResponse.impact.transcript,
                    transvarAnnotation.position()
                )

                createVariantWithEvidence(
                    extraction,
                    evidence,
                    geneAlteration,
                    transvarAnnotation,
                    paveResponse,
                    paveLiteAnnotations
                )
            }
        }
            .filterNotNull()

        val variantsByGene = annotatedVariants.groupBy { it.gene }
        val variantsWithDriverLikelihoodModel = variantsByGene.map {
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

        return PanelRecord(
            panelExtraction = input,
            type = experimentType,
            date = input.date,
            drivers = Drivers(variants = variantsWithDriverLikelihoodModel.toSet()),
            characteristics = MolecularCharacteristics(),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display()
        )
    }

    private fun externalAnnotation(it: PanelVariantExtraction): com.hartwig.actin.tools.variant.Variant? {
        val externalVariantAnnotation = transcriptAnnotator.resolve(it.gene, null, it.hgvsCodingImpact)

        if (externalVariantAnnotation == null) {
            LOGGER.error("Unable to resolve variant '$it' in variant annotator. See prior warnings.")
            return null
        }

        return externalVariantAnnotation
    }

    private fun serveEvidence(
        it: PanelVariantExtraction,
        transcriptPositionAndVariationAnnotation: com.hartwig.actin.tools.variant.Variant
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
        transcriptAnnotation: com.hartwig.actin.tools.variant.Variant,
        paveResponse: PaveResponse,
        paveLiteAnnotation: VariantTranscriptImpact?
    ) = Variant(
        isReportable = true,
        event = "${it.gene} ${it.hgvsCodingImpact}",
        driverLikelihood = DriverLikelihood.LOW,
        evidence = evidence,
        gene = it.gene,
        geneRole = geneAlteration.geneRole,
        proteinEffect = geneAlteration.proteinEffect,
        isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance,
        isHotspot = geneAlteration is KnownHotspot,
        ref = transcriptAnnotation.ref(),
        alt = transcriptAnnotation.alt(),
        canonicalImpact = impact(paveResponse.impact, paveLiteAnnotation),
        // TODO:
        //  * should we filter out transcript impacts with coding effect of None? Synonymous?
        //  * should we filter out transcript impact if its the same transcript as canonicalImpact?
        //  * should we filter on gene, in case the Pave response contains multiple genes?
        otherImpacts = paveResponse.transcriptImpact.map { transcriptImpact(it, paveLiteAnnotation) }.toSet(),
        chromosome = transcriptAnnotation.chromosome(),
        position = transcriptAnnotation.position(),
        type = variantType(transcriptAnnotation)
    )

    private fun impact(impact: PaveImpact, paveLiteAnnotation: VariantTranscriptImpact?): TranscriptImpact {
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

    private fun transcriptImpact(impact: PaveTranscriptImpact, paveLiteAnnotation: VariantTranscriptImpact?): TranscriptImpact {
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

    private fun variantType(transcriptAnnotation: com.hartwig.actin.tools.variant.Variant): VariantType {
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

    private fun codingEffect(paveCodingEffect: PaveCodingEffect) =
        when (paveCodingEffect) {
            PaveCodingEffect.NONE -> CodingEffect.NONE
            PaveCodingEffect.MISSENSE -> CodingEffect.MISSENSE
            PaveCodingEffect.NONSENSE_OR_FRAMESHIFT -> CodingEffect.NONSENSE_OR_FRAMESHIFT
            PaveCodingEffect.SPLICE -> CodingEffect.SPLICE
            PaveCodingEffect.SYNONYMOUS -> CodingEffect.SYNONYMOUS
        }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PanelAnnotator::class.java)
    }
}