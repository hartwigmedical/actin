package com.hartwig.actin.molecular.orange

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
import com.hartwig.hmftools.datamodel.chord.ChordRecord
import com.hartwig.hmftools.datamodel.chord.ChordStatus
import com.hartwig.hmftools.datamodel.cuppa.CuppaData
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.purple.PurpleCharacteristics
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus

object CharacteristicsExtraction {

    fun extract(record: OrangeRecord): MolecularCharacteristics {
        val purple = record.purple()

        return MolecularCharacteristics(
            purity = purple.fit().purity(),
            ploidy = purple.fit().ploidy(),
            predictedTumorOrigin = determinePredictedTumorOrigin(record.cuppa()),
            microsatelliteStability = determineMicrosatelliteStability(purple.characteristics()),
            homologousRecombination = determineHomologousRecombination(record.chord()),
            tumorMutationalBurden = determineTumorMutationalBurden(purple.characteristics()),
            tumorMutationalLoad = determineTumorMutationalLoad(purple.characteristics())
        )
    }

    private fun determinePredictedTumorOrigin(cuppa: CuppaData?): PredictedTumorOrigin? {
        return cuppa?.let {
            PredictedTumorOrigin(predictions = determineCupPredictions(it.predictions(), CuppaMode.valueOf(it.mode().toString())))
        }
    }

    private fun determineMicrosatelliteStability(characteristics: PurpleCharacteristics): MicrosatelliteStability? {
        return isMicrosatelliteUnstable(characteristics.microsatelliteStatus())?.let { isUnstable ->
            MicrosatelliteStability(
                microsatelliteIndelsPerMb = characteristics.microsatelliteIndelsPerMb(),
                isUnstable = isUnstable,
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }

    private fun determineHomologousRecombination(chord: ChordRecord?): HomologousRecombination? {
        return chord?.let {
            HomologousRecombination(
                score = it.hrdValue(),
                isDeficient = isHomologousRecombinationDeficient(it.hrStatus()),
                type = HomologousRecombinationType.valueOf(it.hrdType().uppercase()),
                brca1Value = it.brca1Value(),
                brca2Value = it.brca2Value(),
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }

    private fun determineTumorMutationalBurden(characteristics: PurpleCharacteristics): TumorMutationalBurden? {
        return hasHighStatus(characteristics.tumorMutationalBurdenStatus())?.let { isHigh ->
            TumorMutationalBurden(
                score = characteristics.tumorMutationalBurdenPerMb(),
                isHigh = isHigh,
                evidence = ExtractionUtil.noEvidence()
            )
        }
    }

    private fun determineTumorMutationalLoad(characteristics: PurpleCharacteristics): TumorMutationalLoad? {
        return hasHighStatus(characteristics.tumorMutationalLoadStatus())?.let { isHigh ->
            TumorMutationalLoad(
                score = characteristics.tumorMutationalLoad(),
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

    private fun isHomologousRecombinationDeficient(hrStatus: ChordStatus): Boolean? {
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
            cuppaMode = cuppaMode
        )
    }
}