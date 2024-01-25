package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource

class EhrToxicityExtractor: EhrExtractor<List<Toxicity>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Toxicity>> {
        return ExtractionResult(ehrPatientRecord.toxicities.map {
            ImmutableToxicity.builder().name(it.name).grade(it.grade).categories(it.categories).evaluatedDate(it.evaluatedDate)
                .source(ToxicitySource.EHR)
                .build()
        }, ExtractionEvaluation())
    }
}