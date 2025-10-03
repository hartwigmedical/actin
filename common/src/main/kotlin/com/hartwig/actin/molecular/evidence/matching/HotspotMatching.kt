package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.VariantAnnotation
import com.hartwig.serve.datamodel.molecular.hotspot.VariantHotspot

data class HotspotCoordinates(
    val gene: String,
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String
)

object HotspotMatching {

    fun isMatch(actionableHotspot: ActionableHotspot, variant: Variant): Boolean {
        return actionableHotspot.variants().any { variantHotspot -> isMatch(variantHotspot, variant) }
    }

    fun isMatch(variantHotspot: VariantHotspot, variant: Variant): Boolean {
        return coordinates(variantHotspot) == coordinates(variant)
    }

    fun coordinates(variantHotspot: VariantHotspot): HotspotCoordinates {
        return HotspotCoordinates(
            gene = variantHotspot.gene(),
            chromosome = variantHotspot.chromosome(),
            position = variantHotspot.position(),
            ref = variantHotspot.ref(),
            alt = variantHotspot.alt()
        )
    }

    fun coordinates(variant: Variant): HotspotCoordinates {
        return HotspotCoordinates(
            gene = variant.gene,
            chromosome = variant.chromosome,
            position = variant.position,
            ref = variant.ref,
            alt = variant.alt
        )
    }

    fun coordinates(annotation: VariantAnnotation): HotspotCoordinates {
        return HotspotCoordinates(
            gene = annotation.gene(),
            chromosome = annotation.chromosome(),
            position = annotation.position(),
            ref = annotation.ref(),
            alt = annotation.alt()
        )
    }

    fun coordinates(hotspot: KnownHotspot): HotspotCoordinates {
        return HotspotCoordinates(
            gene = hotspot.gene(),
            chromosome = hotspot.chromosome(),
            position = hotspot.position(),
            ref = hotspot.ref(),
            alt = hotspot.alt()
        )
    }
}
