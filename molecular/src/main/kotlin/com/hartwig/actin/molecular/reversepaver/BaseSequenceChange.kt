package com.hartwig.actin.molecular.reversepaver

data class BaseSequenceChange(
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
)