package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage

class EhrTumorDetailsExtractor : EhrExtractor<TumorDetails> {


    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<TumorDetails> {
        return ExtractionResult(
            TumorDetails(
                primaryTumorLocation = ehrPatientRecord.tumorDetails.tumorLocation,
                primaryTumorType = ehrPatientRecord.tumorDetails.tumorType,
                stage = TumorStage.valueOf(ehrPatientRecord.tumorDetails.tumorStage.name),
                hasBoneLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.BONE),
                hasBrainLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.BRAIN),
                hasLiverLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.LIVER),
                hasLungLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.LUNG),
                hasLymphNodeLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.LYMPH_NODE),
                hasCnsLesions = hasLesions(ehrPatientRecord.tumorDetails.lesions, EhrLesionLocation.CNS),
                otherLesions = ehrPatientRecord.tumorDetails.lesions.filter { it.location == EhrLesionLocation.OTHER }
                    .map { it.location.name }
            ), ExtractionEvaluation()
        )
    }

    private fun hasLesions(lesions: List<EhrLesion>, location: EhrLesionLocation): Boolean {
        return lesions.any { it.location == location }
    }
}