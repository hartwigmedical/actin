package com.hartwig.actin.clinical.feed.vitalfunction

import com.hartwig.actin.clinical.feed.FeedEntry
import java.time.LocalDateTime

data class VitalFunctionEntry(
    override val subject: String,
    val effectiveDateTime: LocalDateTime,
    val codeDisplayOriginal: String,
    val componentCodeDisplay: String,
    val quantityUnit: String,
    val quantityValue: Double
) : FeedEntry