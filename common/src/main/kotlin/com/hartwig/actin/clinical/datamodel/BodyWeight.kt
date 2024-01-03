package com.hartwig.actin.clinical.datamodel

import java.time.LocalDate

data class BodyWeight(
    val date: LocalDate,
    val value: Double,
    val unit: String
)
