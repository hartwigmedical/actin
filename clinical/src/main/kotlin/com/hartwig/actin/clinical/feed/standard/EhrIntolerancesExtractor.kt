package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Intolerance

class EhrIntolerancesExtractor(private val atcModel: AtcModel, private val intoleranceCuration: CurationDatabase<IntoleranceConfig>) :
    EhrExtractor<List<Intolerance>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Intolerance>> {
        return ehrPatientRecord.allergies.map {
            Intolerance(
                name = it.name,
                category = it.category,
                type = "unspecified",
                clinicalStatus = it.clinicalStatus,
                verificationStatus = it.verificationStatus,
                criticality = it.severity,
                doids = emptySet(),
                subcategories = emptySet(),
                treatmentCategories = emptySet()
            )
        }
            .map {
                val curationResponse = CurationResponse.createFromConfigs(
                    intoleranceCuration.find(it.name),
                    ehrPatientRecord.patientDetails.hashedId,
                    CurationCategory.INTOLERANCE,
                    it.name,
                    "intolerance",
                    true
                )
                val curatedIntolerance = curationResponse.config()?.let { config ->
                    val subcategories = if (it.category.equals("medication", ignoreCase = true)) {
                        atcModel.resolveByName(config.name.lowercase())
                    } else emptySet()
                    it.copy(
                        name = config.name,
                        doids = config.doids,
                        subcategories = subcategories,
                        treatmentCategories = config.treatmentCategories
                    )
                } ?: it
                ExtractionResult(listOf(curatedIntolerance), curationResponse.extractionEvaluation)
            }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { (intolerances, aggregatedEval), (intolerance, eval) ->
                ExtractionResult(intolerances + intolerance, aggregatedEval + eval)
            }
    }
}