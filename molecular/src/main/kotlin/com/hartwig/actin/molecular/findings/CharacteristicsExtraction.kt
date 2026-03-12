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

object CharacteristicsExtraction {

    fun extract(record: FindingRecord): MolecularCharacteristics {
        val purityPloidyFit = record.purityPloidyFit()

        return MolecularCharacteristics(
            purity = purityPloidyFit.purity(),
            ploidy = purityPloidyFit.ploidy(),
            predictedTumorOrigin = determinePredictedTumorOrigin(record.predictedTumorOrigins),
            microsatelliteStability = determineMicrosatelliteStability(record.microsatelliteStability),
            homologousRecombination = determineHomologousRecombination(record.homologousRecombination),
            tumorMutationalBurden = determineTumorMutationalBurden(record.tumorMutationalBurden()),
            tumorMutationalLoad = determineTumorMutationalLoad(record.tumorMutationalLoad())
        )
    }

    private fun determinePredictedTumorOrigin(findings: FindingList<com.hartwig.hmftools.finding.datamodel.PredictedTumorOrigin>): PredictedTumorOrigin? {
        return if (findings.status() == FindingsStatus.OK) {
            PredictedTumorOrigin(predictions = determineCupPredictions(findings.findings))
        } else null
    }

    private fun determineMicrosatelliteStability(finding: FindingItem<com.hartwig.hmftools.finding.datamodel.MicrosatelliteStability>): MicrosatelliteStability? {
        return finding.takeIf { it.status() == FindingsStatus.OK }?.finding()?.let {
            MicrosatelliteStability(
                microsatelliteIndelsPerMb = it.indelsPerMb,
                isUnstable = isMicrosatelliteUnstable(it.status),
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }


    private fun determineHomologousRecombination(finding: FindingItem<com.hartwig.hmftools.finding.datamodel.HomologousRecombination>): HomologousRecombination? {
        return finding.takeIf { it.status() == FindingsStatus.OK }?.finding()?.let {
            HomologousRecombination(
                isDeficient = isHomologousRecombinationDeficient(it.status()),
                score = it.hrdValue(),
                type = HomologousRecombinationType.valueOf(it.hrdType().uppercase()),
                brca1Value = it.brca1Value(),
                brca2Value = it.brca2Value(),
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }

    private fun determineTumorMutationalBurden(finding: FindingItem<com.hartwig.hmftools.finding.datamodel.TumorMutationalBurden>): TumorMutationalBurden? {
        return finding.takeIf { it.status() == FindingsStatus.OK }?.finding()?.let {
            TumorMutationalBurden(
                score = it.burdenPerMb(),
                isHigh = it.status() == com.hartwig.hmftools.finding.datamodel.TumorMutationalBurden.Status.HIGH,
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }

    private fun determineTumorMutationalLoad(finding: FindingItem<com.hartwig.hmftools.finding.datamodel.TumorMutationalLoad>): TumorMutationalLoad? {
        return finding.takeIf { it.status() == FindingsStatus.OK }?.finding()?.let {
            TumorMutationalLoad(
                score = it.load(),
                isHigh = it.status() == com.hartwig.hmftools.finding.datamodel.TumorMutationalLoad.Status.HIGH,
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }

    private fun isMicrosatelliteUnstable(microsatelliteStatus: com.hartwig.hmftools.finding.datamodel.MicrosatelliteStability.Status): Boolean {
        return when (microsatelliteStatus) {
            com.hartwig.hmftools.finding.datamodel.MicrosatelliteStability.Status.MSI -> true
            com.hartwig.hmftools.finding.datamodel.MicrosatelliteStability.Status.MSS -> false
        }
    }

    private fun isHomologousRecombinationDeficient(hrStatus: com.hartwig.hmftools.finding.datamodel.HomologousRecombination.Status): Boolean {
        return when (hrStatus) {
            com.hartwig.hmftools.finding.datamodel.HomologousRecombination.Status.HR_DEFICIENT -> true
            com.hartwig.hmftools.finding.datamodel.HomologousRecombination.Status.HR_PROFICIENT -> false
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