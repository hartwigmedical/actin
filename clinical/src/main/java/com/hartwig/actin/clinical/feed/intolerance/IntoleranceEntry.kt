package com.hartwig.actin.clinical.feed.intolerance

import com.hartwig.actin.clinical.feed.FeedEntry
import java.time.LocalDate

data class IntoleranceEntry(
    override val subject: String,
    val assertedDate: LocalDate,
    val category: String,
    val categoryAllergyCategoryDisplay: String,
    val clinicalStatus: String,
    val verificationStatus: String,
    val codeText: String,
    val criticality: String,
    val isSideEffect: String
) : FeedEntry