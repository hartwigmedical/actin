package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableComplication

class EhrComplicationExtractor : EhrExtractor<List<Complication>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Complication>> {
        return ExtractionResult(ehrPatientRecord.complications.map {
            ImmutableComplication.builder().name(it.name).year(it.startDate.year).month(it.startDate.monthValue).build()
        }, ExtractionEvaluation())
    }

}
