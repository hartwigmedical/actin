package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import java.time.LocalDate

class PathologyReportsExtractor : StandardDataExtractor<List<PathologyReport>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PathologyReport>> {
        println(ehrPatientRecord.tumorDetails.rawPathologyReport)
        return ExtractionResult(
            /*
            NOTE: The 1st section on rawPathologyReport is to be removed once the feed is updated with the new format.
                  This is here just to keep temporary backwards compatibility.
             */
            ehrPatientRecord.tumorDetails.rawPathologyReport?.let {
                listOf(
                    PathologyReport(
                        reportRequested = false,
                        source = "",
                        diagnosis = "",
                        tissueDate = LocalDate.of(1970, 1, 1),
                        authorisationDate = LocalDate.of(1970, 1, 1),
                        report = it
                    )
                )
            } ?: ehrPatientRecord.tumorDetails.pathology.takeIf { !it.isNullOrEmpty() }?.map {
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