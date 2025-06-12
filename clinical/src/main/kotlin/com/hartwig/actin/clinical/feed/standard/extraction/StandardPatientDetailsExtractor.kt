package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.FeedParseFunctions.parseGender
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardPatientDetailsExtractor : StandardDataExtractor<PatientDetails> {
    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<PatientDetails> {
        return ExtractionResult(
            with(feedPatientRecord.patientDetails) {
                PatientDetails(
                    gender = parseGender(gender),
                    birthYear = birthYear,
                    registrationDate = registrationDate,
                    hasHartwigSequencing = feedPatientRecord.patientDetails.hartwigMolecularDataExpected ?: false,
                    questionnaireDate = questionnaireDate,
                    sourceId = sourceId
                )
            }, CurationExtractionEvaluation()
        )
    }
}