package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource

class ProvidedToxicityExtractor(private val toxicityCuration: CurationDatabase<ToxicityConfig>) :
    ProvidedDataExtractor<List<Toxicity>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Toxicity>> {

        return ehrPatientRecord.toxicities.map { toxicity ->
            val curatedToxicity = CurationResponse.createFromConfigs(
                toxicityCuration.find(toxicity.name),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.TOXICITY,
                toxicity.name,
                "toxicity"
            )

            ExtractionResult(listOfNotNull(curatedToxicity.config()?.let {
                Toxicity(
                    name = it.name,
                    grade = toxicity.grade,
                    categories = it.categories,
                    evaluatedDate = toxicity.evaluatedDate,
                    source = ToxicitySource.EHR
                )
            }), curatedToxicity.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }
}