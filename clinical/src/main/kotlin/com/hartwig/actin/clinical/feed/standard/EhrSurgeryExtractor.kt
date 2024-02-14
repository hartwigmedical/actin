package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus

class EhrSurgeryExtractor : EhrExtractor<List<Surgery>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Surgery>> {
        return ExtractionResult(ehrPatientRecord.surgeries.map {
            Surgery(endDate = it.endDate, status = SurgeryStatus.valueOf(it.status))
        }, CurationExtractionEvaluation())
    }
}