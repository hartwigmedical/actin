package com.hartwig.actin.molecular.hotspotcomparison

data class AnnotatedHotspot(
    val gene: String,
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
    val codingImpact: String,
    val proteinImpact: String,
    val isHotspotOrange: Boolean,
    val isHotspotServe: Boolean
)
