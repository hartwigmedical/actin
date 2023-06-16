package com.hartwig.actin.clinical.feed.vitalfunction

import com.hartwig.actin.clinical.feed.FeedEntry
import java.time.LocalDate

data class VitalFunctionEntry(
    override val subject: String,
    val effectiveDateTime: LocalDate,
    val codeDisplayOriginal: String,
    val componentCodeDisplay: String,
    val quantityUnit: String,
    val quantityValue: Double
) : FeedEntry