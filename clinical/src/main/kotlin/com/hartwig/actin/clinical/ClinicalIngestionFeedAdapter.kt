package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.feed.ClinicalFeedIngestion
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator
import com.hartwig.actin.datamodel.clinical.ingestion.IngestionResult

class ClinicalIngestionFeedAdapter(
    private val clinicalDataFeed: ClinicalFeedIngestion,
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