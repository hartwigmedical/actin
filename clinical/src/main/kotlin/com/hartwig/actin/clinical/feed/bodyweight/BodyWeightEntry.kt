package com.hartwig.actin.clinical.feed.bodyweight

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.clinical.feed.EuropeanDecimalDeserializer
import com.hartwig.actin.clinical.feed.FeedEntry
import com.hartwig.actin.clinical.feed.FeedValidation
import com.hartwig.actin.clinical.feed.FeedValidator
import com.hartwig.actin.clinical.feed.JacksonSerializable
import java.time.LocalDateTime

@JacksonSerializable
data class BodyWeightEntry(
    @JsonProperty("subject")
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