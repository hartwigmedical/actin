package com.hartwig.actin.clinical.feed.external

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.PatientDetails

class EhrPatientDetailsExtractor : EhrExtractor<PatientDetails> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<PatientDetails> {
        return ExtractionResult(
            PatientDetails(
                gender = Gender.valueOf(enumeratedInput<EhrGender>(ehrPatientRecord.patientDetails.gender).toString()),
                birthYear = ehrPatientRecord.patientDetails.birthYear,
                registrationDate = ehrPatientRecord.patientDetails.registrationDate,
            ), CurationExtractionEvaluation()
        )
    }
}