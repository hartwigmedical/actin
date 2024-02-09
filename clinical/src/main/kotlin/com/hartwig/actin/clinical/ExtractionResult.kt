package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation

data class ExtractionResult<T>(val extracted: T, val evaluation: CurationExtractionEvaluation)