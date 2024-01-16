package com.hartwig.actin.clinical.feed.vitalfunction

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.clinical.feed.EuropeanDecimalDeserializer
import com.hartwig.actin.clinical.feed.FeedEntry
import com.hartwig.actin.clinical.feed.FeedValidator
import com.hartwig.actin.clinical.feed.TsvRow
import java.time.LocalDate

@TsvRow
data class VitalFunctionEntry(
    @JsonProperty("subject")
    override val subject: String,

    @JsonProperty("effectiveDateTime")
    val effectiveDateTime: LocalDate,

    @JsonProperty("code_display_original")
    val codeDisplayOriginal: String,

    @JsonProperty("component_code_display")
    val componentCodeDisplay: String,

    @JsonProperty("quantity_unit")
    val quantityUnit: String,

    @JsonProperty("value_quantity")
    @JsonDeserialize(using = EuropeanDecimalDeserializer::class)
    val quantityValue: Double?
) : FeedEntry

class VitalFunctionFeedValidator : FeedValidator<VitalFunctionEntry> {
    override fun validate(feed: VitalFunctionEntry): Boolean {
        // In vital function data there can be entries with no or NULL value.
        // They likely should be filtered prior to being ingested in ACTIN.
        return feed.codeDisplayOriginal.isNotEmpty() && VitalFunctionExtraction.toCategory(feed.codeDisplayOriginal) != null
                && feed.quantityValue != null
    }
}