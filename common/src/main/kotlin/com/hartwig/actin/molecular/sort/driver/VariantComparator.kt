package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.Variant

class VariantComparator : Comparator<Variant> {

    private val comparator = Comparator.comparing<Variant, Variant>({ it }, DriverComparator())
        .thenComparing({ it }, GeneAlterationComparator())
        .thenComparing({ it.canonicalImpact.hgvsProteinImpact }, String::compareTo)
        .thenComparing({ it.canonicalImpact.hgvsCodingImpact }, String::compareTo)
    
    override fun compare(variant1: Variant, variant2: Variant): Int {
        return comparator.compare(variant1, variant2)
    }
}
