package com.hartwig.actin.datamodel.clinical

import java.time.LocalDateTime

data class VitalFunction(
    val date: LocalDateTime,
    val category: VitalFunctionCategory,
    val subcategory: String,
    val value: Double,
    val unit: String,
    val valid: Boolean
)
