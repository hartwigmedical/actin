package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TumorStatus

class EhrSecondPrimariesExtractor : EhrExtractor<List<PriorSecondPrimary>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<PriorSecondPrimary>> {
        return ExtractionResult(ehrPatientRecord.priorPrimaries.map {
            ImmutablePriorSecondPrimary.builder().tumorLocation(it.tumorLocalization).tumorType(it.tumorTypeDetails)
                .status(TumorStatus.valueOf(it.statusDetails)).diagnosedYear(it.diagnosisDate.year)
                .diagnosedMonth(it.diagnosisDate.monthValue).build()
        }, ExtractionEvaluation())
    }
}