package com.hartwig.actin.clinical.feed

interface FeedEntryCreator<T : FeedEntry> {
    fun fromLine(line: FeedLine): T
    fun isValid(line: FeedLine): Boolean
}