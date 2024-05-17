package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult

interface ProvidedDataExtractor<T> {
    fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<T>
}