package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.serve.datamodel.hotspot.VariantHotspot

object HotspotMatching {

    fun isMatch(hotspot: VariantHotspot, variant: Variant): Boolean {
        val geneMatch = hotspot.gene() == variant.gene
        val chromosomeMatch = hotspot.chromosome() == variant.chromosome
        val positionMatch = hotspot.position() == variant.position
        val refMatch = hotspot.ref() == variant.ref
        val altMatch = hotspot.alt() == variant.alt

        return geneMatch && chromosomeMatch && positionMatch && refMatch && altMatch
    }
}
