package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult

interface EhrExtractor<T> {
    fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<T>
}