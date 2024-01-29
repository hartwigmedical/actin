package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight


class EhrBodyWeightExtractor : EhrExtractor<List<BodyWeight>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<BodyWeight>> {
        return ExtractionResult(ehrPatientRecord.bodyWeights.map {
            ImmutableBodyWeight.builder().value(it.value).date(it.date.atStartOfDay()).unit(it.unit).valid(true).build()
        }, ExtractionEvaluation())
    }
}