package com.hartwig.actin.clinical.feed.patient

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.OptBoolean
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.feed.FeedEntry
import com.hartwig.actin.clinical.feed.TsvRow
import java.time.LocalDate

@TsvRow
data class PatientEntry(
    override val subject: String,
    val birthYear: Int,
    val gender: Gender,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS") val periodStart: LocalDate,
    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd HH:mm:ss.SSS",
        lenient = OptBoolean.TRUE
    ) val periodEnd: LocalDate?
) : FeedEntry