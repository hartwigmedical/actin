package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult

interface StandardDataExtractor<T> {
    fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<T>
}