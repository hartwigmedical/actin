package com.hartwig.actin.clinical.feed.emc.surgery

import com.hartwig.actin.clinical.feed.JacksonSerializable
import com.hartwig.actin.clinical.feed.emc.FeedEntry
import com.hartwig.actin.clinical.feed.emc.FeedValidation
import com.hartwig.actin.clinical.feed.emc.FeedValidator
import java.time.LocalDate

private const val BIOPSY_PROCEDURE_DISPLAY = "Procedurele sedatie analgesie ANE op OK"

@JacksonSerializable
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
    override fun validate(feed: SurgeryEntry): FeedValidation {
        return FeedValidation(feed.codeCodingDisplayOriginal != BIOPSY_PROCEDURE_DISPLAY)
    }
}