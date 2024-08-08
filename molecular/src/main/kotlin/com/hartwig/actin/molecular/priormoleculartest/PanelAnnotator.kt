package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.*
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusionDetails
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.panel.*
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionableEvidenceFactory
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.orange.interpretation.GeneAlterationFactory
import com.hartwig.actin.molecular.paver.*
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.hmftools.common.fusion.KnownFusionCache
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
    private val paveLite: PaveLite,
    private val knownFusionCache: KnownFusionCache
) :
    MolecularAnnotator<PanelExtraction, PanelRecord> {

    override fun annotate(input: PanelExtraction): PanelRecord {
        val variantExtractions = indexVariantExtractionsToUniqueIds(input.variants)
        val transvarVariants = resolveVariants(variantExtractions)
        // TODO the pave call still runs runs when no variants. maybe this entire block to do with variants can be
        //   factored into a separate class, and short-circuit if no variants
        val paveAnnotations = annotateWithPave(transvarVariants)
        val variantsWithEvidence = annotateWithEvidence(transvarVariants, paveAnnotations, variantExtractions)
        val variantsWithDriverLikelihoodModel = annotateWithDriverLikelihood(variantsWithEvidence)

        val annotatedAmplifications = input.amplifications.map(::inferredCopyNumber).map(::annotatedInferredCopyNumber)

        val annotatedFusions =
            (input.fusions.map { createFusion(it) } + input.skippedExons.map { createFusionFromExonSkip(it) })
                .map { annotateFusion(it) }
                .toSet()

        return PanelRecord(
            panelExtraction = input,
            experimentType = ExperimentType.PANEL,
            testTypeDisplay = input.panelType,
            date = input.date,
            drivers = Drivers(
                variants = variantsWithDriverLikelihoodModel.toSet(),
                copyNumbers = annotatedAmplifications.toSet(),
                fusions = annotatedFusions,
            ),
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

    private fun annotateWithEvidence(
        transvarVariants: Map<String, TransvarVariant>,
        paveAnnotations: Map<String, PaveResponse>,
        variantExtractions: Map<String, PanelVariantExtraction>
    ): List<Variant> {
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
        it: PanelVariantExtraction,
        evidence: ActionableEvidence,
        geneAlteration: GeneAlteration,
        serveGeneAlteration: ServeGeneAlteration?,
        transcriptAnnotation: TransvarVariant,
        paveResponse: PaveResponse
    ) = Variant(
        isReportable = true,
        event = "${it.gene} ${it.hgvsCodingOrProteinImpact}",
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
        isHotspot = serveGeneAlteration is KnownHotspot || serveGeneAlteration is KnownCodon,
        ref = transcriptAnnotation.ref(),
        alt = transcriptAnnotation.alt(),


        canonicalImpact = impact(paveResponse.impact, transcriptAnnotation),
        otherImpacts = otherImpacts(paveResponse, transcriptAnnotation),
        chromosome = transcriptAnnotation.chromosome(),
        position = transcriptAnnotation.position(),
        type = variantType(transcriptAnnotation)
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

    private fun createFusion(panelFusionExtraction: PanelFusionExtraction): Fusion {
        return Fusion(
            geneStart = panelFusionExtraction.geneUp ?: "", // TODO no no we don't want empty strings
            geneEnd = panelFusionExtraction.geneDown ?: "",
            geneTranscriptStart = "",  // TODO also nullable here, or move to extended
            geneTranscriptEnd = "",
            driverType = determineFusionDriverType(panelFusionExtraction.geneUp, panelFusionExtraction.geneDown),
            proteinEffect = ProteinEffect.UNKNOWN,
            isReportable = true,
            event = panelFusionExtraction.display(),
            driverLikelihood = DriverLikelihood.HIGH, // TODO can we model this? defaulting to high
            evidence = ActionableEvidenceFactory.createNoEvidence(),
            extendedFusionDetails = ExtendedFusionDetails(
                fusedExonUp = 0,  // TODO make nullable? using 0 which is not valid exon
                fusedExonDown = 0,
                isAssociatedWithDrugResistance = null
            )
        )
    }

    private fun createFusionFromExonSkip(panelSkippedExonsExtraction: PanelSkippedExonsExtraction): Fusion {
        // TODO note that we have exons here without knowing the transcript! maybe we should plug in canonical?
        return Fusion(
            geneStart = panelSkippedExonsExtraction.gene,
            geneEnd = panelSkippedExonsExtraction.gene,
            geneTranscriptStart = "",  // TODO nullable here, or move to extended
            geneTranscriptEnd = "",
            driverType = determineFusionDriverType(panelSkippedExonsExtraction.gene, panelSkippedExonsExtraction.gene),
            proteinEffect = ProteinEffect.UNKNOWN,
            isReportable = true,
            event = panelSkippedExonsExtraction.display(),
            driverLikelihood = DriverLikelihood.HIGH, // TODO can we model this? defaulting to high
            evidence = ActionableEvidenceFactory.createNoEvidence(),
            extendedFusionDetails = ExtendedFusionDetails(
                fusedExonUp = panelSkippedExonsExtraction.start - 1, // TODO so we can't represent skips of first/last exons, throw error?
                fusedExonDown = panelSkippedExonsExtraction.end + 1,
                isAssociatedWithDrugResistance = null
            )
        )
    }

    private fun determineFusionDriverType(geneUp: String?, geneDown: String?): FusionDriverType {
        if (geneUp != null && geneDown != null) {
            if (knownFusionCache.hasKnownFusion(geneUp, geneDown)) {
                return FusionDriverType.KNOWN_PAIR
            }

            if (geneUp == geneDown && knownFusionCache.hasExonDelDup(geneUp)) {
                return FusionDriverType.KNOWN_PAIR_DEL_DUP
            }
        }

        val isPromiscuous5 = geneUp?.let { knownFusionCache.hasPromiscuousFiveGene(it) } ?: false
        val isPromiscuous3 = geneDown?.let { knownFusionCache.hasPromiscuousThreeGene(it) } ?: false

        when {
            isPromiscuous5 && isPromiscuous3 -> return FusionDriverType.PROMISCUOUS_BOTH
            isPromiscuous5 -> return FusionDriverType.PROMISCUOUS_5
            isPromiscuous3 -> return FusionDriverType.PROMISCUOUS_3
        }

        return FusionDriverType.NONE
    }

    // TODO duplication with molecularRecordAnnotator
    private fun annotateFusion(fusion: Fusion): Fusion {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForFusion(createFusionMatchCriteria(fusion)))
        val knownFusion = evidenceDatabase.lookupKnownFusion(createFusionMatchCriteria(fusion))

        val proteinEffect = if (knownFusion == null) ProteinEffect.UNKNOWN else {
            GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
        }
        val isAssociatedWithDrugResistance = knownFusion?.associatedWithDrugResistance()

        return fusion.copy(
            evidence = evidence,
            proteinEffect = proteinEffect,
            extendedFusionDetails = fusion.extendedFusionOrThrow().copy(
                isAssociatedWithDrugResistance = isAssociatedWithDrugResistance,
            ),
        )
    }

    private fun createFusionMatchCriteria(fusion: Fusion) = FusionMatchCriteria(
        isReportable = fusion.isReportable,
        geneStart = fusion.geneStart,
        geneEnd = fusion.geneEnd,
        driverType = fusion.driverType
    )

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