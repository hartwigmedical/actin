package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.FeedParseFunctions.parseGender
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.feed.datamodel.FeedPatientRecord

class PatientDetailsExtractor {

    fun extract(feedRecord: FeedPatientRecord): ExtractionResult<PatientDetails> {
        return ExtractionResult(
            with(feedRecord.patientDetails) {
                PatientDetails(
                    gender = parseGender(gender),
                    birthYear = birthYear,
                    registrationDate = registrationDate,
                    hasHartwigSequencing = true,
                    questionnaireDate = questionnaireDate,
                    sourceId = sourceId
                )
            }, CurationExtractionEvaluation()
        )
    }
}