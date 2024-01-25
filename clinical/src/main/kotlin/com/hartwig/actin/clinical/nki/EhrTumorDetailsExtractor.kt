package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage

class EhrTumorDetailsExtractor: EhrExtractor<TumorDetails> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<TumorDetails> {
        return ExtractionResult(
            ImmutableTumorDetails.builder().primaryTumorLocation(ehrPatientRecord.tumorDetails.tumorLocalization)
                .primaryTumorType(ehrPatientRecord.tumorDetails.tumorTypeDetails)
                .stage(TumorStage.valueOf(ehrPatientRecord.tumorDetails.tumorStage))
                .hasBoneLesions(true)
                .hasMeasurableDisease(ehrPatientRecord.tumorDetails.measurableDisease)
                .build(), ExtractionEvaluation()
        )
    }
}