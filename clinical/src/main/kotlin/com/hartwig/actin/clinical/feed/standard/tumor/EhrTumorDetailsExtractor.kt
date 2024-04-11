package com.hartwig.actin.clinical.feed.standard.tumor

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.feed.standard.EhrExtractor
import com.hartwig.actin.clinical.feed.standard.EhrPatientRecord

private const val CONCLUSIE_ = "Conclusie:"

class EhrTumorDetailsExtractor(
    private val primaryTumorConfigCurationDatabase: CurationDatabase<PrimaryTumorConfig>,
    private val lesionCurationDatabase: CurationDatabase<LesionLocationConfig>
) : EhrExtractor<TumorDetails> {


    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<TumorDetails> {
        val input = "${ehrPatientRecord.tumorDetails.tumorLocation} | ${ehrPatientRecord.tumorDetails.tumorType}"
        val curatedTumorResponse = CurationResponse.createFromConfigs(
            primaryTumorConfigCurationDatabase.find(input),
            ehrPatientRecord.patientDetails.hashedId, CurationCategory.PRIMARY_TUMOR, input, "primary tumor", true
        )
        val lesionCurationResponse =
            extractLesions(ehrPatientRecord)
        val curatedLesions = lesionCurationResponse.mapNotNull { it.config() }
        val tumorDetailsFromEhr = tumorDetails(ehrPatientRecord, curatedLesions)
        return curatedTumorResponse.config()?.let {
            ExtractionResult(
                tumorDetailsFromEhr.copy(
                    primaryTumorLocation = it.primaryTumorLocation,
                    primaryTumorType = it.primaryTumorType,
                    primaryTumorSubLocation = it.primaryTumorSubLocation,
                    primaryTumorSubType = it.primaryTumorSubType,
                    doids = it.doids
                ),
                curatedTumorResponse.extractionEvaluation + lesionCurationResponse.map { l -> l.extractionEvaluation }
                    .fold(CurationExtractionEvaluation()) { acc, extractionEvaluation -> acc + extractionEvaluation }
            )
        } ?: ExtractionResult(tumorDetailsFromEhr, curatedTumorResponse.extractionEvaluation)
    }

    private fun tumorDetails(
        ehrPatientRecord: EhrPatientRecord,
        lesions: List<LesionLocationConfig>
    ) = TumorDetails(
        primaryTumorLocation = ehrPatientRecord.tumorDetails.tumorLocation,
        primaryTumorType = ehrPatientRecord.tumorDetails.tumorType,
        stage = ehrPatientRecord.tumorDetails.tumorStage?.let { TumorStage.valueOf(it) },
        hasBoneLesions = hasLesions(lesions, LesionLocationCategory.BONE),
        boneLesionsCount = countLesions(lesions, LesionLocationCategory.BONE),
        hasBrainLesions = hasLesions(lesions, LesionLocationCategory.BRAIN),
        brainLesionsCount = countLesions(lesions, LesionLocationCategory.BRAIN),
        hasLiverLesions = hasLesions(lesions, LesionLocationCategory.LIVER),
        liverLesionsCount = countLesions(lesions, LesionLocationCategory.LIVER),
        hasLungLesions = hasLesions(lesions, LesionLocationCategory.LUNG),
        lungLesionsCount = countLesions(lesions, LesionLocationCategory.LUNG),
        hasLymphNodeLesions = hasLesions(lesions, LesionLocationCategory.LYMPH_NODE),
        lymphNodeLesionsCount = countLesions(lesions, LesionLocationCategory.LYMPH_NODE),
        hasCnsLesions = hasLesions(lesions, LesionLocationCategory.CNS),
        cnsLesionsCount = countLesions(lesions, LesionLocationCategory.CNS),
        otherLesions = lesions.filter { lesion -> lesion.ignore.not() }.filter { lesion -> lesion.category == null }
            .map { lesion -> lesion.location },
        hasMeasurableDisease = ehrPatientRecord.tumorDetails.measurableDisease,
        doids = emptySet()
    )

    private fun hasLesions(lesions: List<LesionLocationConfig>, location: LesionLocationCategory): Boolean {
        return lesions.any { it.category == location }
    }

    private fun countLesions(lesions: List<LesionLocationConfig>, location: LesionLocationCategory): Int {
        return lesions.count { it.category == location }
    }

    private fun extractLesions(patientRecord: EhrPatientRecord): List<CurationResponse<LesionLocationConfig>> {
        val patientId = patientRecord.patientDetails.hashedId
        val lesionsFromRadiologyReport = fromRadiologyReport(patientRecord.tumorDetails.lesionSite, patientId)
        val lesionsFromPriorOtherConditions = extractFromSecondarySource(patientId, patientRecord.priorOtherConditions) { it.name }
        val lesionsFromTreatmentHistory = extractFromSecondarySource(patientId, patientRecord.treatmentHistory) { it.treatmentName }
        return lesionsFromRadiologyReport + lesionsFromPriorOtherConditions + lesionsFromTreatmentHistory
    }

    private fun <T> extractFromSecondarySource(
        patientId: String,
        sourceList: List<T>,
        inputAccessor: (T) -> String
    ): List<CurationResponse<LesionLocationConfig>> {
        return sourceList.map {
            lesionCurationResponse(patientId, inputAccessor.invoke(it))
        }.filter { it.config() != null }
    }

    private fun fromRadiologyReport(
        radiologyReport: String?,
        patientId: String
    ): List<CurationResponse<LesionLocationConfig>> {
        return radiologyReport?.substringAfter(CONCLUSIE_)?.split(CONCLUSIE_)?.flatMap { section ->
            section.substringBefore("\r\n\n\n").split("\n")
                .filter { it.isNotBlank() }
                .map { line -> line.substringBeforeLast(".") }
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
        true
    )
}