package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedComorbidity
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.Comorbidity

class StandardComorbidityExtractor(
    private val comorbidityCuration: CurationDatabase<ComorbidityConfig>
) : StandardDataExtractor<List<Comorbidity>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Comorbidity>> {
        return with(ehrPatientRecord) {
            listOf(
                extractCategory(priorOtherConditions, ehrPatientRecord, CurationCategory.NON_ONCOLOGICAL_HISTORY, "non-oncological history"),
                extractCategory(complications, ehrPatientRecord, CurationCategory.COMPLICATION, "complication")
            ).flatten()
                .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
                    ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
                }
        }
    }

    private fun extractCategory(
        comorbidities: List<ProvidedComorbidity>,
        ehrPatientRecord: ProvidedPatientRecord,
        category: CurationCategory,
        configType: String
    ): List<ExtractionResult<List<Comorbidity>>> = comorbidities.map { comorbidity ->
        val curatedComorbidity = CurationResponse.createFromConfigs(
            comorbidityCuration.find(comorbidity.name),
            ehrPatientRecord.patientDetails.hashedId,
            category,
            comorbidity.name,
            configType,
            false
        )
        ExtractionResult(
            extracted = curatedComorbidity.configs.mapNotNull(ComorbidityConfig::curated).map { curated ->
                comorbidity.startDate?.let { sourceStartDate ->
                    curated.withDefaultYearAndMonth(sourceStartDate.year, sourceStartDate.monthValue)
                } ?: curated
            },
            evaluation = curatedComorbidity.extractionEvaluation
        )
    }
}