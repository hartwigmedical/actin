package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.ClinicalFeedIngestion
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.ingestion.IngestionResult
import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionResult

typealias IngestionResultWithClinicalRecords = Pair<IngestionResult, List<ClinicalRecord>>

class ClinicalIngestionFeedAdapter(
    private val clinicalDataFeed: ClinicalFeedIngestion,
    private val curationDatabaseContext: CurationDatabaseContext
) {

    fun run(): IngestionResultWithClinicalRecords {
        val (clinicalRecords, patientResults, evaluation) = clinicalDataFeed.ingest()
            .sortedWith { (record1, _, _), (record2, _, _) -> ClinicalRecordComparator().compare(record1, record2) }
            .fold(
                Triple(emptyList<ClinicalRecord>(), emptyList<PatientIngestionResult>(), CurationExtractionEvaluation())
            ) { (previousRecords, previousResults, previousEvaluations), (record, result, evaluation) ->
                Triple(previousRecords + record, previousResults + result, previousEvaluations + evaluation)
            }

        return IngestionResult(
            unusedConfigs = curationDatabaseContext.allUnusedConfig(evaluation),
            patientResults = patientResults
        ) to clinicalRecords
    }
}