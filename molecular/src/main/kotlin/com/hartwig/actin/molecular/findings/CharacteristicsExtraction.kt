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
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus
import com.hartwig.hmftools.finding.datamodel.FindingItem
import com.hartwig.hmftools.finding.datamodel.FindingList
import com.hartwig.hmftools.finding.datamodel.FindingRecord
import com.hartwig.hmftools.finding.datamodel.FindingsStatus

object CharacteristicsExtraction {

    fun extract(record: FindingRecord): MolecularCharacteristics {
        val purityPloidyFit = record.purityPloidyFit()

        return MolecularCharacteristics(
            purity = purityPloidyFit.purity(),
            ploidy = purityPloidyFit.ploidy(),
            predictedTumorOrigin = determinePredictedTumorOrigin(record.predictedTumorOrigins),
            microsatelliteStability = determineMicrosatelliteStability(record.microsatelliteStability),
            homologousRecombination = determineHomologousRecombination(record.homologousRecombination),
            tumorMutationalBurden = determineTumorMutationalBurden(record.tumorMutationStatus()),
            tumorMutationalLoad = determineTumorMutationalLoad(record.tumorMutationStatus())
        )
    }

    private fun determinePredictedTumorOrigin(findings: FindingList<com.hartwig.hmftools.finding.datamodel.PredictedTumorOrigin>): PredictedTumorOrigin? {
        return if (findings.status() == FindingsStatus.OK) {
            PredictedTumorOrigin(predictions = determineCupPredictions(findings.findings))
        } else null
    }

    private fun determineMicrosatelliteStability(finding: FindingItem<com.hartwig.hmftools.finding.datamodel.MicrosatelliteStability>): MicrosatelliteStability? {
        return finding.takeIf { it.status() == FindingsStatus.OK }?.finding()?.let { microsatelliteStability ->
            isMicrosatelliteUnstable(microsatelliteStability.microsatelliteStatus)?.let { isUnstable ->
                MicrosatelliteStability(
                    microsatelliteIndelsPerMb = microsatelliteStability.microsatelliteIndelsPerMb,
                    isUnstable = isUnstable,
                    evidence = ExtractionUtil.noEvidence()
                )
            }
        }
    }

    private fun determineHomologousRecombination(finding: FindingItem<com.hartwig.hmftools.finding.datamodel.HomologousRecombination>): HomologousRecombination? {
        return finding.takeIf { it.status() == FindingsStatus.OK }?.finding()
            ?.takeIf { it.hrStatus() in setOf(ChordStatus.HR_DEFICIENT, ChordStatus.HR_PROFICIENT) }?.let {
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

    private fun determineTumorMutationalBurden(finding: FindingItem<com.hartwig.hmftools.finding.datamodel.TumorMutationStatus>): TumorMutationalBurden? {
        return finding.takeIf { it.status() == FindingsStatus.OK }?.finding()?.let { tumorMutationStatus ->
            hasHighStatus(tumorMutationStatus.tumorMutationalBurdenStatus())?.let { isHigh ->
                TumorMutationalBurden(
                    score = tumorMutationStatus.tumorMutationalBurdenPerMb(),
                    isHigh = isHigh,
                    evidence = ExtractionUtil.noEvidence()
                )
            }
        }
    }

    private fun determineTumorMutationalLoad(finding: FindingItem<com.hartwig.hmftools.finding.datamodel.TumorMutationStatus>): TumorMutationalLoad? {
        return finding.takeIf { it.status() == FindingsStatus.OK }?.finding()?.let { tumorMutationStatus ->
            hasHighStatus(tumorMutationStatus.tumorMutationalLoadStatus())?.let { isHigh ->
                TumorMutationalLoad(
                    score = tumorMutationStatus.tumorMutationalLoad(),
                    isHigh = isHigh,
                    evidence = ExtractionUtil.noEvidence()
                )
            }
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

    private fun determineCupPredictions(cuppaPredictions: List<com.hartwig.hmftools.finding.datamodel.PredictedTumorOrigin>): List<CupPrediction> {
        return cuppaPredictions.map { cuppaPrediction -> determineCupPrediction(cuppaPrediction) }
    }

    private fun determineCupPrediction(cuppaPrediction: com.hartwig.hmftools.finding.datamodel.PredictedTumorOrigin): CupPrediction {
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
            cuppaMode = CuppaMode.valueOf(cuppaPrediction.mode().toString())
        )
    }
}