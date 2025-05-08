package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord

class PathologyReportsExtractor : StandardDataExtractor<List<PathologyReport>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PathologyReport>> {
        return ExtractionResult(
            ehrPatientRecord.tumorDetails.pathology.takeIf { !it.isNullOrEmpty() }?.map {
                PathologyReport(
                    tissueId = it.tissueId,
                    lab = it.lab,
                    diagnosis = it.diagnosis,
                    tissueDate = it.tissueDate,
                    authorisationDate = it.authorisationDate,
                    reportDate = it.reportDate,
                    report = it.rawPathologyReport
                )
            } ?: emptyList(), CurationExtractionEvaluation()
        )
    }
}