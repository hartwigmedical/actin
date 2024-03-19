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
        return ehrPatientRecord.priorPrimaries.map {
            val input = "${it.tumorLocation} | ${it.tumorType}"
            val curatedPriorPrimary = CurationResponse.createFromConfigs(
                priorPrimaryCuration.find(input),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.SECOND_PRIMARY,
                input,
                "prior primary"
            )
            ExtractionResult(listOfNotNull(curatedPriorPrimary.config()?.let { secondPrimaryConfig ->
                if (!secondPrimaryConfig.ignore) {
                    null
                } else {
                    secondPrimaryConfig.curated
                }
            }), curatedPriorPrimary.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }
}