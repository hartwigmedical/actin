package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.chord.ChordStatus
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction
import com.hartwig.hmftools.datamodel.finding.FindingItem
import com.hartwig.hmftools.datamodel.finding.FindingRecord
import com.hartwig.hmftools.datamodel.purple.PurpleCharacteristics
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus

object CharacteristicsExtraction {

    fun extract(record: FindingRecord): MolecularCharacteristics {
        val purityPloidyFit = record.purityPloidyFit();

        return MolecularCharacteristics(
            purity = purityPloidyFit.purity(),
            ploidy = purityPloidyFit.ploidy(),
            predictedTumorOrigin = determinePredictedTumorOrigin(record.predictedTumorOrigin),
            microsatelliteStability = determineMicrosatelliteStability(record.microsatelliteStability),
            homologousRecombination = determineHomologousRecombination(record.homologousRecombination),
            tumorMutationalBurden = determineTumorMutationalBurden(record.tumorMutationStatus()),
            tumorMutationalLoad = determineTumorMutationalLoad(record.tumorMutationStatus())
        )
    }

    private fun determinePredictedTumorOrigin(finding: FindingItem<com.hartwig.hmftools.datamodel.finding.PredictedTumorOrigin>): PredictedTumorOrigin? {
        return finding.finding()?.let {
            PredictedTumorOrigin(predictions = determineCupPredictions(it.predictions(), CuppaMode.valueOf(it.mode().toString())))
        }
    }

    private fun determineMicrosatelliteStability(finding: FindingItem<com.hartwig.hmftools.datamodel.finding.MicrosatelliteStability>): MicrosatelliteStability? {
        val microsatelliteStability = finding.finding;
        return isMicrosatelliteUnstable(microsatelliteStability.microsatelliteStatus)?.let { isUnstable ->
            MicrosatelliteStability(
                microsatelliteIndelsPerMb = microsatelliteStability.microsatelliteIndelsPerMb,
                isUnstable = isUnstable,
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }

    private fun determineHomologousRecombination(finding: FindingItem<com.hartwig.hmftools.datamodel.finding.HomologousRecombination>): HomologousRecombination? {
        return finding.finding()?.takeIf { it.hrStatus() in setOf(ChordStatus.HR_DEFICIENT, ChordStatus.HR_PROFICIENT) }?.let {
            HomologousRecombination(
                isDeficient = isHomologousRecombinationDeficient(it.hrStatus()),
                score = it.hrdValue(),
                type = HomologousRecombinationType.valueOf(it.hrdType().uppercase()),
                brca1Value = it.brca1Value(),
                brca2Value = it.brca2Value(),
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }

    private fun determineTumorMutationalBurden(finding: FindingItem<com.hartwig.hmftools.datamodel.finding.TumorMutationStatus>): TumorMutationalBurden? {
        var tumorMutationStatus = finding.finding;
        return hasHighStatus(tumorMutationStatus.tumorMutationalBurdenStatus())?.let { isHigh ->
            TumorMutationalBurden(
                score = tumorMutationStatus.tumorMutationalBurdenPerMb(),
                isHigh = isHigh,
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }

    private fun determineTumorMutationalLoad(finding: FindingItem<com.hartwig.hmftools.datamodel.finding.TumorMutationStatus>): TumorMutationalLoad? {
        var tumorMutationStatus = finding.finding;
        return hasHighStatus(tumorMutationStatus.tumorMutationalLoadStatus())?.let { isHigh ->
            TumorMutationalLoad(
                score = tumorMutationStatus.tumorMutationalLoad(),
                isHigh = isHigh,
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }

    private fun isMicrosatelliteUnstable(microsatelliteStatus: PurpleMicrosatelliteStatus): Boolean? {
        return when (microsatelliteStatus) {
            PurpleMicrosatelliteStatus.MSI -> true
            PurpleMicrosatelliteStatus.MSS -> false
            PurpleMicrosatelliteStatus.UNKNOWN -> null
        }
    }

    private fun isHomologousRecombinationDeficient(hrStatus: ChordStatus): Boolean {
        return when (hrStatus) {
            ChordStatus.HR_DEFICIENT -> true
            ChordStatus.HR_PROFICIENT -> false
            else -> throw IllegalStateException("HR status must be deficient or proficient")
        }
    }

    private fun hasHighStatus(tumorMutationalStatus: PurpleTumorMutationalStatus): Boolean? {
        return when (tumorMutationalStatus) {
            PurpleTumorMutationalStatus.HIGH -> {
                true
            }

            PurpleTumorMutationalStatus.LOW -> {
                false
            }

            PurpleTumorMutationalStatus.UNKNOWN -> {
                null
            }
        }
    }

    private fun determineCupPredictions(cuppaPredictions: List<CuppaPrediction>, cuppaMode: CuppaMode): List<CupPrediction> {
        return cuppaPredictions.map { cuppaPrediction: CuppaPrediction -> determineCupPrediction(cuppaPrediction, cuppaMode) }
    }

    private fun determineCupPrediction(cuppaPrediction: CuppaPrediction, cuppaMode: CuppaMode): CupPrediction {
        if (cuppaPrediction.snvPairwiseClassifier() == null || cuppaPrediction.genomicPositionClassifier() == null ||
            cuppaPrediction.featureClassifier() == null
        ) {
            throw IllegalStateException(
                "CUPPA classifiers are not supposed to be missing at this point " +
                        "in cuppa prediction: $cuppaPrediction"
            )
        }

        return CupPrediction(
            cancerType = cuppaPrediction.cancerType(),
            likelihood = cuppaPrediction.likelihood(),
            snvPairwiseClassifier = cuppaPrediction.snvPairwiseClassifier()!!,
            genomicPositionClassifier = cuppaPrediction.genomicPositionClassifier()!!,
            featureClassifier = cuppaPrediction.featureClassifier()!!,
            expressionPairWiseClassifier = cuppaPrediction.expressionPairwiseClassifier(),
            altSjCohortClassifier = cuppaPrediction.altSjCohortClassifier(),
            cuppaMode = cuppaMode
        )
    }
}