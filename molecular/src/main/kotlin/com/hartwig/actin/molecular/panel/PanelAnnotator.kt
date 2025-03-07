package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.PriorSequencingTest
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants

private const val TMB_HIGH_CUTOFF = 10.0
private const val PLOIDY = 2.0

class PanelAnnotator(
    private val evidenceDatabase: EvidenceDatabase,
    private val panelVariantAnnotator: PanelVariantAnnotator,
    private val panelFusionAnnotator: PanelFusionAnnotator,
    private val panelCopyNumberAnnotator: PanelCopyNumberAnnotator
) : MolecularAnnotator<PriorSequencingTest, PanelRecord> {

    override fun annotate(input: PriorSequencingTest): PanelRecord {
        val annotatedVariants = panelVariantAnnotator.annotate(input.variants)
        val annotatedAmplifications = panelCopyNumberAnnotator.annotate(input.amplifications)
        val annotatedDeletions = panelCopyNumberAnnotator.annotate(input.deletedGenes)
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
                    evidenceDatabase.evidenceForMicrosatelliteStatus(it)
                },
                tumorMutationalBurden = input.tumorMutationalBurden,
                hasHighTumorMutationalBurden = hasHighTumorMutationalBurden,
                tumorMutationalBurdenEvidence = hasHighTumorMutationalBurden?.let {
                    evidenceDatabase.evidenceForTumorMutationalBurdenStatus(it)
                },
                ploidy = PLOIDY
            ),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
            hasSufficientPurity = true,
            hasSufficientQuality = true
        )
    }
}