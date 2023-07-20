package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.AtcModel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

internal object ClinicalFeedValidation {
    fun validate(feed: ClinicalFeed, atcModel: AtcModel) {
        val duplicateSubjects = feed.patientEntries.groupBy { it.subject }.filter { it.value.size > 1 }
        check(duplicateSubjects.isEmpty()) {
            "Duplicate subject(s) found in clinical feed patient entries: " + duplicateSubjects.keys.joinToString(", ")
        }
        val medicationFeedValidation = MedicationFeedValidation(atcModel)
        feed.medicationEntries.map { medicationFeedValidation.validate(it) }.forEach { LOGGER.warn(it) }
    }

    val LOGGER: Logger = LogManager.getLogger(ClinicalFeedValidation::class.java)
}