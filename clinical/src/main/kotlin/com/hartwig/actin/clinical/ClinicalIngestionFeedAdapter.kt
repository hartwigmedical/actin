package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.ClinicalFeedIngestion
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.ingestion.CurationRequirement
import com.hartwig.actin.datamodel.clinical.ingestion.CurationResult
import com.hartwig.actin.datamodel.clinical.ingestion.IngestionResult
import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionResult
import org.apache.logging.log4j.LogManager

class ClinicalIngestionFeedAdapter(
    private val clinicalDataFeed: ClinicalFeedIngestion,
    private val curationDatabaseContext: CurationDatabaseContext
) {
    private val logger = LogManager.getLogger(ClinicalIngestionFeedAdapter::class.java)

    fun run(): Pair<IngestionResult, List<ClinicalRecord>> {
        val (clinicalRecords, patientResults, evaluation) = clinicalDataFeed.ingest()
            .sortedWith { (record1, _, _), (record2, _, _) -> ClinicalRecordComparator().compare(record1, record2) }
            .fold(
                Triple(emptyList<ClinicalRecord>(), emptyList<PatientIngestionResult>(), CurationExtractionEvaluation())
            ) { (previousRecords, previousResults, previousEvaluations), (record, result, evaluation) ->
                Triple(previousRecords + record, previousResults + result, previousEvaluations + evaluation)
            }

        patientResults.flatMap(PatientIngestionResult::curationResults)
            .groupBy(CurationResult::category)
            .mapValues { (_, results) ->
                results.flatMap { it.requirements.map(CurationRequirement::message) }.toSet()
            }
            .filter { it.value.isNotEmpty() }
            .forEach { (category, messages) ->
                logger.info("Curation warning(s) for category '${category.categoryName}':\n${messages.joinToString("\n") { "  - $it" }}")
            }

        return IngestionResult(
            unusedConfigs = curationDatabaseContext.allUnusedConfig(evaluation),
            patientResults = patientResults
        ) to clinicalRecords
    }
}