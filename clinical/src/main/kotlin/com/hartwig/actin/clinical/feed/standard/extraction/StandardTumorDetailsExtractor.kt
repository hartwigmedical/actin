package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.TumorStage

class StandardTumorDetailsExtractor(
    private val primaryTumorConfigCurationDatabase: CurationDatabase<PrimaryTumorConfig>,
    private val lesionCurationDatabase: CurationDatabase<LesionLocationConfig>,
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

        val lesionCurationResponse = extractFromLesionList(ehrPatientRecord)
        val curatedLesions = lesionCurationResponse.flatMap { it.configs }
        val tumorDetailsFromEhr = tumorDetails(ehrPatientRecord, curatedLesions)
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
                combinedTumorResponse.extractionEvaluation + lesionCurationResponse.map { l -> l.extractionEvaluation }
                    .fold(CurationExtractionEvaluation()) { acc, extractionEvaluation -> acc + extractionEvaluation }
            )
        } ?: ExtractionResult(tumorDetailsFromEhr, combinedTumorResponse.extractionEvaluation)
    }

    private fun combinedTumorResponse(
        curatedTumorResponse: CurationResponse<PrimaryTumorConfig>,
        curatedTumorResponseFromOtherConditions: PrimaryTumorConfig?
    ) = curatedTumorResponseFromOtherConditions?.let { CurationResponse(setOf(it), CurationExtractionEvaluation()) }
        ?: curatedTumorResponse

    private fun tumorDetails(
        ehrPatientRecord: ProvidedPatientRecord,
        lesions: List<LesionLocationConfig>
    ): TumorDetails {
        val hasBrainOrGliomaTumor =
            ehrPatientRecord.tumorDetails.tumorLocation == "Brain" || ehrPatientRecord.tumorDetails.tumorType == "Glioma"
        return TumorDetails(
            primaryTumorLocation = ehrPatientRecord.tumorDetails.tumorLocation,
            primaryTumorType = ehrPatientRecord.tumorDetails.tumorType,
            doids = emptySet(),
            stage = ehrPatientRecord.tumorDetails.tumorStage?.let { TumorStage.valueOf(it) },
            hasMeasurableDisease = ehrPatientRecord.tumorDetails.measurableDisease,
            hasBrainLesions = if (hasBrainOrGliomaTumor) false else hasBrainLesions(lesions),
            hasActiveBrainLesions = if (hasBrainOrGliomaTumor) false else hasBrainLesions(lesions, true),
            hasCnsLesions = determineCnsLesions(hasBrainOrGliomaTumor, lesions).first,
            hasActiveCnsLesions = determineCnsLesions(hasBrainOrGliomaTumor, lesions).second,
            hasBoneLesions = hasLesions(lesions, LesionLocationCategory.BONE),
            hasLiverLesions = hasLesions(lesions, LesionLocationCategory.LIVER),
            rawPathologyReport = ehrPatientRecord.tumorDetails.rawPathologyReport
        )
    }

    private fun hasLesions(lesions: List<LesionLocationConfig>, location: LesionLocationCategory, active: Boolean? = null): Boolean {
        return lesions.any { it.category == location && (active == null || it.active == active) && (it.suspected == null || it.suspected == false) }
    }

    private fun hasBrainLesions(lesions: List<LesionLocationConfig>, active: Boolean? = null) =
        if (active == true) hasLesions(lesions, LesionLocationCategory.BRAIN, true) else hasLesions(lesions, LesionLocationCategory.BRAIN)

    private fun determineCnsLesions(hasBrainOrGliomaTumor: Boolean, lesions: List<LesionLocationConfig>): Pair<Boolean?, Boolean?> {
        val hasCnsLesions = if (hasBrainLesions(lesions)) hasBrainLesions(lesions) else null
        val hasActiveCnsLesions = if (hasBrainLesions(lesions, true)) hasBrainLesions(lesions, true) else null
        return if (hasBrainOrGliomaTumor) Pair(false, false) else Pair(hasCnsLesions, hasActiveCnsLesions)
    }

    private fun extractFromLesionList(patientRecord: ProvidedPatientRecord): List<CurationResponse<LesionLocationConfig>> {
        val categories = LesionLocationCategory.entries.toSet().map { e -> e.name.uppercase() }
        return patientRecord.tumorDetails.lesions?.map {
            val locationAsEnumName = it.location.uppercase().replace(" ", "_")
            if (categories.contains(locationAsEnumName)) {
                CurationResponse(
                    configs = setOf(
                        LesionLocationConfig(
                            input = it.location,
                            location = it.location,
                            category = LesionLocationCategory.valueOf(locationAsEnumName),
                            active = it.active
                        )
                    )
                )
            } else {
                lesionCurationResponse(patientRecord.patientDetails.hashedId, it.location)
            }
        } ?: emptyList()
    }

    private fun lesionCurationResponse(patientId: String, input: String) = CurationResponse.createFromConfigs(
        lesionCurationDatabase.find(input),
        patientId,
        CurationCategory.LESION_LOCATION,
        input,
        "lesion",
        false
    )
}