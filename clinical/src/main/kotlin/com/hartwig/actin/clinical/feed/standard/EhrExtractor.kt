package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult

interface EhrExtractor<T> {
    fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<T>
}