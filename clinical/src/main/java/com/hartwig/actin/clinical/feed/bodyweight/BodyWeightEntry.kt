package com.hartwig.actin.clinical.feed.bodyweight

import com.hartwig.actin.clinical.feed.FeedEntry
import java.time.LocalDate

data class BodyWeightEntry(
    override val subject: String,
    val valueQuantityValue: Double,
    val valueQuantityUnit: String,
    val effectiveDateTime: LocalDate
) : FeedEntry