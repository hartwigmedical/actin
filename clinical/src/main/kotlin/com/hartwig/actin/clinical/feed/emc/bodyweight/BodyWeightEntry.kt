package com.hartwig.actin.clinical.feed.emc.bodyweight

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.datamodel.clinical.provided.JacksonSerializable
import com.hartwig.actin.clinical.feed.emc.EuropeanDecimalDeserializer
import com.hartwig.actin.clinical.feed.emc.FeedEntry
import com.hartwig.actin.clinical.feed.emc.FeedSubjectDeserializer
import com.hartwig.actin.clinical.feed.emc.FeedValidation
import com.hartwig.actin.clinical.feed.emc.FeedValidator
import java.time.LocalDateTime

@JacksonSerializable
data class BodyWeightEntry(
    @JsonProperty("subject")
    @JsonDeserialize(using = FeedSubjectDeserializer::class)
    override val subject: String,

    @JsonProperty("valueQuantity_value")
    @JsonDeserialize(using = EuropeanDecimalDeserializer::class)
    val valueQuantityValue: Double,

    @JsonProperty("valueQuantity_unit")
    val valueQuantityUnit: String,

    @JsonProperty("effectiveDateTime")
    val effectiveDateTime: LocalDateTime
) : FeedEntry

class BodyWeightEntryValidator : FeedValidator<BodyWeightEntry> {
    override fun validate(feed: BodyWeightEntry): FeedValidation {
        return FeedValidation(feed.valueQuantityValue > 0)
    }
}