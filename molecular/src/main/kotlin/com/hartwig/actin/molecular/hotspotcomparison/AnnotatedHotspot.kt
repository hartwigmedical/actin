package com.hartwig.actin.molecular.hotspotcomparison

import com.hartwig.actin.datamodel.molecular.driver.VariantType

data class AnnotatedHotspot(
    val gene: String,
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
    val type: VariantType,
    val codingImpact: String,
    val proteinImpact: String,
    val isHotspotOrange: Boolean,
    val isHotspotServe: Boolean
)
