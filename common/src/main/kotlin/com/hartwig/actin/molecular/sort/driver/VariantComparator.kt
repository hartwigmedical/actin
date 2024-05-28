package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.hmf.driver.ExhaustiveVariant

class VariantComparator : Comparator<ExhaustiveVariant> {

    private val comparator = Comparator.comparing<ExhaustiveVariant, ExhaustiveVariant>({ it }, DriverComparator())
        .thenComparing({ it }, GeneAlterationComparator())
        .thenComparing({ it.canonicalImpact.hgvsProteinImpact }, String::compareTo)
        .thenComparing({ it.canonicalImpact.hgvsCodingImpact }, String::compareTo)
    
    override fun compare(variant1: ExhaustiveVariant, variant2: ExhaustiveVariant): Int {
        return comparator.compare(variant1, variant2)
    }
}
