package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.PatientDetails

class StandardPatientDetailsExtractor : StandardDataExtractor<PatientDetails> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<PatientDetails> {
        return ExtractionResult(
            PatientDetails(
                gender = Gender.valueOf(enumeratedInput<ProvidedGender>(ehrPatientRecord.patientDetails.gender).toString()),
                birthYear = ehrPatientRecord.patientDetails.birthYear,
                registrationDate = ehrPatientRecord.patientDetails.registrationDate,
                hasHartwigSequencing = ehrPatientRecord.patientDetails.hartwigMolecularDataExpected
            ), CurationExtractionEvaluation()
        )
    }
}