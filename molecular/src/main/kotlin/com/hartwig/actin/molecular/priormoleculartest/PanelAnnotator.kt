package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.panel.PanelAmplificationExtraction
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
import com.hartwig.actin.tools.variant.Variant as TransvarVariant
import com.hartwig.actin.tools.variant.VariantAnnotator as VariantResolver
import com.hartwig.serve.datamodel.common.GeneAlteration as ServeGeneAlteration

private const val TMB_HIGH_CUTOFF = 10.0

class PanelAnnotator(
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

        val annotatedAmplifications = input.amplifications.map(::inferredCopyNumber).map(::annotatedInferredCopyNumber)

        return PanelRecord(
            panelExtraction = input,
            experimentType = ExperimentType.PANEL,
            testTypeDisplay = input.panelType,
            date = input.date,
            drivers = Drivers(variants = variantsWithDriverLikelihoodModel.toSet(), copyNumbers = annotatedAmplifications.toSet()),
            characteristics = MolecularCharacteristics(
                isMicrosatelliteUnstable = input.isMicrosatelliteUnstable,
                tumorMutationalBurden = input.tumorMutationalBurden,
                hasHighTumorMutationalBurden = input.tumorMutationalBurden?.let { it > TMB_HIGH_CUTOFF }),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display()
        )
    }

    private fun variantMatchCriteria(
        panelVariantExtraction: PanelVariantExtraction,
        transvarVariant: TransvarVariant,
        paveResonse: PaveResponse
    ) = VariantMatchCriteria(
        isReportable = true,
        gene = panelVariantExtraction.gene,
        chromosome = transvarVariant.chromosome(),
        ref = transvarVariant.ref(),
        alt = transvarVariant.alt(),
        position = transvarVariant.position(),
        type = variantType(transvarVariant),
        codingEffect = codingEffect(paveResonse.impact.canonicalCodingEffect)
    )

    private fun annotatedInferredCopyNumber(copyNumber: CopyNumber): CopyNumber {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(copyNumber))
        val geneAlteration =
            GeneAlterationFactory.convertAlteration(copyNumber.gene, evidenceDatabase.geneAlterationForCopyNumber(copyNumber))
        return copyNumber.copy(
            evidence = evidence,
            geneRole = geneAlteration.geneRole,
            proteinEffect = geneAlteration.proteinEffect,
            isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance
        )
    }

    private fun inferredCopyNumber(panelAmplificationExtraction: PanelAmplificationExtraction) = CopyNumber(
        gene = panelAmplificationExtraction.gene,
        geneRole = GeneRole.UNKNOWN,
        proteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance = null,
        isReportable = true,
        event = panelAmplificationExtraction.display(),
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = ActionableEvidenceFactory.createNoEvidence(),
        type = CopyNumberType.FULL_GAIN,
        minCopies = 6,
        maxCopies = 6
    )

    private fun indexVariantExtractionsToUniqueIds(variants: List<PanelVariantExtraction>): Map<String, PanelVariantExtraction> {
        return variants.withIndex().associate { it.index.toString() to it.value }
    }

    private fun resolveVariants(variantExtractions: Map<String, PanelVariantExtraction>): Map<String, TransvarVariant> {
        return variantExtractions.mapValues { (_, value) -> transvarAnnotation(value) }
            .mapNotNull { if (it.value != null) it.key to it.value!! else null }
            .toMap()
    }

    private fun annotateWithPave(transvarVariants: Map<String, TransvarVariant>): Map<String, PaveResponse> {
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

    private fun annotateWithEvidence(transvarVariants: Map<String, TransvarVariant>,
                                     paveAnnotations: Map<String, PaveResponse>,
                                     variantExtractions: Map<String, PanelVariantExtraction>): List<Variant> {
        return transvarVariants.map { (id, transvarAnnotation) ->
            val paveResponse = paveAnnotations[id]!!
            val extraction = variantExtractions[id]!!

            val criteria = variantMatchCriteria(extraction, transvarAnnotation, paveResponse)
            val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVariant(criteria))
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

    private fun transvarAnnotation(panelVariantExtraction: PanelVariantExtraction): TransvarVariant? {
        val externalVariantAnnotation =
            variantResolver.resolve(panelVariantExtraction.gene, null, panelVariantExtraction.hgvsCodingOrProteinImpact)

        if (externalVariantAnnotation == null) {
            LOGGER.error("Unable to resolve variant '$panelVariantExtraction' in variant annotator. See prior warnings.")
            return null
        }

        return externalVariantAnnotation
    }

    private fun createVariantWithEvidence(
        variant: PanelVariantExtraction,
        evidence: ActionableEvidence,
        geneAlteration: GeneAlteration,
        serveGeneAlteration: ServeGeneAlteration?,
        transcriptAnnotation: TransvarVariant,
        paveResponse: PaveResponse
    ) = Variant(
        isReportable = true,
        event = "${variant.gene} ${variant.hgvsCodingOrProteinImpact}",
        driverLikelihood = DriverLikelihood.LOW,
        evidence = evidence,
        gene = variant.gene,
        geneRole = geneAlteration.geneRole,
        proteinEffect = when (geneAlteration.proteinEffect) {
            ProteinEffect.LOSS_OF_FUNCTION,
            ProteinEffect.LOSS_OF_FUNCTION_PREDICTED,
            ProteinEffect.GAIN_OF_FUNCTION,
            ProteinEffect.GAIN_OF_FUNCTION_PREDICTED -> geneAlteration.proteinEffect

            else -> ProteinEffect.NO_EFFECT
        },
        isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance,
        isHotspot = serveGeneAlteration is KnownHotspot || serveGeneAlteration is KnownCodon,
        ref = transcriptAnnotation.ref(),
        alt = transcriptAnnotation.alt(),


        canonicalImpact = impact(paveResponse.impact, transcriptAnnotation),
        otherImpacts = otherImpacts(paveResponse, transcriptAnnotation),
        chromosome = transcriptAnnotation.chromosome(),
        position = transcriptAnnotation.position(),
        type = variantType(transcriptAnnotation),
        variantAlleleFrequency = variant.variantAlleleFrequency
    )

    private fun impact(paveImpact: PaveImpact, transvarVariant: TransvarVariant): TranscriptImpact {

        val paveLiteAnnotation = paveLite.run(
            paveImpact.gene,
            paveImpact.transcript,
            transvarVariant.position()
        ) ?: throw IllegalStateException("PaveLite did not return a response for $transvarVariant")


        return TranscriptImpact(
            transcriptId = paveImpact.transcript,
            hgvsCodingImpact = paveImpact.hgvsCodingImpact,
            hgvsProteinImpact = paveImpact.hgvsProteinImpact,
            isSpliceRegion = paveImpact.spliceRegion,
            affectedExon = paveLiteAnnotation.affectedExon(),
            affectedCodon = paveLiteAnnotation.affectedCodon(),
            codingEffect = codingEffect(paveImpact.canonicalCodingEffect),
        )
    }

    fun otherImpacts(paveResponse: PaveResponse, transvarVariant: TransvarVariant): Set<TranscriptImpact> {
        return paveResponse.transcriptImpact
            .filter { it.gene == paveResponse.impact.gene && it.transcript != paveResponse.impact.transcript }
            .map { transcriptImpact(it, transvarVariant) }
            .toSet()
    }

    private fun transcriptImpact(paveTranscriptImpact: PaveTranscriptImpact, transvarVariant: TransvarVariant): TranscriptImpact {
        val paveLiteAnnotation = paveLite.run(
            paveTranscriptImpact.gene,
            paveTranscriptImpact.transcript,
            transvarVariant.position()
        ) ?: throw IllegalStateException("PaveLite did not return a response for $transvarVariant")

        return TranscriptImpact(
            transcriptId = paveTranscriptImpact.transcript,
            hgvsCodingImpact = paveTranscriptImpact.hgvsCodingImpact,
            hgvsProteinImpact = paveTranscriptImpact.hgvsProteinImpact,
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

    private fun variantType(transvarVariant: TransvarVariant): VariantType {
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

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PanelAnnotator::class.java)
    }
}