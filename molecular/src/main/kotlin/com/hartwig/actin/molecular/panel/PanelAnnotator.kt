package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.PriorSequencingTest
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.driver.Drivers
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
        
        return PanelRecord(
            testedGenes = input.testedGenes,
            experimentType = ExperimentType.PANEL,
            testTypeDisplay = input.test,
            date = input.date,
            drivers = Drivers(
                variants = annotatedVariants,
                copyNumbers = annotatedAmplifications + annotatedDeletions,
                homozygousDisruptions = emptyList(),
                disruptions = emptyList(),
                fusions = annotatedFusions,
                viruses = emptyList()
            ),
            characteristics = MolecularCharacteristics(
                purity = null,
                ploidy = PLOIDY,
                predictedTumorOrigin = null,
                microsatelliteStability = input.isMicrosatelliteUnstable?.let {
                    MicrosatelliteStability(
                        microsatelliteIndelsPerMb = null,
                        isUnstable = it,
                        evidenceDatabase.evidenceForMicrosatelliteStatus(it)
                    )
                },
                homologousRecombination = null,
                tumorMutationalBurden = input.tumorMutationalBurden?.let {
                    val isHigh = it > TMB_HIGH_CUTOFF
                    TumorMutationalBurden(
                        score = it,
                        isHigh = isHigh,
                        evidenceDatabase.evidenceForTumorMutationalBurdenStatus(isHigh)
                    )
                },
                tumorMutationalLoad = null
            ),
            evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
            hasSufficientPurity = true,
            hasSufficientQuality = true
        )
    }
}