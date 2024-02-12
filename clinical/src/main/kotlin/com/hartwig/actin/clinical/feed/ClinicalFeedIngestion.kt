package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.PatientIngestionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation

interface ClinicalFeedIngestion {

    fun ingest(): List<Pair<PatientIngestionResult, CurationExtractionEvaluation>>

}