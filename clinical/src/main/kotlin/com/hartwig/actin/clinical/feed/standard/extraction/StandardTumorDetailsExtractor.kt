package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
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

private const val CONCLUSION_LABEL = "Conclusie:"

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

        val lesionCurationResponse = extractLesions(ehrPatientRecord)
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
    ) = TumorDetails(
        primaryTumorLocation = ehrPatientRecord.tumorDetails.tumorLocation,
        primaryTumorType = ehrPatientRecord.tumorDetails.tumorType,
        stage = ehrPatientRecord.tumorDetails.tumorStage?.let { TumorStage.valueOf(it) },
        hasBoneLesions = hasLesions(lesions, LesionLocationCategory.BONE),
        hasSuspectedBoneLesions = hasSuspectedLesions(lesions, LesionLocationCategory.BONE),
        boneLesionsCount = countLesions(lesions, LesionLocationCategory.BONE),
        hasBrainLesions = hasLesions(lesions, LesionLocationCategory.BRAIN),
        hasSuspectedBrainLesions = hasSuspectedLesions(lesions, LesionLocationCategory.BRAIN),
        hasActiveBrainLesions = hasLesions(lesions, LesionLocationCategory.BRAIN, true),
        brainLesionsCount = countLesions(lesions, LesionLocationCategory.BRAIN),
        hasLiverLesions = hasLesions(lesions, LesionLocationCategory.LIVER),
        hasSuspectedLiverLesions = hasSuspectedLesions(lesions, LesionLocationCategory.LIVER),
        liverLesionsCount = countLesions(lesions, LesionLocationCategory.LIVER),
        hasLungLesions = hasLesions(lesions, LesionLocationCategory.LUNG),
        hasSuspectedLungLesions = hasSuspectedLesions(lesions, LesionLocationCategory.LUNG),
        lungLesionsCount = countLesions(lesions, LesionLocationCategory.LUNG),
        hasLymphNodeLesions = hasLesions(lesions, LesionLocationCategory.LYMPH_NODE),
        hasSuspectedLymphNodeLesions = hasSuspectedLesions(lesions, LesionLocationCategory.LYMPH_NODE),
        lymphNodeLesionsCount = countLesions(lesions, LesionLocationCategory.LYMPH_NODE),
        hasCnsLesions = hasLesions(lesions, LesionLocationCategory.CNS),
        hasSuspectedCnsLesions = hasSuspectedLesions(lesions, LesionLocationCategory.CNS),
        cnsLesionsCount = countLesions(lesions, LesionLocationCategory.CNS),
        hasActiveCnsLesions = hasLesions(lesions, LesionLocationCategory.CNS, true),
        otherLesions = lesions.filter { lesion -> lesion.ignore.not() }
            .filter { lesion -> lesion.category == null || lesion.category == LesionLocationCategory.LYMPH_NODE }
            .filter { lesion -> lesion.suspected != true }
            .map { lesion -> lesion.location },
        otherSuspectedLesions = lesions.filter { lesion -> lesion.ignore.not() }
            .filter { lesion -> lesion.category == null || lesion.category == LesionLocationCategory.LYMPH_NODE }
            .filter { lesion -> lesion.suspected == true }
            .map { lesion -> lesion.location },
        hasMeasurableDisease = ehrPatientRecord.tumorDetails.measurableDisease,
        doids = emptySet(),
        rawPathologyReport = ehrPatientRecord.tumorDetails.rawPathologyReport
    )

    private fun hasLesions(lesions: List<LesionLocationConfig>, location: LesionLocationCategory, active: Boolean? = null): Boolean {
        return lesions.any { it.category == location && (active == null || it.active == active) && (it.suspected == null || it.suspected == false) }
    }


    private fun hasSuspectedLesions(lesions: List<LesionLocationConfig>, location: LesionLocationCategory): Boolean? {
        return lesions.any { it.category == location && it.suspected == true }.takeIf { it }
    }

    private fun countLesions(lesions: List<LesionLocationConfig>, location: LesionLocationCategory): Int {
        return lesions.count { it.category == location }
    }

    private fun extractLesions(patientRecord: ProvidedPatientRecord): List<CurationResponse<LesionLocationConfig>> {
        val patientId = patientRecord.patientDetails.hashedId
        val lesionsFromLesionList = extractFromLesionList(patientRecord)
        val lesionsFromRadiologyReport = extractFromRadiologyReport(patientRecord.tumorDetails.lesionSite, patientId)
        val lesionsFromOtherConditions = extractFromSecondarySource(patientId, patientRecord.priorOtherConditions) { it.name }
        val lesionsFromTreatmentHistory = extractFromSecondarySource(patientId, patientRecord.treatmentHistory) { it.treatmentName }
        return lesionsFromLesionList + lesionsFromRadiologyReport + lesionsFromOtherConditions + lesionsFromTreatmentHistory
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

    private fun <T> extractFromSecondarySource(
        patientId: String,
        sourceList: List<T>,
        inputAccessor: (T) -> String
    ): List<CurationResponse<LesionLocationConfig>> {
        return sourceList.map {
            lesionCurationResponse(patientId, inputAccessor.invoke(it))
        }.filter { it.configs.isNotEmpty() }
    }

    private fun extractFromRadiologyReport(
        radiologyReport: String?,
        patientId: String
    ): List<CurationResponse<LesionLocationConfig>> {
        return radiologyReport?.substringAfter(CONCLUSION_LABEL)?.split(CONCLUSION_LABEL)?.flatMap { section ->
            section.substringBefore("\r\n\n\n").split("\n")
                .filter { it.isNotBlank() }
                .map { line -> line.trim().substringBeforeLast(".") }
                .map { line ->
                    lesionCurationResponse(patientId, line)
                }
        } ?: emptyList()
    }

    private fun lesionCurationResponse(
        patientId: String, input: String
    ) = CurationResponse.createFromConfigs(
        lesionCurationDatabase.find(input),
        patientId,
        CurationCategory.LESION_LOCATION,
        input,
        "lesion",
        false
    )
}