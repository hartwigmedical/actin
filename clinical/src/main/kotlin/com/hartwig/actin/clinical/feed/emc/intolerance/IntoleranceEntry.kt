package com.hartwig.actin.clinical.feed.emc.intolerance

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.clinical.feed.JacksonSerializable
import com.hartwig.actin.clinical.feed.emc.FeedEntry
import com.hartwig.actin.clinical.feed.emc.FeedSubjectDeserializer
import java.time.LocalDate

@JacksonSerializable
data class IntoleranceEntry(
    @JsonProperty("subject")
    @JsonDeserialize(using = FeedSubjectDeserializer::class)
    override val subject: String,
    @JsonProperty("assertedDate")
    val assertedDate: LocalDate,
    @JsonProperty("category")
    val category: String,
    @JsonProperty("category_allergyCategory_display")
    val categoryAllergyCategoryDisplay: String,
    @JsonProperty("clinicalStatus")
    val clinicalStatus: String,
    @JsonProperty("verificationStatus")
    val verificationStatus: String,
    @JsonProperty("code_text")
    val codeText: String,
    @JsonProperty("criticality")
    val criticality: String,
    @JsonProperty("isSideEffect")
    val isSideEffect: String
) : FeedEntry