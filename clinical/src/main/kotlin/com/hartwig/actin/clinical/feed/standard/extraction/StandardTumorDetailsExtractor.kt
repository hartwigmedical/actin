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
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord

class StandardTumorDetailsExtractor(
    private val primaryTumorConfigCurationDatabase: CurationDatabase<PrimaryTumorConfig>,
    private val tumorStageDeriver: TumorStageDeriver
) : StandardDataExtractor<TumorDetails> {

    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<TumorDetails> {
        val input = "${ehrPatientRecord.tumorDetails.tumorLocation} | ${ehrPatientRecord.tumorDetails.tumorType}"

        val curatedTumorResponseFromOtherConditions = ehrPatientRecord.priorOtherConditions.map {
            CurationResponse.createFromConfigs(
                primaryTumorConfigCurationDatabase.find(it.name),
                ehrPatientRecord.patientDetails.hashedId, CurationCategory.PRIMARY_TUMOR, input, "primary tumor"
            )
        }.firstNotNullOfOrNull { it.config() }

        val curatedTumorResponse = CurationResponse.createFromConfigs(
            primaryTumorConfigCurationDatabase.find(input),
            ehrPatientRecord.patientDetails.hashedId, CurationCategory.PRIMARY_TUMOR, input, "primary tumor", true
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

    private fun tumorDetails(ehrPatientRecord: ProvidedPatientRecord): TumorDetails {
        val hasBrainOrGliomaTumor =
            ehrPatientRecord.tumorDetails.tumorLocation == "Brain" || ehrPatientRecord.tumorDetails.tumorType == "Glioma"
        val lesionDetails = ehrPatientRecord.tumorDetails.lesions
        return TumorDetails(
            primaryTumorLocation = ehrPatientRecord.tumorDetails.tumorLocation,
            primaryTumorType = ehrPatientRecord.tumorDetails.tumorType,
            doids = emptySet(),
            stage = ehrPatientRecord.tumorDetails.tumorStage?.let { TumorStage.valueOf(it) },
            hasMeasurableDisease = ehrPatientRecord.tumorDetails.measurableDisease,
            hasBrainLesions = if (hasBrainOrGliomaTumor) false else lesionDetails?.hasBrainLesions,
            hasActiveBrainLesions = if (hasBrainOrGliomaTumor) false else lesionDetails?.hasActiveBrainLesions,
            hasCnsLesions = when {
                hasBrainOrGliomaTumor -> false
                lesionDetails?.hasBrainLesions == true -> true
                else -> null
            },
            hasActiveCnsLesions = when {
                hasBrainOrGliomaTumor -> false
                lesionDetails?.hasActiveBrainLesions == true -> true
                else -> null
            },
            hasBoneLesions = lesionDetails?.hasBoneLesions,
            hasLiverLesions = lesionDetails?.hasLiverLesions
        )
    }
}