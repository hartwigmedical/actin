package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.VariantHotspot

object HotspotMatching {

    fun isMatch(actionableHotspot: ActionableHotspot, variant: Variant): Boolean {
        return actionableHotspot.variants().any { variantHotspot -> isMatch(variantHotspot, variant) }
    }

    fun isMatch(variantHotspot: VariantHotspot, variant: Variant): Boolean {
        val geneMatch = variantHotspot.gene() == variant.gene
        val chromosomeMatch = variantHotspot.chromosome() == variant.chromosome
        val positionMatch = variantHotspot.position() == variant.position
        val refMatch = variantHotspot.ref() == variant.ref
        val altMatch = variantHotspot.alt() == variant.alt

        return geneMatch && chromosomeMatch && positionMatch && refMatch && altMatch
    }
}
