package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord

interface StandardDataExtractor<T> {
    fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<T>
}