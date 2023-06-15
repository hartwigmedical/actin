package com.hartwig.actin.clinical.feed.lab

import com.hartwig.actin.clinical.feed.FeedEntry
import java.time.LocalDate

data class LabEntry(
    override val subject: String,
    val codeCodeOriginal: String,
    val codeDisplayOriginal: String,
    val valueQuantityComparator: String,
    val valueQuantityValue: Double,
    val valueQuantityUnit: String,
    val referenceRangeText: String,
    val effectiveDateTime: LocalDate
) : FeedEntry