package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary

class EhrPriorPrimariesExtractor(private val priorPrimaryCuration: CurationDatabase<SecondPrimaryConfig>) :
    EhrExtractor<List<PriorSecondPrimary>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<PriorSecondPrimary>> {
        val priorPrimaries = fromPriorPrimaries(ehrPatientRecord)
        val priorPrimariesFromPriorOtherConditions = fromPriorOtherConditions(ehrPatientRecord)
        val priorPrimariesFromTreatmentHistory = fromTreatmentHistory(ehrPatientRecord)
        return (priorPrimaries + priorPrimariesFromPriorOtherConditions + priorPrimariesFromTreatmentHistory).fold(
            ExtractionResult(
                emptyList(),
                CurationExtractionEvaluation()
            )
        ) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    private fun fromPriorOtherConditions(ehrPatientRecord: EhrPatientRecord): List<ExtractionResult<List<PriorSecondPrimary>>> {
        return extractFromSecondarySource(ehrPatientRecord, ehrPatientRecord.priorOtherConditions) { it.name }
    }

    private fun fromTreatmentHistory(ehrPatientRecord: EhrPatientRecord): List<ExtractionResult<List<PriorSecondPrimary>>> {
        return extractFromSecondarySource(ehrPatientRecord, ehrPatientRecord.treatmentHistory) { it.treatmentName }
    }

    private fun <T> extractFromSecondarySource(
        ehrPatientRecord: EhrPatientRecord,
        target: List<T>,
        input: (T) -> String
    ): List<ExtractionResult<List<PriorSecondPrimary>>> {
        return target.map {
            curationResponse(ehrPatientRecord.patientDetails.hashedId, input.invoke(it))
        }.mapNotNull {
            it.config()?.curated?.let { priorSecondPrimary ->
                ExtractionResult(
                    listOf(priorSecondPrimary),
                    it.extractionEvaluation
                )
            }
        }
    }

    private fun fromPriorPrimaries(ehrPatientRecord: EhrPatientRecord): List<ExtractionResult<List<PriorSecondPrimary>>> =
        ehrPatientRecord.priorPrimaries.map {
            val input = "${it.tumorLocation} | ${it.tumorType}"
            val curatedPriorPrimary = curationResponse(ehrPatientRecord.patientDetails.hashedId, input)
            ExtractionResult(listOfNotNull(curatedPriorPrimary.config()?.let { secondPrimaryConfig ->
                if (secondPrimaryConfig.ignore) {
                    null
                } else {
                    secondPrimaryConfig.curated
                }
            }), curatedPriorPrimary.extractionEvaluation)
        }

    private fun curationResponse(
        patientId: String,
        input: String
    ) = CurationResponse.createFromConfigs(
        priorPrimaryCuration.find(input),
        patientId,
        CurationCategory.SECOND_PRIMARY,
        input,
        "prior primary"
    )
}