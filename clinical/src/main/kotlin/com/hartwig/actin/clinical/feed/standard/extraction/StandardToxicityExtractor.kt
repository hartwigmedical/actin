package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource

class StandardToxicityExtractor(
    private val comorbidityCuration: CurationDatabase<ComorbidityConfig>
) : StandardDataExtractor<List<Toxicity>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Toxicity>> {

        return ehrPatientRecord.toxicities.map { toxicity ->
            val curatedToxicity = CurationResponse.createFromConfigs(
                comorbidityCuration.find(toxicity.name),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.TOXICITY,
                toxicity.name,
                "toxicity"
            )

            ExtractionResult(listOfNotNull(curatedToxicity.config()?.curated?.let { curated ->
                val curatedToxicity = curated as? Toxicity
                Toxicity(
                    name = curated.name,
                    grade = curatedToxicity?.grade ?: toxicity.grade,
                    icdCodes = curated.icdCodes,
                    evaluatedDate = curatedToxicity?.evaluatedDate ?: toxicity.evaluatedDate,
                    source = ToxicitySource.EHR,
                    endDate = curatedToxicity?.endDate ?: toxicity.endDate
                )
            }), curatedToxicity.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }
}