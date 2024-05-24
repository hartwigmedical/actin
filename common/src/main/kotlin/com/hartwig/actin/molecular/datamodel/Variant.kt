package com.hartwig.actin.molecular.datamodel

interface Variant : Driver, GeneAlteration {
    val chromosome: String
    val position: Int
    val ref: String
    val alt: String
    val type: VariantType
    val isHotspot: Boolean
    val canonicalImpact: TranscriptImpact
    val clonalLikelihood: Double
}