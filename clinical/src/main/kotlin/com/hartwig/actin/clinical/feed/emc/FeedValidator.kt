package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.ingestion.FeedValidationWarning

data class FeedValidation(val valid: Boolean, val warnings: List<FeedValidationWarning> = emptyList())