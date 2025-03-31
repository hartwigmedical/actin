package com.hartwig.actin.clinical.feed.emc.vitalfunction

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.datamodel.clinical.provided.JacksonSerializable
import com.hartwig.actin.clinical.feed.emc.EuropeanDecimalDeserializer
import com.hartwig.actin.clinical.feed.emc.FeedEntry
import com.hartwig.actin.clinical.feed.emc.FeedSubjectDeserializer
import com.hartwig.actin.clinical.feed.emc.FeedValidation
import com.hartwig.actin.datamodel.clinical.ingestion.FeedValidationWarning
import com.hartwig.actin.clinical.feed.emc.FeedValidator
import java.time.LocalDateTime

@JacksonSerializable
data class VitalFunctionEntry(
    @JsonProperty("subject")
    @JsonDeserialize(using = FeedSubjectDeserializer::class)
    override val subject: String,

    @JsonProperty("effectiveDateTime")
    val effectiveDateTime: LocalDateTime,

    @JsonProperty("code_display_original")
    val codeDisplayOriginal: String,

    @JsonProperty("component_code_display")
    val componentCodeDisplay: String,

    @JsonProperty("quantity_unit")
    val quantityUnit: String,

    @JsonProperty("value_quantity")
    @JsonDeserialize(using = EuropeanDecimalDeserializer::class)
    val quantityValue: Double?
) : FeedEntry {
    fun isValid(): Boolean {
        return true
    }
}

class VitalFunctionFeedValidator : FeedValidator<VitalFunctionEntry> {
    override fun validate(feed: VitalFunctionEntry): FeedValidation {
        val emptyCodeDisplayValidation =
            if (feed.codeDisplayOriginal.isEmpty()) listOf(
                FeedValidationWarning(
                    feed.subject,
                    "Empty vital function category"
                )
            ) else emptyList()
        val emptyQuantityValidation =
            if (feed.quantityValue == null) listOf(FeedValidationWarning(feed.subject, "Empty vital function value"))
            else emptyList()
        val noCategoryValidation =
            if (feed.codeDisplayOriginal.isNotEmpty() && VitalFunctionExtraction.toCategory(feed.codeDisplayOriginal) == null) listOf(
                FeedValidationWarning(
                    feed.subject,
                    "Invalid vital function category: ${feed.codeDisplayOriginal}"
                )
            ) else emptyList()

        val warnings = emptyCodeDisplayValidation + emptyQuantityValidation + noCategoryValidation
        return FeedValidation(warnings.isEmpty(), warnings)
    }
}