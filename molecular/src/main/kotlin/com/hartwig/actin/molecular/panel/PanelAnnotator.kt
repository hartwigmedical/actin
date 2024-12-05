package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.PriorSequencingTest
import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletedGene
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.orange.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory

private const val TMB_HIGH_CUTOFF = 10.0
private const val MIN_COPY_NUMBER = 6
private const val MAX_COPY_NUMBER = 6
private const val PLOIDY = 2.0

class PanelAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val panelVariantAnnotator: PanelVariantAnnotator,
    private val panelFusionAnnotator: PanelFusionAnnotator
) : MolecularAnnotator<PriorSequencingTest, PanelRecord> {

    override fun annotate(input: PriorSequencingTest): PanelRecord {
        val annotatedVariants = panelVariantAnnotator.annotate(input.variants)
        val annotatedAmplifications = input.amplifications.map(::inferredCopyNumber).map(::annotatedInferredCopyNumber)
        val annotatedDeletions = input.deletedGenes.map(::inferredCopyNumber).map(::annotatedInferredCopyNumber)
        val annotatedFusions = panelFusionAnnotator.annotate(input.fusions, input.skippedExons)

        val hasHighTumorMutationalBurden = input.tumorMutationalBurden?.let { it > TMB_HIGH_CUTOFF }

        return PanelRecord(
            testedGenes = input.testedGenes ?: emptySet(),
            experimentType = ExperimentType.PANEL,
            testTypeDisplay = input.test,
            date = input.date,
            drivers = Drivers(
                variants = annotatedVariants,
                copyNumbers = annotatedAmplifications + annotatedDeletions,
                fusions = annotatedFusions,
            ),
            characteristics = MolecularCharacteristics(
                isMicrosatelliteUnstable = input.isMicrosatelliteUnstable,
                microsatelliteEvidence = input.isMicrosatelliteUnstable?.let {
                    ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForMicrosatelliteStatus(it))
                },
                tumorMutationalBurden = input.tumorMutationalBurden,
                hasHighTumorMutationalBurden = hasHighTumorMutationalBurden,
                tumorMutationalBurdenEvidence = hasHighTumorMutationalBurden?.let {
                    ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForTumorMutationalBurdenStatus(it))
                },
                ploidy = PLOIDY
            ),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
            hasSufficientPurity = true,
            hasSufficientQuality = true
        )
    }

    private fun annotatedInferredCopyNumber(copyNumber: CopyNumber): CopyNumber {
        val evidence = ClinicalEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(copyNumber))
        val geneAlteration =
            GeneAlterationFactory.convertAlteration(copyNumber.gene, evidenceDatabase.geneAlterationForCopyNumber(copyNumber))
        return copyNumber.copy(
            evidence = evidence,
            geneRole = geneAlteration.geneRole,
            proteinEffect = geneAlteration.proteinEffect,
            isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance
        )
    }

    private fun inferredCopyNumber(panelAmplificationExtraction: SequencedAmplification) = CopyNumber(
        gene = panelAmplificationExtraction.gene,
        geneRole = GeneRole.UNKNOWN,
        proteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance = null,
        isReportable = true,
        event = panelAmplificationExtraction.gene,
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = ClinicalEvidenceFactory.createNoEvidence(),
        canonicalImpact = TranscriptCopyNumberImpact( // Question for mr. Duyvesteyn: Is it oke for CDKN2A to only look at canonical for panels? We do have the transcript, so we could check if it is the canonical transcript. But don't know if that too complex.
            transcriptId = panelAmplificationExtraction.transcript ?: "",
            type = CopyNumberType.FULL_GAIN,
            minCopies = MIN_COPY_NUMBER,
            maxCopies = MAX_COPY_NUMBER
        ),
        otherImpacts = emptySet()
    )

    private fun inferredCopyNumber(sequencedDeletedGene: SequencedDeletedGene) = CopyNumber(
        gene = sequencedDeletedGene.gene,
        geneRole = GeneRole.UNKNOWN,
        proteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance = null,
        isReportable = true,
        event = sequencedDeletedGene.gene,
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = ClinicalEvidenceFactory.createNoEvidence(),
        canonicalImpact = TranscriptCopyNumberImpact(
            transcriptId = sequencedDeletedGene.transcript ?: "",
            type = CopyNumberType.LOSS,
            minCopies = 0,
            maxCopies = 0
        ),
        otherImpacts = emptySet()
    )
}