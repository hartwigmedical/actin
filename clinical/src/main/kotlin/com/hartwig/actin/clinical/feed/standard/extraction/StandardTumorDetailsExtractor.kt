package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardTumorDetailsExtractor(
    private val primaryTumorConfigCurationDatabase: CurationDatabase<PrimaryTumorConfig>,
    private val tumorStageDeriver: TumorStageDeriver
) : StandardDataExtractor<TumorDetails> {

    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<TumorDetails> {
        val input = "${ehrPatientRecord.tumorDetails.tumorLocation} | ${ehrPatientRecord.tumorDetails.tumorType}"

        val curatedTumorResponseFromOtherConditions = ehrPatientRecord.otherConditions.map {
            CurationResponse.createFromConfigs(
                primaryTumorConfigCurationDatabase.find(it.name),
                ehrPatientRecord.patientDetails.patientId, CurationCategory.PRIMARY_TUMOR, input, "primary tumor"
            )
        }.firstNotNullOfOrNull { it.config() }

        val curatedTumorResponse = CurationResponse.createFromConfigs(
            primaryTumorConfigCurationDatabase.find(input),
            ehrPatientRecord.patientDetails.patientId, CurationCategory.PRIMARY_TUMOR, input, "primary tumor", true
        )

        val tumorDetailsFromEhr = tumorDetails(ehrPatientRecord)
        val combinedTumorResponse = combinedTumorResponse(curatedTumorResponse, curatedTumorResponseFromOtherConditions)
        return combinedTumorResponse.config()?.let {
            val curatedTumorDetails = tumorDetailsFromEhr.copy(
                primaryTumorLocation = it.primaryTumorLocation,
                primaryTumorType = it.primaryTumorType,
                primaryTumorSubLocation = it.primaryTumorSubLocation,
                primaryTumorSubType = it.primaryTumorSubType,
                doids = it.doids
            )
            ExtractionResult(
                curatedTumorDetails.copy(derivedStages = tumorStageDeriver.derive(curatedTumorDetails)),
                combinedTumorResponse.extractionEvaluation
            )
        } ?: ExtractionResult(tumorDetailsFromEhr, combinedTumorResponse.extractionEvaluation)
    }

    private fun combinedTumorResponse(
        curatedTumorResponse: CurationResponse<PrimaryTumorConfig>,
        curatedTumorResponseFromOtherConditions: PrimaryTumorConfig?
    ) = curatedTumorResponseFromOtherConditions?.let { CurationResponse(setOf(it), CurationExtractionEvaluation()) }
        ?: curatedTumorResponse

    private fun tumorDetails(ehrPatientRecord: FeedPatientRecord): TumorDetails {
        with(ehrPatientRecord.tumorDetails) {
            val hasBrainOrGliomaTumor = tumorLocation == "Brain" || tumorType == "Glioma"
            return TumorDetails(
                primaryTumorLocation = tumorLocation,
                primaryTumorType = tumorType,
                doids = emptySet(),
                stage = stage?.let { TumorStage.valueOf(it) },
                hasMeasurableDisease = measurableDisease,
                hasBrainLesions = if (hasBrainOrGliomaTumor) false else hasBrainLesions,
                hasActiveBrainLesions = if (hasBrainOrGliomaTumor) false else hasActiveBrainLesions,
                hasCnsLesions = when {
                    hasBrainOrGliomaTumor -> false
                    hasBrainLesions == true -> true
                    else -> null
                },
                hasActiveCnsLesions = when {
                    hasBrainOrGliomaTumor -> false
                    hasActiveBrainLesions == true -> true
                    else -> null
                },
                hasBoneLesions = hasBoneLesions,
                hasLiverLesions = hasLiverLesions
            )
        }
    }
}