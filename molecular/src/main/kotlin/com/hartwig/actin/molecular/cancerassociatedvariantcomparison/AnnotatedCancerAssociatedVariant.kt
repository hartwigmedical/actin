package com.hartwig.actin.molecular.cancerassociatedvariantcomparison

data class AnnotatedCancerAssociatedVariant(
    val gene: String,
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
    val codingImpact: String,
    val proteinImpact: String,
    val isCancerAssociatedVariantOrange: Boolean,
    val isCancerAssociatedVariantServe: Boolean
)
