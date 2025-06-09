package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardClinicalStatusExtractor() : StandardDataExtractor<ClinicalStatus> {
    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<ClinicalStatus> {
        val mostRecentWho = feedPatientRecord.whoEvaluations.maxByOrNull { who -> who.evaluationDate }
        val clinicalStatus = ClinicalStatus(
            who = mostRecentWho?.status,
            hasComplications = feedPatientRecord.complications?.isNotEmpty()
        )
        return ExtractionResult(clinicalStatus, CurationExtractionEvaluation())
    }
}