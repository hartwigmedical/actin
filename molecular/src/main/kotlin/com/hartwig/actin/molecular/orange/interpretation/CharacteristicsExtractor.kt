package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin
import com.hartwig.actin.molecular.datamodel.wgs.characteristics.CupPrediction
import com.hartwig.actin.molecular.orange.interpretation.ActionableEvidenceFactory.createNoEvidence
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
        val hasHighTumorMutationalBurden = hasHighStatus(purple.characteristics().tumorMutationalBurdenStatus())
        val hasHighTumorMutationalLoad = hasHighStatus(purple.characteristics().tumorMutationalLoadStatus())

        return MolecularCharacteristics(
            purity = purple.fit().purity(),
            ploidy = purple.fit().ploidy(),
            predictedTumorOrigin = predictedTumorOrigin,
            isMicrosatelliteUnstable = isMicrosatelliteUnstable,
            microsatelliteEvidence = createNoEvidence(),
            isHomologousRepairDeficient = isHomologousRepairDeficient,
            homologousRepairEvidence = createNoEvidence(),
            tumorMutationalBurden = purple.characteristics().tumorMutationalBurdenPerMb(),
            hasHighTumorMutationalBurden = hasHighTumorMutationalBurden,
            tumorMutationalBurdenEvidence = createNoEvidence(),
            tumorMutationalLoad = purple.characteristics().tumorMutationalLoad(),
            hasHighTumorMutationalLoad = hasHighTumorMutationalLoad,
            tumorMutationalLoadEvidence = createNoEvidence(),
            homologousRepairScore = homologousRepairScore
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
            // TODO (KZ): the classifiers are nullable in the CuppaPrediction class, is there a sane default or should we error out?
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
