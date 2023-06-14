package com.hartwig.actin.clinical.feed.patient

import com.hartwig.actin.clinical.feed.FeedEntryCreator
import com.hartwig.actin.clinical.feed.FeedLine
import org.apache.logging.log4j.LogManager

class PatientEntryCreator : FeedEntryCreator<PatientEntry> {
    override fun fromLine(line: FeedLine): PatientEntry {
        val subjectTrimmed = line.trimmed("subject")
        val subjectNormal = line.string("subject")
        if (subjectNormal != subjectTrimmed) {
            LOGGER.warn("Patient ID detected with trailing whitespace: '{}'", subjectNormal)
        }
        return PatientEntry(
            subject = subjectTrimmed,
            birthYear = line.integer("birth_year"),
            gender = line.gender("gender"),
            periodStart = line.date("period_start"),
            periodEnd = line.optionalDate("period_end")
        )
    }

    override fun isValid(line: FeedLine): Boolean {
        return true
    }

    companion object {
        private val LOGGER = LogManager.getLogger(PatientEntryCreator::class.java)
    }
}