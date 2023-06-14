package com.hartwig.actin.clinical.feed.surgery

import com.hartwig.actin.clinical.feed.FeedEntryCreator
import com.hartwig.actin.clinical.feed.FeedLine

class SurgeryEntryCreator : FeedEntryCreator<SurgeryEntry> {
    override fun fromLine(line: FeedLine): SurgeryEntry {
        return SurgeryEntry(
            subject = line.trimmed("subject"),
            classDisplay = line.string("class_display"),
            periodStart = line.date("period_start"),
            periodEnd = line.date("period_end"),
            codeCodingDisplayOriginal = line.string("code_coding_display_original"),
            encounterStatus = line.string("encounter_status"),
            procedureStatus = line.string("procedure_status"),
        )
    }

    override fun isValid(line: FeedLine): Boolean {
        return line.string("code_coding_display_original") != BIOPSY_PROCEDURE_DISPLAY
    }

    companion object {
        private const val BIOPSY_PROCEDURE_DISPLAY = "Procedurele sedatie analgesie ANE op OK"
    }
}