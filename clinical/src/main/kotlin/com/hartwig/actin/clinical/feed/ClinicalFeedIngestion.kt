package com.hartwig.actin.clinical.feed

import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.ingestion.CurationRequirement
import com.hartwig.actin.datamodel.clinical.ingestion.CurationResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import kotlin.collections.component1
import kotlin.collections.component2

interface ClinicalFeedIngestion {

    fun ingest(): List<Triple<ClinicalRecord, PatientIngestionResult, CurationExtractionEvaluation>>
}

fun curationResultsFromWarnings(curationWarnings: Iterable<CurationWarning>): Set<CurationResult> =
    curationWarnings.groupBy { it.category }
        .map { (category, warnings) ->
            CurationResult(category, warnings.map { CurationRequirement(it.feedInput, it.message) })
        }.toSet()
