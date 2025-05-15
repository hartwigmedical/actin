package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.feed.datamodel.FeedPatientRecord

interface StandardDataExtractor<T> {
    fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<T>
}