package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue

class EhrLabValuesExtractor : EhrExtractor<List<LabValue>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<LabValue>> {
        return ExtractionResult(ehrPatientRecord.labValues.map {
            LabValue(
                date = it.evaluationTime.toLocalDate(),
                name = it.name,
                unit = LabUnit.valueOf(it.unit),
                value = it.value,
                code = it.code,
                comparator = "="
            )
        }, ExtractionEvaluation())
    }
}