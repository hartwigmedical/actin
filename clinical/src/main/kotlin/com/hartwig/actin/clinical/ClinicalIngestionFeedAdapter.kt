package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.feed.ClinicalFeedIngestion

class ClinicalIngestionFeedAdapter(
    private val clinicalDataFeed: ClinicalFeedIngestion,
    private val curationDatabaseContext: CurationDatabaseContext
) {

    fun run(): IngestionResult {
        val records = clinicalDataFeed.ingest()
        var patientResults: HashMap<String, List<PatientIngestionResult>> = HashMap()
        records.forEach { key, value ->
            patientResults.put(key, listOf(value.get(0).first))
        }

        return IngestionResult(
            unusedConfigs = curationDatabaseContext.allUnusedConfig(records.values.flatten().map { it.second }),
            patientResults = patientResults
        )
    }
}