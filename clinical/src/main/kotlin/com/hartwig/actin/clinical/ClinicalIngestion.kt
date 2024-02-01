package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator

data class ExtractionResult<T>(val extracted: T, val evaluation: ExtractionEvaluation)

class ClinicalIngestion(
    private val clinicalDataFeed: ClinicalDataFeed,
    private val curationDatabaseContext: CurationDatabaseContext
) {

    fun run(): IngestionResult {
        val records = clinicalDataFeed.ingest()
            .sortedWith { (result1, _), (result2, _) ->
                ClinicalRecordComparator().compare(
                    result1.clinicalRecord,
                    result2.clinicalRecord
                )
            }
        return IngestionResult(
            unusedConfigs = curationDatabaseContext.allUnusedConfig(records.map { it.second }),
            patientResults = records.map { it.first }
        )
    }
}