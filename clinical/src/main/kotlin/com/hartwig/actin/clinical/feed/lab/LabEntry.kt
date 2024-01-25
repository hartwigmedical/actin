package com.hartwig.actin.clinical.feed.lab

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.clinical.feed.EuropeanDecimalDeserializer
import com.hartwig.actin.clinical.feed.FeedEntry
import com.hartwig.actin.clinical.feed.TsvRow
import java.time.LocalDate

@TsvRow
data class LabEntry(
    @JsonProperty("subject")
    override val subject: String,

    @JsonProperty("code_code_original")
    val codeCodeOriginal: String,

    @JsonProperty("code_display_original")
    val codeDisplayOriginal: String,

    @JsonProperty("valueQuantity_comparator")
    val valueQuantityComparator: String,

    @JsonProperty("valueQuantity_value")
    @JsonDeserialize(using = EuropeanDecimalDeserializer::class)
    val valueQuantityValue: Double,

    @JsonProperty("valueQuantity_unit")
    val valueQuantityUnit: String,

    @JsonProperty("referenceRange_text")
    val referenceRangeText: String,

    @JsonProperty("effectiveDateTime")
    val effectiveDateTime: LocalDate
) : FeedEntry