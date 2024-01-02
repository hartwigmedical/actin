package com.hartwig.actin.clinical.datamodel

data class ObservedToxicity(
    val name: String,
    val categories: Set<String>,
    val grade: Int?
)
