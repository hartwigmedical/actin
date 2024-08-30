package com.hartwig.actin.datamodel.clinical

import java.time.LocalDateTime

data class BodyHeight(
    val date: LocalDateTime,
    val value: Double,
    val unit: String,
    val valid: Boolean
)