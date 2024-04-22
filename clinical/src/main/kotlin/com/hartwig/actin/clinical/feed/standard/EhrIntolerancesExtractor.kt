package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Intolerance

class EhrIntolerancesExtractor(
    private val atcModel: AtcModel,
    private val intoleranceCuration: CurationDatabase<IntoleranceConfig>
) :
    EhrExtractor<List<Intolerance>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Intolerance>> {
        val intolerancesFromAllergies = ehrPatientRecord.allergies.map {
            Intolerance(
                name = it.name,
                category = it.category,
                clinicalStatus = it.clinicalStatus,
                verificationStatus = it.verificationStatus,
                criticality = it.severity,
                doids = emptySet(),
                subcategories = emptySet(),
                treatmentCategories = emptySet()
            )
        }
            .map {
                val curationResponse = curateIntolerance(ehrPatientRecord, true, it.name, intoleranceCuration.find(it.name))
                val curatedIntolerance = curationResponse.config()?.let { config ->
                    it.copy(
                        name = config.name,
                        doids = config.doids,
                        subcategories = subcategoriesFromAtc(config),
                        treatmentCategories = config.treatmentCategories
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
                        doids = config.doids,
                        subcategories = subcategoriesFromAtc(config),
                        treatmentCategories = config.treatmentCategories
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
        ehrPatientRecord: EhrPatientRecord,
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