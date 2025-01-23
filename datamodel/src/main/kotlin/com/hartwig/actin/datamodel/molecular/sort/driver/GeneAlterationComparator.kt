package com.hartwig.actin.datamodel.molecular.sort.driver

import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration

class GeneAlterationComparator : Comparator<GeneAlteration> {

    private val comparator = Comparator.comparing(GeneAlteration::gene)
        .thenComparing({ it.geneRole.toString() }, String::compareTo)
        .thenComparing({ it.proteinEffect.toString() }, String::compareTo)
    
    override fun compare(alteration1: GeneAlteration, alteration2: GeneAlteration): Int {
        return comparator.compare(alteration1, alteration2)
    }
}
