package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.feed.datamodel.FeedPatientRecord

class PathologyReportsExtractor : StandardDataExtractor<List<PathologyReport>> {
    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<PathologyReport>> {
        return ExtractionResult(
            ehrPatientRecord.tumorDetails.pathology.takeIf { it.isNotEmpty() }?.map {
                PathologyReport(
                    tissueId = it.tissueId,
                    reportRequested = it.reportRequested,
                    source = it.source,
                    lab = it.lab,
                    diagnosis = it.diagnosis,
                    tissueDate = it.tissueDate,
                    authorisationDate = it.authorisationDate,
                    report = it.rawPathologyReport
                )
            } ?: emptyList(), CurationExtractionEvaluation()
        )
    }
}