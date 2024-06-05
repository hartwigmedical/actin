package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.sort.driver.VariantComparator

interface Variant : Driver, GeneAlteration, Comparable<Variant> {
    val chromosome: String
    val position: Int
    val ref: String
    val alt: String
    val type: VariantType
    val isHotspot: Boolean
    val canonicalImpact: TranscriptImpact
    val clonalLikelihood: Double?

    override fun compareTo(other: Variant): Int {
        return VariantComparator().compare(this, other)
    }
}