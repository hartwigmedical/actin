package com.hartwig.actin.datamodel.clinical

data class Complication(
    val name: String,
    val categories: Set<String>,
    val year: Int?,
    val month: Int?
)
