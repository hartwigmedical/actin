package com.hartwig.actin.clinical.feed.surgery

import com.hartwig.actin.clinical.feed.FeedEntry
import com.hartwig.actin.clinical.feed.FeedValidator
import com.hartwig.actin.clinical.feed.TsvRow
import java.time.LocalDate

private const val BIOPSY_PROCEDURE_DISPLAY = "Procedurele sedatie analgesie ANE op OK"

@TsvRow
data class SurgeryEntry(
    override val subject: String,
    val classDisplay: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val codeCodingDisplayOriginal: String,
    val encounterStatus: String,
    val procedureStatus: String
) : FeedEntry

class SurgeryEntryFeedValidator : FeedValidator<SurgeryEntry> {
    override fun validate(feed: SurgeryEntry): Boolean {
        return feed.codeCodingDisplayOriginal != BIOPSY_PROCEDURE_DISPLAY
    }
}