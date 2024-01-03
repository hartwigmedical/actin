package com.hartwig.actin.clinical.datamodel

import java.time.LocalDate

data class VitalFunction(
    val date: LocalDate,
    val category: VitalFunctionCategory,
    val subcategory: String,
    val value: Double,
    val unit: String
)
