package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.PriorPrimaryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.TumorStatus
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord

class StandardPriorPrimariesExtractor(private val priorPrimaryCuration: CurationDatabase<PriorPrimaryConfig>) :
    StandardDataExtractor<List<PriorPrimary>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PriorPrimary>> {
        val priorPrimaries = fromPriorPrimaries(ehrPatientRecord)
        val priorPrimariesFromOtherConditions = fromOtherConditions(ehrPatientRecord)
        val priorPrimariesFromTreatmentHistory = fromTreatmentHistory(ehrPatientRecord)
        return (priorPrimaries + priorPrimariesFromOtherConditions + priorPrimariesFromTreatmentHistory).fold(
            ExtractionResult(
                emptyList(),
                CurationExtractionEvaluation()
            )
        ) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    private fun fromOtherConditions(ehrPatientRecord: ProvidedPatientRecord): List<ExtractionResult<List<PriorPrimary>>> {
        return extractFromSecondarySource(ehrPatientRecord, ehrPatientRecord.priorOtherConditions) { it.name }
    }

    private fun fromTreatmentHistory(ehrPatientRecord: ProvidedPatientRecord): List<ExtractionResult<List<PriorPrimary>>> {
        return extractFromSecondarySource(ehrPatientRecord, ehrPatientRecord.treatmentHistory) { it.treatmentName }
    }

    private fun <T> extractFromSecondarySource(
        ehrPatientRecord: ProvidedPatientRecord,
        sourceList: List<T>,
        inputAccessor: (T) -> String
    ): List<ExtractionResult<List<PriorPrimary>>> {
        return sourceList.map {
            curate(ehrPatientRecord.patientDetails.hashedId, inputAccessor.invoke(it))
        }
            .filter { it.configs.isNotEmpty() }
            .map {
                ExtractionResult(
                    it.configs.mapNotNull { c -> c.curated },
                    it.extractionEvaluation
                )
            }
    }

    private fun fromPriorPrimaries(ehrPatientRecord: ProvidedPatientRecord): List<ExtractionResult<List<PriorPrimary>>> =
        ehrPatientRecord.priorPrimaries.map {
            val input = "${it.tumorLocation} | ${it.tumorType}"
            val curatedPriorPrimary = curate(ehrPatientRecord.patientDetails.hashedId, input)
            ExtractionResult(listOfNotNull(curatedPriorPrimary.config()?.let { secondPrimaryConfig ->
                if (secondPrimaryConfig.ignore) {
                    null
                } else {
                    secondPrimaryConfig.curated?.copy(
                        diagnosedMonth = it.diagnosisDate?.monthValue,
                        diagnosedYear = it.diagnosisDate?.year,
                        lastTreatmentYear = it.lastTreatmentDate?.year,
                        lastTreatmentMonth = it.lastTreatmentDate?.monthValue,
                        status = it.status?.let { status -> TumorStatus.valueOf(status.uppercase()) } ?: TumorStatus.UNKNOWN
                    )
                }
            }), curatedPriorPrimary.extractionEvaluation)
        }

    private fun curate(
        patientId: String,
        input: String
    ) = CurationResponse.createFromConfigs(
        priorPrimaryCuration.find(input),
        patientId,
        CurationCategory.PRIOR_PRIMARY,
        input,
        "prior primary",
        false
    )
}