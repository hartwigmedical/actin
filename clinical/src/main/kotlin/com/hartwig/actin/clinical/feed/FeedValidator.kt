package com.hartwig.actin.clinical.feed

interface FeedValidator<T> {
    fun validate(feed: T): Boolean
}

class AlwaysValidFeedValidator<T> : FeedValidator<T> {
    override fun validate(feed: T): Boolean {
        return true
    }
}
