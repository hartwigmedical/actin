package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource

class EhrToxicityExtractor(private val toxicityCuration: CurationDatabase<ToxicityConfig>) : EhrExtractor<List<Toxicity>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Toxicity>> {

        return ehrPatientRecord.toxicities.map { toxicity ->
            val curatedToxicity = CurationResponse.createFromConfigs(
                toxicityCuration.find(toxicity.name),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.TOXICITY,
                toxicity.name,
                "toxicity"
            )
            ExtractionResult(listOfNotNull(curatedToxicity.config()?.let {
                Toxicity(
                    name = it.name,
                    grade = it.grade,
                    categories = it.categories,
                    evaluatedDate = toxicity.evaluatedDate,
                    source = ToxicitySource.EHR
                )
            }), curatedToxicity.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }
}