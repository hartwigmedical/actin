package com.hartwig.actin.clinical.feed.digitalfile

import com.fasterxml.jackson.annotation.JsonProperty
import com.hartwig.actin.clinical.feed.FeedEntry
import com.hartwig.actin.clinical.feed.JacksonSerializable
import java.time.LocalDate

@JacksonSerializable
data class DigitalFileEntry(
    @JsonProperty("subject")
    override val subject: String,
    @JsonProperty("authored")
    val authored: LocalDate,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("item_text")
    val itemText: String,
    @JsonProperty("item_answer_value_valueString")
    val itemAnswerValueValueString: String
) : FeedEntry {

    fun isBloodTransfusionEntry(): Boolean {
        return description == BLOOD_TRANSFUSION_DESCRIPTION
    }

    fun isToxicityEntry(): Boolean {
        return description == TOXICITY_DESCRIPTION
    }

    companion object {
        private const val BLOOD_TRANSFUSION_DESCRIPTION = "Aanvraag bloedproducten_test"
        private const val TOXICITY_DESCRIPTION = "ONC Kuuroverzicht"
    }
}