package com.hartwig.actin.clinical.feed.standard

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

class EhrTumorDetailsExtractor(
    private val primaryTumorConfigCurationDatabase: CurationDatabase<PrimaryTumorConfig>,
    private val lesionCurationDatabase: CurationDatabase<LesionLocationConfig>
) : EhrExtractor<TumorDetails> {


    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<TumorDetails> {
        val input = "${ehrPatientRecord.tumorDetails.tumorLocation} | ${ehrPatientRecord.tumorDetails.tumorType}"
        val curatedTumorResponse = CurationResponse.createFromConfigs(
            primaryTumorConfigCurationDatabase.find(input),
            ehrPatientRecord.patientDetails.hashedIdBase64(), CurationCategory.PRIMARY_TUMOR, input, "primary tumor", true
        )
        val lesionCurationResponse =
            extractLesions(ehrPatientRecord.patientDetails.hashedIdBase64(), ehrPatientRecord.tumorDetails.lesionSite)
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
        primaryTumorExtraDetails = ehrPatientRecord.tumorDetails.tumorGradeDifferentiation,
        stage = ehrPatientRecord.tumorDetails.tumorStage?.let { TumorStage.valueOf(it) },
        hasBoneLesions = hasLesions(lesions, LesionLocationCategory.BONE),
        hasBrainLesions = hasLesions(lesions, LesionLocationCategory.BRAIN),
        hasLiverLesions = hasLesions(lesions, LesionLocationCategory.LIVER),
        hasLungLesions = hasLesions(lesions, LesionLocationCategory.LUNG),
        hasLymphNodeLesions = hasLesions(lesions, LesionLocationCategory.LYMPH_NODE),
        hasCnsLesions = hasLesions(lesions, LesionLocationCategory.CNS),
        otherLesions = lesions.filter { lesion -> lesion.category == null }.map { lesion -> lesion.location },
        hasMeasurableDisease = ehrPatientRecord.tumorDetails.measurableDisease,
        doids = emptySet()
    )

    private fun hasLesions(lesions: List<LesionLocationConfig>, location: LesionLocationCategory): Boolean {
        return lesions.any { it.category == location }
    }

    private fun extractLesions(patientId: String, radiologyReport: String?): List<CurationResponse<LesionLocationConfig>> {
        return radiologyReport?.substringAfter("Conclusie:")?.substringBefore("Hersenen radiologie rapport:")?.split("\n")
            ?.filter { it.isNotBlank() }?.map { line -> line.substringBeforeLast(".") }?.map { line ->
                CurationResponse.createFromConfigs(
                    lesionCurationDatabase.find(line),
                    patientId,
                    CurationCategory.LESION_LOCATION,
                    line,
                    "lesion",
                    true
                )
            } ?: emptyList()
    }
}