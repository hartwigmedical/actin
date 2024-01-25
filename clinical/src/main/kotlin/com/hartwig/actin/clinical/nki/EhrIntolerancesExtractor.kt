package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.Intolerance

class EhrIntolerancesExtractor : EhrExtractor<List<Intolerance>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Intolerance>> {
        return ExtractionResult(ehrPatientRecord.allergies.map {
            ImmutableIntolerance.builder().name(it.description).category(it.category).type("unspecified").clinicalStatus(it.clinicalStatus)
                .verificationStatus(it.verificationStatus).criticality(it.severity).build()
        }, ExtractionEvaluation())
    }
}