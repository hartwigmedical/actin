package com.hartwig.actin.clinical.feed

interface FeedValidator<T> {
    fun validate(feed: T): FeedValidation
}

data class FeedValidation(val valid: Boolean, val warnings: List<FeedValidationWarning> = emptyList())

data class FeedValidationWarning(val subject: String, val message: String) : Comparable<FeedValidationWarning> {

    override fun compareTo(other: FeedValidationWarning): Int {
        return Comparator.comparing(FeedValidationWarning::subject)
            .thenComparing(FeedValidationWarning::message)
            .compare(this, other)
    }
}

class AlwaysValidFeedValidator<T> : FeedValidator<T> {
    override fun validate(feed: T): FeedValidation {
        return FeedValidation(true, emptyList())
    }
}
