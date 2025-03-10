package com.hartwig.actin.clinical.feed.emc.surgery

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.datamodel.clinical.provided.JacksonSerializable
import com.hartwig.actin.clinical.feed.emc.FeedEntry
import com.hartwig.actin.clinical.feed.emc.FeedSubjectDeserializer
import com.hartwig.actin.clinical.feed.emc.FeedValidation
import com.hartwig.actin.clinical.feed.emc.FeedValidator
import java.time.LocalDate

private const val BIOPSY_PROCEDURE_DISPLAY = "Procedurele sedatie analgesie ANE op OK"

@JacksonSerializable
data class SurgeryEntry(
    @JsonProperty("subject")
    @JsonDeserialize(using = FeedSubjectDeserializer::class)
    override val subject: String,
    @JsonProperty("class_display")
    val classDisplay: String,
    @JsonProperty("period_start")
    val periodStart: LocalDate,
    @JsonProperty("period_end")
    val periodEnd: LocalDate,
    @JsonProperty("code_coding_display_original")
    val codeCodingDisplayOriginal: String,
    @JsonProperty("encounter_status")
    val encounterStatus: String,
    @JsonProperty("procedure_status")
    val procedureStatus: String
) : FeedEntry

class SurgeryEntryFeedValidator : FeedValidator<SurgeryEntry> {
    override fun validate(feed: SurgeryEntry): FeedValidation {
        return FeedValidation(feed.codeCodingDisplayOriginal != BIOPSY_PROCEDURE_DISPLAY)
    }
}