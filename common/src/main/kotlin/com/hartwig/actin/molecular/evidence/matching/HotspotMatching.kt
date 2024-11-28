package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.hotspot.VariantHotspot
import com.hartwig.serve.datamodel.trial.ActionableTrial

object HotspotMatching {

    fun isMatch(hotspot: VariantHotspot, variant: VariantMatchCriteria): Boolean {
        val geneMatch = hotspot.gene() == variant.gene
        val chromosomeMatch = hotspot.chromosome() == variant.chromosome
        val positionMatch = hotspot.position() == variant.position
        val refMatch = hotspot.ref() == variant.ref
        val altMatch = hotspot.alt() == variant.alt

        return geneMatch && chromosomeMatch && positionMatch && refMatch && altMatch
    }
}
