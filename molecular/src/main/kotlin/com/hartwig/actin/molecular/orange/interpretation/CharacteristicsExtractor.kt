package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.orange.characteristics.CupPrediction
import com.hartwig.actin.molecular.evidence.ClinicalEvidenceFactory.createNoEvidence
import com.hartwig.hmftools.datamodel.chord.ChordStatus
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus

internal class CharacteristicsExtractor() {

    fun extract(record: OrangeRecord): MolecularCharacteristics {
        val predictedTumorOrigin = record.cuppa()?.let {
            PredictedTumorOrigin(predictions = determineCupPredictions(it.predictions()))
        }
        val purple = record.purple()
        val isMicrosatelliteUnstable = isMSI(purple.characteristics().microsatelliteStatus())
        val homologousRepairScore = record.chord()?.hrdValue()
        val isHomologousRepairDeficient = record.chord()?.let { isHRD(it.hrStatus()) }
        val brca1Value = record.chord()?.brca1Value()
        val brca2Value = record.chord()?.brca2Value()
        val hrdType = record.chord()?.hrdType()
        val hasHighTumorMutationalBurden = hasHighStatus(purple.characteristics().tumorMutationalBurdenStatus())
        val hasHighTumorMutationalLoad = hasHighStatus(purple.characteristics().tumorMutationalLoadStatus())

        return MolecularCharacteristics(
            purity = purple.fit().purity(),
            ploidy = purple.fit().ploidy(),
            predictedTumorOrigin = predictedTumorOrigin,
            isMicrosatelliteUnstable = isMicrosatelliteUnstable,
            microsatelliteEvidence = createNoEvidence(),
            homologousRepairScore = homologousRepairScore,
            isHomologousRepairDeficient = isHomologousRepairDeficient,
            brca1Value = brca1Value,
            brca2Value = brca2Value,
            hrdType = hrdType,
            homologousRepairEvidence = createNoEvidence(),
            tumorMutationalBurden = purple.characteristics().tumorMutationalBurdenPerMb(),
            hasHighTumorMutationalBurden = hasHighTumorMutationalBurden,
            tumorMutationalBurdenEvidence = createNoEvidence(),
            tumorMutationalLoad = purple.characteristics().tumorMutationalLoad(),
            hasHighTumorMutationalLoad = hasHighTumorMutationalLoad,
            tumorMutationalLoadEvidence = createNoEvidence()
        )
    }

    companion object {
        private fun isMSI(microsatelliteStatus: PurpleMicrosatelliteStatus): Boolean? {
            return when (microsatelliteStatus) {
                PurpleMicrosatelliteStatus.MSI -> true
                PurpleMicrosatelliteStatus.MSS -> false
                PurpleMicrosatelliteStatus.UNKNOWN -> null
            }
        }

        private fun isHRD(hrStatus: ChordStatus): Boolean? {
            return when (hrStatus) {
                ChordStatus.HR_DEFICIENT -> true
                ChordStatus.HR_PROFICIENT -> false
                ChordStatus.UNKNOWN, ChordStatus.CANNOT_BE_DETERMINED -> null
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

        private fun determineCupPredictions(cuppaPredictions: List<CuppaPrediction>): List<CupPrediction> {
            return cuppaPredictions.map { cuppaPrediction: CuppaPrediction -> determineCupPrediction(cuppaPrediction) }
        }

        private fun determineCupPrediction(cuppaPrediction: CuppaPrediction): CupPrediction {
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
                featureClassifier = cuppaPrediction.featureClassifier()!!
            )
        }
    }
}
