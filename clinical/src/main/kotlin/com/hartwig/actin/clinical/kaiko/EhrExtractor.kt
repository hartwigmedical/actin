package com.hartwig.actin.clinical.kaiko

import com.hartwig.actin.clinical.ExtractionResult

interface EhrExtractor<T> {
    fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<T>
}