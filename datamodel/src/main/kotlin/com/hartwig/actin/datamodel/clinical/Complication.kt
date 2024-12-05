package com.hartwig.actin.datamodel.clinical

//TODO(make categories property redundant)
data class Complication(
    val name: String,
    val categories: Set<String>,
    val icdCode: String,
    val year: Int?,
    val month: Int?
)
