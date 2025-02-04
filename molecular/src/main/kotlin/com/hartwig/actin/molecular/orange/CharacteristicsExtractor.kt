package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.characteristics.HrdType
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.chord.ChordStatus
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus

internal class CharacteristicsExtractor {

    fun extract(record: OrangeRecord): MolecularCharacteristics {
        val predictedTumorOrigin = record.cuppa()?.let {
            PredictedTumorOrigin(predictions = determineCupPredictions(it.predictions()))
        }
        val purple = record.purple()
        val chord = record.chord()

        return MolecularCharacteristics(
            purity = purple.fit().purity(),
            ploidy = purple.fit().ploidy(),
            predictedTumorOrigin = predictedTumorOrigin,
            isMicrosatelliteUnstable = isMSI(purple.characteristics().microsatelliteStatus()),
            microsatelliteEvidence = ExtractionUtil.noEvidence(),
            homologousRepairScore = chord?.hrdValue(),
            isHomologousRepairDeficient = chord?.let { isHRD(it.hrStatus()) },
            brca1Value = chord?.brca1Value(),
            brca2Value = chord?.brca2Value(),
            hrdType = chord?.hrdType()?.let { HrdType.valueOf(it.uppercase()) },
            homologousRepairEvidence = ExtractionUtil.noEvidence(),
            tumorMutationalBurden = purple.characteristics().tumorMutationalBurdenPerMb(),
            hasHighTumorMutationalBurden = hasHighStatus(purple.characteristics().tumorMutationalBurdenStatus()),
            tumorMutationalBurdenEvidence = ExtractionUtil.noEvidence(),
            tumorMutationalLoad = purple.characteristics().tumorMutationalLoad(),
            hasHighTumorMutationalLoad = hasHighStatus(purple.characteristics().tumorMutationalLoadStatus()),
            tumorMutationalLoadEvidence = ExtractionUtil.noEvidence()
        )
    }

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