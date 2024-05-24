package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.wgs.driver.WgsVariant

class VariantComparator : Comparator<WgsVariant> {

    private val comparator = Comparator.comparing<WgsVariant, WgsVariant>({ it }, DriverComparator())
        .thenComparing({ it }, GeneAlterationComparator())
        .thenComparing({ it.canonicalImpact.hgvsProteinImpact }, String::compareTo)
        .thenComparing({ it.canonicalImpact.hgvsCodingImpact }, String::compareTo)
    
    override fun compare(variant1: WgsVariant, variant2: WgsVariant): Int {
        return comparator.compare(variant1, variant2)
    }
}
