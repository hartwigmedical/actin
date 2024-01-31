package com.hartwig.actin.clinical.ehr

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TumorStatus

class EhrSecondPrimariesExtractor :
    EhrExtractor<List<PriorSecondPrimary>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<PriorSecondPrimary>> {
        return ExtractionResult(ehrPatientRecord.priorPrimaries.map {
            PriorSecondPrimary(
                tumorLocation = it.tumorLocation,
                tumorType = it.tumorType,
                status = TumorStatus.valueOf(it.status.acceptedValues.name),
                diagnosedYear = it.diagnosisDate.year,
                diagnosedMonth = it.diagnosisDate.monthValue,
                tumorSubLocation = "",
                tumorSubType = "",
                treatmentHistory = ""
            )
        }, ExtractionEvaluation())
    }
}