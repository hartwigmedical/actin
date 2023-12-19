package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration

class GeneAlterationComparator : Comparator<GeneAlteration> {
    override fun compare(alteration1: GeneAlteration, alteration2: GeneAlteration): Int {
        val geneCompare = alteration1.gene().compareTo(alteration2.gene())
        if (geneCompare != 0) {
            return geneCompare
        }
        val geneRoleCompare = alteration1.geneRole().toString().compareTo(alteration2.geneRole().toString())
        return if (geneRoleCompare != 0) {
            geneRoleCompare
        } else alteration1.proteinEffect().toString().compareTo(alteration2.proteinEffect().toString())
    }
}
