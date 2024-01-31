package com.hartwig.actin.clinical.ehr

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Intolerance

class EhrIntolerancesExtractor : EhrExtractor<List<Intolerance>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Intolerance>> {
        return ExtractionResult(ehrPatientRecord.allergies.map {
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
        }, ExtractionEvaluation())
    }
}