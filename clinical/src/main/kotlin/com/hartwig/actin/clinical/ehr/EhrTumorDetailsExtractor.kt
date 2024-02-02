package com.hartwig.actin.clinical.ehr

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage

class EhrTumorDetailsExtractor(private val curationDatabase: CurationDatabase<PrimaryTumorConfig>) : EhrExtractor<TumorDetails> {


    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<TumorDetails> {
        val input = "${ehrPatientRecord.tumorDetails.tumorLocation} | ${ehrPatientRecord.tumorDetails.tumorType}"
        val curatedTumorResponse = CurationResponse.createFromConfigs(
            curationDatabase.find(input),
            ehrPatientRecord.patientDetails.patientId, CurationCategory.PRIMARY_TUMOR, input, "primary tumor", true
        )
        val tumorDetailsFromEhr = tumorDetails(ehrPatientRecord)
        return curatedTumorResponse.config()?.let {
            ExtractionResult(
                tumorDetailsFromEhr.copy(
                    primaryTumorLocation = it.primaryTumorLocation,
                    primaryTumorType = it.primaryTumorType,
                    doids = it.doids
                ),
                curatedTumorResponse.extractionEvaluation
            )
        } ?: ExtractionResult(tumorDetailsFromEhr, curatedTumorResponse.extractionEvaluation)
    }

    private fun tumorDetails(
        ehrPatientRecord: EhrPatientRecord
    ) = TumorDetails(
        primaryTumorLocation = ehrPatientRecord.tumorDetails.tumorLocation,
        primaryTumorType = ehrPatientRecord.tumorDetails.tumorType,
        primaryTumorExtraDetails = ehrPatientRecord.tumorDetails.tumorGradeDifferentiation,
        stage = ehrPatientRecord.tumorDetails.tumorStage?.let { TumorStage.valueOf(it) },
        hasBoneLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.BONE),
        hasBrainLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.BRAIN),
        hasLiverLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.LIVER),
        hasLungLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.LUNG),
        hasLymphNodeLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.LYMPH_NODE),
        hasCnsLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.CNS),
        otherLesions = ehrPatientRecord.tumorDetails.lesions.filter { lesion -> enumeratedInput<EhrLesionLocation>(lesion.location) == EhrLesionLocation.OTHER }
            .map { lesion -> lesion.location },
        doids = emptySet()
    )

    private fun hasLesions(lesions: List<EhrLesion>, location: EhrLesionLocation): Boolean {
        return lesions.any { enumeratedInput<EhrLesionLocation>(it.location) == location }
    }
}