package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.ToxicityCuration
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.provided.ProvidedAllergy
import com.hartwig.actin.datamodel.clinical.provided.ProvidedComorbidity
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.provided.ProvidedToxicity
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource

class StandardComorbidityExtractor(
    private val comorbidityCuration: CurationDatabase<ComorbidityConfig>
) : StandardDataExtractor<List<Comorbidity>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Comorbidity>> {
        return with(ehrPatientRecord) {
            val patientId = patientDetails.hashedId
            listOf(
                extractCategory(priorOtherConditions, CurationCategory.NON_ONCOLOGICAL_HISTORY, "non-oncological history", patientId),
                extractCategory(complications, CurationCategory.COMPLICATION, "complication", patientId),
                extractToxicities(toxicities, patientId),
                extractIntolerances(allergies, patientId)
            ).flatten()
                .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
                    ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
                }
        }
    }

    private fun extractCategory(
        comorbidities: List<ProvidedComorbidity>, category: CurationCategory, configType: String, patientId: String
    ): List<ExtractionResult<List<Comorbidity>>> = comorbidities.map { comorbidity ->
        val curatedComorbidity = curate(comorbidity.name, patientId, category, configType)
        ExtractionResult(
            extracted = curatedComorbidity.configs.mapNotNull(ComorbidityConfig::curated).map { curated ->
                comorbidity.startDate?.let(curated::withDefaultDate) ?: curated
            },
            evaluation = curatedComorbidity.extractionEvaluation
        )
    }

    private fun extractToxicities(toxicities: List<ProvidedToxicity>, patientId: String): List<ExtractionResult<List<Toxicity>>> {
        return toxicities.map { toxicity ->
            val curatedToxicity = curate(toxicity.name, patientId, CurationCategory.TOXICITY, "toxicity")

            ExtractionResult(listOfNotNull(curatedToxicity.config()?.curated?.let { curated ->
                val curatedToxicity = curated as? ToxicityCuration
                Toxicity(
                    name = curated.name ?: toxicity.name,
                    grade = curatedToxicity?.grade ?: toxicity.grade,
                    icdCodes = curated.icdCodes,
                    evaluatedDate = toxicity.evaluatedDate,
                    source = ToxicitySource.EHR,
                    endDate = toxicity.endDate
                )
            }), curatedToxicity.extractionEvaluation)
        }
    }

    private fun extractIntolerances(intolerances: List<ProvidedAllergy>, patientId: String): List<ExtractionResult<List<Intolerance>>> {
        return intolerances.map { providedIntolerance ->
            val curatedIntolerance = curate(providedIntolerance.name, patientId, CurationCategory.INTOLERANCE, "intolerance", true)

            val intoleranceCuration = curatedIntolerance.config()?.curated
            val intolerance = with(providedIntolerance) {
                Intolerance(
                    intoleranceCuration?.name ?: name,
                    intoleranceCuration?.icdCodes ?: setOf(IcdCode("", null)),
                    clinicalStatus = clinicalStatus,
                    verificationStatus = verificationStatus,
                    criticality = severity
                )
            }
            ExtractionResult(listOf(intolerance), curatedIntolerance.extractionEvaluation)
        }
    }

    private fun curate(
        input: String, patientId: String, category: CurationCategory, configType: String, requireUniqueness: Boolean = false
    ): CurationResponse<ComorbidityConfig> {
        return CurationResponse.createFromConfigs(
            comorbidityCuration.find(input), patientId, category, input, configType, requireUniqueness
        )
    }
}