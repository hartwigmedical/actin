package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.datamodel.clinical.provided.JacksonSerializable
import com.hartwig.actin.clinical.feed.emc.FeedEntry
import com.hartwig.actin.clinical.feed.emc.FeedSubjectDeserializer
import com.hartwig.actin.clinical.feed.emc.FeedValidation
import com.hartwig.actin.clinical.feed.emc.FeedValidator
import java.time.LocalDate

@JacksonSerializable
data class QuestionnaireEntry(
    @JsonProperty("subject")
    @JsonDeserialize(using = FeedSubjectDeserializer::class)
    override val subject: String,
    @JsonProperty("authored")
    val authored: LocalDate,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("item_text")
    val itemText: String,
    @JsonProperty("text")
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