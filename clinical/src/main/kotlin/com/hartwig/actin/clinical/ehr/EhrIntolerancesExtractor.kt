package com.hartwig.actin.clinical.ehr

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Intolerance

class EhrIntolerancesExtractor(private val intoleranceCuration: CurationDatabase<IntoleranceConfig>, private val atcModel: AtcModel) :
    EhrExtractor<List<Intolerance>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Intolerance>> {
        return ehrPatientRecord.allergies.map {
            Intolerance(
                name = it.name,
                category = it.category.acceptedValues.name,
                type = "unspecified",
                clinicalStatus = if (it.clinicalStatus.acceptedValues != EhrAllergyClinicalStatus.OTHER) it.clinicalStatus.acceptedValues.name else it.clinicalStatus.input,
                verificationStatus = if (it.verificationStatus.acceptedValues != EhrAllergyVerificationStatus.OTHER) it.verificationStatus.acceptedValues.name else it.verificationStatus.input,
                criticality = if (it.severity.acceptedValues != EhrAllergySeverity.OTHER) it.severity.acceptedValues.name else it.severity.input,
                doids = emptySet(),
                subcategories = emptySet()
            )
        }
            .map {
                val curationResponse = CurationResponse.createFromConfigs(
                    intoleranceCuration.find(it.name),
                    ehrPatientRecord.patientDetails.patientId,
                    CurationCategory.INTOLERANCE,
                    it.name,
                    "intolerance",
                    true
                )
                val curatedIntolerance = curationResponse.config()?.let { config ->
                    val subcategories = if (it.category.equals("medication", ignoreCase = true)) {
                        atcModel.resolveByName(config.name.lowercase())
                    } else emptySet()
                    it.copy(name = config.name, doids = config.doids, subcategories = subcategories)
                } ?: it
                ExtractionResult(listOf(curatedIntolerance), curationResponse.extractionEvaluation)
            }
            .fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { (intolerances, aggregatedEval), (intolerance, eval) ->
                ExtractionResult(intolerances + intolerance, aggregatedEval + eval)
            }
    }
}