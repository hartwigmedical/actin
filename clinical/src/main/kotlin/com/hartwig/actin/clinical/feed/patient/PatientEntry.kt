package com.hartwig.actin.clinical.feed.patient

import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.feed.FeedEntry
import com.hartwig.actin.clinical.feed.JacksonSerializable
import java.time.LocalDate

@JacksonSerializable
data class PatientEntry(
    override val subject: String,
    val birthYear: Int,
    val gender: Gender,
    val periodStart: LocalDate,
    val periodEnd: LocalDate?
) : FeedEntry