package com.hartwig.actin.clinical.datamodel

data class Complication(
    val name: String,
    val categories: Set<String>,
    val year: Int?,
    val month: Int?
)
