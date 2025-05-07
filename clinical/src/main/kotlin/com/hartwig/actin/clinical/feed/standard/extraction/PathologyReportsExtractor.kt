package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord

class PathologyReportsExtractor(config: EnvironmentConfiguration) : StandardDataExtractor<List<PathologyReport>> {
    private val requestingHospital = config.requestingHospital

    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PathologyReport>> {
        return ExtractionResult(
            ehrPatientRecord.tumorDetails.pathology.takeIf { !it.isNullOrEmpty() }?.map {
                PathologyReport(
                    tissueId = it.tissueId,
                    reportRequested = it.reportRequested,
                    source = it.source,
                    lab = if (it.lab == null && it.source.lowercase() == "internal") requestingHospital else it.lab,
                    diagnosis = it.diagnosis,
                    tissueDate = it.tissueDate,
                    authorisationDate = it.authorisationDate,
                    externalDate = it.externalDate,
                    report = it.rawPathologyReport
                )
            } ?: emptyList(), CurationExtractionEvaluation()
        )
    }
}