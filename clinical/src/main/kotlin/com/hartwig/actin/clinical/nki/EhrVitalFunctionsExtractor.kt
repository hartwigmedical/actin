package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory

class EhrVitalFunctionsExtractor : EhrExtractor<List<VitalFunction>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<VitalFunction>> {
        return ExtractionResult(ehrPatientRecord.vitalFunctions.map {
            ImmutableVitalFunction.builder().date(it.date.atStartOfDay()).category(VitalFunctionCategory.fromString(it.measure))
                .value(it.value).unit(it.unit).subcategory(it.subcategory)
                .valid(true).build()
        }, ExtractionEvaluation())
    }

}