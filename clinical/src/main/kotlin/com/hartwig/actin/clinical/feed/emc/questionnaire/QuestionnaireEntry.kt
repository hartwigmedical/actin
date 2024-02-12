package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.feed.JacksonSerializable
import com.hartwig.actin.clinical.feed.emc.FeedEntry
import com.hartwig.actin.clinical.feed.emc.FeedValidation
import com.hartwig.actin.clinical.feed.emc.FeedValidator
import java.time.LocalDate

@JacksonSerializable
data class QuestionnaireEntry(
    override val subject: String,
    val authored: LocalDate,
    val description: String,
    val itemText: String,
    val text: String
) : FeedEntry

class QuestionnaireEntryFeedValidator : FeedValidator<QuestionnaireEntry> {
    override fun validate(feed: QuestionnaireEntry): FeedValidation {
        return FeedValidation(QUESTIONNAIRE_DESCRIPTIONS.contains(feed.description))
    }

    companion object {
        private val QUESTIONNAIRE_DESCRIPTIONS = setOf("INT Consult", "consultation")
    }
}