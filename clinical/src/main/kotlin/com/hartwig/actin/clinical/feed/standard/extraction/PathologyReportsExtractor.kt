package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.feed.datamodel.FeedPatientRecord

class PathologyReportsExtractor : StandardDataExtractor<List<PathologyReport>> {
    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<List<PathologyReport>> {
        return ExtractionResult(
            feedPatientRecord.tumorDetails.pathology.takeIf { it.isNotEmpty() }?.map {
                PathologyReport(
                    tissueId = it.tissueId,
                    lab = it.lab,
                    diagnosis = it.diagnosis,
                    tissueDate = it.tissueDate,
                    authorisationDate = it.authorisationDate,
                    reportDate = it.reportDate,
                    report = it.rawPathologyReport,
                    reportHash = it.reportHash
                )
            } ?: emptyList(), CurationExtractionEvaluation()
        )
    }
}