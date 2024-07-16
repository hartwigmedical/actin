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
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.pave.VariantTranscriptImpact
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.serve.datamodel.hotspot.KnownHotspot
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private const val TMB_HIGH_CUTOFF = 10.0

class PanelAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    private val transcriptAnnotator: VariantAnnotator,
    private val paveLite: PaveLite
) :
    MolecularAnnotator<PanelExtraction, PanelRecord> {

    override fun annotate(input: PanelExtraction): PanelRecord {
        val annotatedVariants = input.variants.mapNotNull {
            val externalVariantAnnotation = externalAnnotation(it)

            if (externalVariantAnnotation != null) {
                val transcriptImpactAnnotation = paveLite.run(
                    it.gene,
                    externalVariantAnnotation.transcript(),
                    externalVariantAnnotation.position()
                )

                val (evidence, serveGeneAlteration, geneAlteration) = serveEvidence(it, externalVariantAnnotation)

                createVariantWithEvidence(
                    it,
                    evidence,
                    geneAlteration,
                    serveGeneAlteration,
                    externalVariantAnnotation,
                    transcriptImpactAnnotation
                )
            } else {
                null
            }
        }

        val annotatedAmplifications = input.amplifications.map { inferredCopyNumber(it) }.map { annotatedInferredCopyNumber(it) }

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
            experimentType = ExperimentType.PANEL,
            testType = input.panelType,
            date = input.date,
            drivers = Drivers(variants = variantsWithDriverLikelihoodModel.toSet(), copyNumbers = annotatedAmplifications.toSet()),
            characteristics = MolecularCharacteristics(
                isMicrosatelliteUnstable = input.msi,
                tumorMutationalBurden = input.tmb,
                hasHighTumorMutationalBurden = input.tmb?.let { it > TMB_HIGH_CUTOFF }),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display()
        )
    }

    private fun annotatedInferredCopyNumber(it: CopyNumber): CopyNumber {
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(it))
        val geneAlteration = GeneAlterationFactory.convertAlteration(it.gene, evidenceDatabase.geneAlterationForCopyNumber(it))
        return it.copy(
            evidence = evidence,
            geneRole = geneAlteration.geneRole,
            proteinEffect = geneAlteration.proteinEffect,
            isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance
        )
    }

    private fun inferredCopyNumber(it: PanelAmplificationExtraction) = CopyNumber(
        gene = it.gene,
        geneRole = GeneRole.UNKNOWN,
        proteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance = null,
        isReportable = true,
        event = it.display(),
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = ActionableEvidenceFactory.createNoEvidence(),
        type = CopyNumberType.FULL_GAIN,
        minCopies = 6,
        maxCopies = 6
    )

    private fun externalAnnotation(it: PanelVariantExtraction): com.hartwig.actin.tools.variant.Variant? {
        val externalVariantAnnotation = transcriptAnnotator.resolve(it.gene, null, it.hgvsCodingImpact)

        if (externalVariantAnnotation == null) {
            LOGGER.error("Unable to resolve variant '$it' in variant annotator. See prior warnings.")
            return null
        }

        if (!externalVariantAnnotation.isCanonical) {
            LOGGER.error(
                "Annotator deems variant '$it' as on the non-canonical transcript '${externalVariantAnnotation.transcript()}. " +
                        "It cannot be annotated, filtering this variant from panel record"
            )
            return null
        }
        return externalVariantAnnotation
    }

    private fun serveEvidence(
        it: PanelVariantExtraction,
        transcriptPositionAndVariationAnnotation: com.hartwig.actin.tools.variant.Variant
    ): Triple<ActionableEvidence, com.hartwig.serve.datamodel.common.GeneAlteration?, GeneAlteration> {
        val criteria = VariantMatchCriteria(
            isReportable = true,
            gene = it.gene,
            chromosome = transcriptPositionAndVariationAnnotation.chromosome(),
            ref = transcriptPositionAndVariationAnnotation.ref(),
            alt = transcriptPositionAndVariationAnnotation.alt(),
            position = transcriptPositionAndVariationAnnotation.position(),
            type = variantType(transcriptPositionAndVariationAnnotation),
            codingEffect = codingEffect(transcriptPositionAndVariationAnnotation)
        )
        val evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVariant(criteria))
        val serveAlteration = evidenceDatabase.geneAlterationForVariant(criteria)
        val geneAlteration = GeneAlterationFactory.convertAlteration(
            it.gene, serveAlteration
        )
        return Triple(evidence, serveAlteration, geneAlteration)
    }

    private fun createVariantWithEvidence(
        it: PanelVariantExtraction,
        evidence: ActionableEvidence,
        geneAlteration: GeneAlteration,
        serveGeneAlteration: com.hartwig.serve.datamodel.common.GeneAlteration?,
        transcriptAnnotation: com.hartwig.actin.tools.variant.Variant,
        paveAnnotation: VariantTranscriptImpact?
    ) = Variant(
        isReportable = true,
        event = "${it.gene} ${it.hgvsCodingImpact}",
        driverLikelihood = DriverLikelihood.LOW,
        evidence = evidence,
        gene = it.gene,
        geneRole = geneAlteration.geneRole,
        proteinEffect = geneAlteration.proteinEffect,
        isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance,
        isHotspot = serveGeneAlteration is KnownHotspot,
        ref = transcriptAnnotation.ref(),
        alt = transcriptAnnotation.alt(),
        canonicalImpact = TranscriptImpact(
            transcriptId = transcriptAnnotation.transcript(),
            hgvsCodingImpact = it.hgvsCodingImpact,
            hgvsProteinImpact = transcriptAnnotation.hgvsProteinImpact() ?: "",
            isSpliceRegion = transcriptAnnotation.isSpliceRegion,
            affectedExon = paveAnnotation?.affectedExon(),
            affectedCodon = paveAnnotation?.affectedCodon(),
            codingEffect = codingEffect(transcriptAnnotation),
        ),
        chromosome = transcriptAnnotation.chromosome(),
        position = transcriptAnnotation.position(),
        type = variantType(transcriptAnnotation)
    )

    private fun variantType(transcriptAnnotation: com.hartwig.actin.tools.variant.Variant) = when (transcriptAnnotation.type()) {
        com.hartwig.actin.tools.variant.VariantType.SNV -> VariantType.SNV
        com.hartwig.actin.tools.variant.VariantType.INS -> VariantType.INSERT
        com.hartwig.actin.tools.variant.VariantType.DEL -> VariantType.DELETE
        com.hartwig.actin.tools.variant.VariantType.MNV -> VariantType.MNV
        else -> VariantType.UNDEFINED
    }

    private fun codingEffect(transcriptAnnotation: com.hartwig.actin.tools.variant.Variant) =
        when (transcriptAnnotation.codingEffect()) {
            com.hartwig.actin.tools.variant.CodingEffect.NONE -> CodingEffect.NONE
            com.hartwig.actin.tools.variant.CodingEffect.MISSENSE -> CodingEffect.MISSENSE
            com.hartwig.actin.tools.variant.CodingEffect.NONSENSE_OR_FRAMESHIFT -> CodingEffect.NONSENSE_OR_FRAMESHIFT
            com.hartwig.actin.tools.variant.CodingEffect.SPLICE -> CodingEffect.SPLICE
            com.hartwig.actin.tools.variant.CodingEffect.SYNONYMOUS -> CodingEffect.SYNONYMOUS
            else -> null
        }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PanelAnnotator::class.java)
    }
}