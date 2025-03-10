package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.ingestion.FeedValidationWarning

interface FeedValidator<T> {
    fun validate(feed: T): FeedValidation
}

data class FeedValidation(val valid: Boolean, val warnings: List<FeedValidationWarning> = emptyList())

class AlwaysValidFeedValidator<T> : FeedValidator<T> {
    override fun validate(feed: T): FeedValidation {
        return FeedValidation(true, emptyList())
    }
}
