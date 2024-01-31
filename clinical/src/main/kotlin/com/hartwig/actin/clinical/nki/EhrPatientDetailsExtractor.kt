package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.PatientDetails

class EhrPatientDetailsExtractor : EhrExtractor<PatientDetails> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<PatientDetails> {
        return ExtractionResult(
            PatientDetails(
                gender = Gender.valueOf(ehrPatientRecord.patientDetails.gender.name),
                birthYear = ehrPatientRecord.patientDetails.birthYear,
                registrationDate = ehrPatientRecord.patientDetails.registrationDate,
            ), ExtractionEvaluation()
        )
    }
}