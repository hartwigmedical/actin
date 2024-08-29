package com.hartwig.actin.clinical.feed.emc.patient

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.clinical.feed.JacksonSerializable
import com.hartwig.actin.clinical.feed.emc.FeedEntry
import com.hartwig.actin.clinical.feed.emc.FeedSubjectDeserializer
import com.hartwig.actin.datamodel.clinical.Gender
import java.time.LocalDate

@JacksonSerializable
data class PatientEntry(
    @JsonProperty("subject")
    @JsonDeserialize(using = FeedSubjectDeserializer::class)
    override val subject: String,
    @JsonProperty("birth_year")
    val birthYear: Int,
    @JsonProperty("gender")
    val gender: Gender,
    @JsonProperty("period_start")
    val periodStart: LocalDate,
    @JsonProperty("period_end")
    val periodEnd: LocalDate?
) : FeedEntry