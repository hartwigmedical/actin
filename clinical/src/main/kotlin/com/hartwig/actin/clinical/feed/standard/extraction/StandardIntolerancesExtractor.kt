package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance

class StandardIntolerancesExtractor(
    private val atcModel: AtcModel,
    private val intoleranceCuration: CurationDatabase<IntoleranceConfig>
) :
    StandardDataExtractor<List<Intolerance>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Intolerance>> {
        val intolerancesFromAllergies = ehrPatientRecord.allergies.map {
            Intolerance(
                name = it.name,
                icdCode = IcdCode("", null),
                category = it.category,
                clinicalStatus = it.clinicalStatus,
                verificationStatus = it.verificationStatus,
                criticality = it.severity,
                subcategories = emptySet()
            )
        }
            .map {
                val curationResponse = curateIntolerance(ehrPatientRecord, true, it.name, intoleranceCuration.find(it.name))
                val curatedIntolerance = curationResponse.config()?.let { config ->
                    it.copy(
                        name = config.name,
                        icdCode = config.icd,
                        subcategories = subcategoriesFromAtc(config)
                    )
                } ?: it
                ExtractionResult(listOf(curatedIntolerance), curationResponse.extractionEvaluation)
            }

        val intolerancesFromNonOncologicalHistory = ehrPatientRecord.priorOtherConditions.mapNotNull {
            val curationResponse = curateIntolerance(ehrPatientRecord, false, it.name, intoleranceCuration.find(it.name))
            if (curationResponse.configs.isNotEmpty()) {
                ExtractionResult(curationResponse.configs.map { config ->
                    Intolerance(
                        name = config.name,
                        icdCode = config.icd,
                        subcategories = subcategoriesFromAtc(config)
                    )
                }, CurationExtractionEvaluation())
            } else {
                null
            }
        }

        return (intolerancesFromAllergies + intolerancesFromNonOncologicalHistory).fold(
            ExtractionResult(
                emptyList(),
                CurationExtractionEvaluation()
            )
        ) { (intolerances, aggregatedEval), (intolerance, eval) ->
            ExtractionResult(intolerances + intolerance, aggregatedEval + eval)
        }

    }

    private fun subcategoriesFromAtc(config: IntoleranceConfig) = atcModel.resolveByName(config.name.lowercase())

    private fun curateIntolerance(
        ehrPatientRecord: ProvidedPatientRecord,
        requireUniqueness: Boolean,
        input: String,
        configs: Set<IntoleranceConfig>
    ) = CurationResponse.createFromConfigs(
        configs,
        ehrPatientRecord.patientDetails.hashedId,
        CurationCategory.INTOLERANCE,
        input,
        "intolerance",
        requireUniqueness
    )
}