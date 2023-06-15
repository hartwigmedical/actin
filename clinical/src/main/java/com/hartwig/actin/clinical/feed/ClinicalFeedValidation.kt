package com.hartwig.actin.clinical.feed

internal object ClinicalFeedValidation {
    fun validate(feed: ClinicalFeed) {
        val duplicateSubjects = feed.patientEntries.groupBy { it.subject }.filter { it.value.size > 1 }
        check(duplicateSubjects.isEmpty()) {
            "Duplicate subject(s) found in clinical feed patient entries: " + duplicateSubjects.keys.joinToString(", ")
        }
    }
}