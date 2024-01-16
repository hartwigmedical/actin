package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.feed.FeedEntry
import com.hartwig.actin.clinical.feed.TsvRow
import java.time.LocalDate

@TsvRow
data class QuestionnaireEntry(
    override val subject: String,
    val authored: LocalDate,
    val description: String,
    val itemText: String,
    val text: String
) : FeedEntry