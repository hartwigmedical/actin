package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Intolerance

class EhrIntolerancesExtractor : EhrExtractor<List<Intolerance>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Intolerance>> {
        return ExtractionResult(ehrPatientRecord.allergies.map {
            Intolerance(
                name = it.name,
                category = it.category.name,
                type = "unspecified",
                clinicalStatus = it.clinicalStatus.name,
                verificationStatus = it.verificationStatus.name,
                criticality = it.severity.name,
                doids = emptySet(),
                subcategories = emptySet()
            )
        }, ExtractionEvaluation())
    }
}