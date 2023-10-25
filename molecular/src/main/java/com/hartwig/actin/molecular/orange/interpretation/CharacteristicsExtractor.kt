package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.characteristics.CupPrediction
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableCupPrediction
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase
import com.hartwig.hmftools.datamodel.chord.ChordStatus
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus
import org.apache.logging.log4j.LogManager
import java.util.stream.Collectors

internal class CharacteristicsExtractor(private val evidenceDatabase: EvidenceDatabase) {
    fun extract(record: OrangeRecord): MolecularCharacteristics {
        val cuppa = record.cuppa()
        val predictedTumorOrigin: PredictedTumorOrigin? = if (cuppa != null) ImmutablePredictedTumorOrigin.builder()
            .predictions(determineCupPredictions(cuppa.predictions()))
            .build() else null
        val purple = record.purple()
        val isMicrosatelliteUnstable = isMSI(purple.characteristics().microsatelliteStatus())
        val chord = record.chord()
        val isHomologousRepairDeficient = if (chord != null) isHRD(chord.hrStatus()) else null
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
            LOGGER.warn("Cannot interpret microsatellite status '{}'", microsatelliteStatus)
            return null
        }

        private fun isHRD(hrStatus: ChordStatus): Boolean? {
            return when (hrStatus) {
                ChordStatus.HR_DEFICIENT -> true
                ChordStatus.HR_PROFICIENT -> false
                ChordStatus.UNKNOWN, ChordStatus.CANNOT_BE_DETERMINED -> null
            }
            LOGGER.warn("Cannot interpret homologous repair status '{}'", hrStatus)
            return null
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
            LOGGER.warn("Cannot interpret tumor mutational status: {}", tumorMutationalStatus)
            return null
        }

        private fun determineCupPredictions(cuppaPredictions: MutableList<CuppaPrediction>): MutableList<CupPrediction> {
            return cuppaPredictions.stream().map { cuppaPrediction: CuppaPrediction -> determineCupPrediction(cuppaPrediction) }.collect(Collectors.toList())
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
