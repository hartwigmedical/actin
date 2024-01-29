package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion

class EhrBloodTransfusionExtractor : EhrExtractor<List<BloodTransfusion>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<BloodTransfusion>> {
        return ExtractionResult(ehrPatientRecord.bloodTransfusions.map {
            ImmutableBloodTransfusion.builder().product(it.product).date(it.evaluationTime.toLocalDate()).build()
        }, ExtractionEvaluation())
    }
}