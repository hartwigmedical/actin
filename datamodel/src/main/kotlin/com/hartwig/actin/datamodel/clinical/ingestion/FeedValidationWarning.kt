package com.hartwig.actin.datamodel.clinical.ingestion

data class FeedValidationWarning(val subject: String, val message: String) : Comparable<FeedValidationWarning> {

    override fun compareTo(other: FeedValidationWarning): Int {
        return Comparator.comparing(FeedValidationWarning::subject)
            .thenComparing(FeedValidationWarning::message)
            .compare(this, other)
    }
}