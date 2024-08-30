package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus

class StandardSurgeryExtractor : StandardDataExtractor<List<Surgery>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Surgery>> {
        return ExtractionResult(ehrPatientRecord.surgeries.map {
            Surgery(endDate = it.endDate, status = SurgeryStatus.valueOf(it.status))
        }, CurationExtractionEvaluation())
    }
}