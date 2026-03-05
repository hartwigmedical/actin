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
import com.hartwig.hmftools.finding.datamodel.FindingItem
import com.hartwig.hmftools.finding.datamodel.FindingList
import com.hartwig.hmftools.finding.datamodel.FindingRecord
import com.hartwig.hmftools.finding.datamodel.FindingsStatus
import com.hartwig.hmftools.finding.datamodel.TumorMutationStatus

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
            ?.takeIf {
                it.hrStatus() in setOf(
                    com.hartwig.hmftools.finding.datamodel.HomologousRecombination.HrStatus.HR_DEFICIENT,
                    com.hartwig.hmftools.finding.datamodel.HomologousRecombination.HrStatus.HR_PROFICIENT
                )
            }?.let {
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

    private fun determineTumorMutationalBurden(finding: FindingItem<TumorMutationStatus>): TumorMutationalBurden? {
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

    private fun determineTumorMutationalLoad(finding: FindingItem<TumorMutationStatus>): TumorMutationalLoad? {
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

    private fun isMicrosatelliteUnstable(microsatelliteStatus: com.hartwig.hmftools.finding.datamodel.MicrosatelliteStability.MicrosatelliteStatus): Boolean? {
        return when (microsatelliteStatus) {
            com.hartwig.hmftools.finding.datamodel.MicrosatelliteStability.MicrosatelliteStatus.MSI -> true
            com.hartwig.hmftools.finding.datamodel.MicrosatelliteStability.MicrosatelliteStatus.MSS -> false
            com.hartwig.hmftools.finding.datamodel.MicrosatelliteStability.MicrosatelliteStatus.UNKNOWN -> null
        }
    }

    private fun isHomologousRecombinationDeficient(hrStatus: com.hartwig.hmftools.finding.datamodel.HomologousRecombination.HrStatus): Boolean {
        return when (hrStatus) {
            com.hartwig.hmftools.finding.datamodel.HomologousRecombination.HrStatus.HR_DEFICIENT -> true
            com.hartwig.hmftools.finding.datamodel.HomologousRecombination.HrStatus.HR_PROFICIENT -> false
            else -> throw IllegalStateException("HR status must be deficient or proficient")
        }
    }

    private fun hasHighStatus(tumorMutationStatus: TumorMutationStatus.Status): Boolean? {
        return when (tumorMutationStatus) {
            TumorMutationStatus.Status.HIGH -> {
                true
            }

            TumorMutationStatus.Status.LOW -> {
                false
            }

            TumorMutationStatus.Status.UNKNOWN -> {
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