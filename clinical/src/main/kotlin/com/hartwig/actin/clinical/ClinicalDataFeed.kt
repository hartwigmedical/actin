package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation

interface ClinicalDataFeed {
    fun ingest(): List<Pair<PatientIngestionResult, ExtractionEvaluation>>
}