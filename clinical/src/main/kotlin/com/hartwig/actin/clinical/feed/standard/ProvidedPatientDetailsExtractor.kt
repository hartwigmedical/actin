package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.PatientDetails

class ProvidedPatientDetailsExtractor : ProvidedDataExtractor<PatientDetails> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<PatientDetails> {
        return ExtractionResult(
            PatientDetails(
                gender = Gender.valueOf(enumeratedInput<ProvidedGender>(ehrPatientRecord.patientDetails.gender).toString()),
                birthYear = ehrPatientRecord.patientDetails.birthYear,
                registrationDate = ehrPatientRecord.patientDetails.registrationDate,
            ), CurationExtractionEvaluation()
        )
    }
}