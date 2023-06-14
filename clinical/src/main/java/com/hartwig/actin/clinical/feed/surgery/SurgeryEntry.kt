package com.hartwig.actin.clinical.feed.surgery

import com.hartwig.actin.clinical.feed.FeedEntry
import java.time.LocalDate

data class SurgeryEntry(
    override val subject: String,
    val classDisplay: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val codeCodingDisplayOriginal: String,
    val encounterStatus: String,
    val procedureStatus: String
) : FeedEntry