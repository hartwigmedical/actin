package com.hartwig.actin.clinical.feed.emc

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

internal object ClinicalFeedValidation {
    fun validate(feed: EmcClinicalFeed) {
        val duplicateSubjects = feed.patientEntries.groupBy { it.subject }.filter { it.value.size > 1 }
        check(duplicateSubjects.isEmpty()) {
            "Duplicate subject(s) found in clinical feed patient entries: " + duplicateSubjects.keys.joinToString(", ")
        }
    }

    val LOGGER: Logger = LogManager.getLogger(ClinicalFeedValidation::class.java)
}