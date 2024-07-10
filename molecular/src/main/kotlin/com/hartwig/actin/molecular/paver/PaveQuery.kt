package com.hartwig.actin.molecular.paver

data class PaveQuery(
    val id: String,
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
    val transcript: String? = null,
)

