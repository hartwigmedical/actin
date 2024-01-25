package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue

class EhrLabValuesExtractor : EhrExtractor<List<LabValue>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<LabValue>> {
        return ExtractionResult(ehrPatientRecord.labValues.map {
            ImmutableLabValue.builder().date(it.dateTime.toLocalDate()).name(it.measure).unit(LabUnit.fromString(it.unit)).value(it.value)
                .code(it.code).comparator("").build()
        }, ExtractionEvaluation())
    }
}