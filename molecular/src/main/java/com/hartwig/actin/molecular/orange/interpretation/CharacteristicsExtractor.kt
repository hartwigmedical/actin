package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.characteristics.CupPrediction
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableCupPrediction
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase
import com.hartwig.hmftools.datamodel.chord.ChordStatus
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus
import org.apache.logging.log4j.LogManager

internal class CharacteristicsExtractor(private val evidenceDatabase: EvidenceDatabase) {
    fun extract(record: OrangeRecord): MolecularCharacteristics {
        val predictedTumorOrigin = record.cuppa()?.let {
            ImmutablePredictedTumorOrigin.builder()
                .predictions(determineCupPredictions(it.predictions()))
                .build()
        }
        val purple = record.purple()
        val isMicrosatelliteUnstable = isMSI(purple.characteristics().microsatelliteStatus())
        val isHomologousRepairDeficient = record.chord()?.let { isHRD(it.hrStatus()) }
        val hasHighTumorMutationalBurden = hasHighStatus(purple.characteristics().tumorMutationalBurdenStatus())
        val hasHighTumorMutationalLoad = hasHighStatus(purple.characteristics().tumorMutationalLoadStatus())
        return ImmutableMolecularCharacteristics.builder()
            .purity(purple.fit().purity())
            .ploidy(purple.fit().ploidy())
            .predictedTumorOrigin(predictedTumorOrigin)
            .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
            .microsatelliteEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForMicrosatelliteStatus(
                isMicrosatelliteUnstable)))
            .isHomologousRepairDeficient(isHomologousRepairDeficient)
            .homologousRepairEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForHomologousRepairStatus(
                isHomologousRepairDeficient)))
            .tumorMutationalBurden(purple.characteristics().tumorMutationalBurdenPerMb())
            .hasHighTumorMutationalBurden(hasHighTumorMutationalBurden)
            .tumorMutationalBurdenEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForTumorMutationalBurdenStatus(
                hasHighTumorMutationalBurden)))
            .tumorMutationalLoad(purple.characteristics().tumorMutationalLoad())
            .hasHighTumorMutationalLoad(hasHighTumorMutationalLoad)
            .tumorMutationalLoadEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForTumorMutationalLoadStatus(
                hasHighTumorMutationalLoad)))
            .build()
    }

    companion object {
        private val LOGGER = LogManager.getLogger(CharacteristicsExtractor::class.java)
        private fun isMSI(microsatelliteStatus: PurpleMicrosatelliteStatus): Boolean? {
            return when (microsatelliteStatus) {
                PurpleMicrosatelliteStatus.MSI -> {
                    true
                }

                PurpleMicrosatelliteStatus.MSS -> {
                    false
                }

                PurpleMicrosatelliteStatus.UNKNOWN -> {
                    null
                }
            }
        }

        private fun isHRD(hrStatus: ChordStatus): Boolean? {
            return when (hrStatus) {
                ChordStatus.HR_DEFICIENT -> true
                ChordStatus.HR_PROFICIENT -> false
                ChordStatus.UNKNOWN, ChordStatus.CANNOT_BE_DETERMINED -> {
                    null
                }
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
            return ImmutableCupPrediction.builder()
                .cancerType(cuppaPrediction.cancerType())
                .likelihood(cuppaPrediction.likelihood())
                .snvPairwiseClassifier(cuppaPrediction.snvPairwiseClassifier()!!)
                .genomicPositionClassifier(cuppaPrediction.genomicPositionClassifier()!!)
                .featureClassifier(cuppaPrediction.featureClassifier()!!)
                .build()
        }
    }
}
