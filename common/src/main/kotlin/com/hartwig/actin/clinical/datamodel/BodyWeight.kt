package com.hartwig.actin.clinical.datamodel

import java.time.LocalDateTime

data class BodyWeight(
    val date: LocalDateTime,
    val value: Double,
    val unit: String,
    val valid: Boolean
)
