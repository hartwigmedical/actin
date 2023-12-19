package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.Variant

class VariantComparator : Comparator<Variant> {
    override fun compare(variant1: Variant, variant2: Variant): Int {
        val driverCompare = DRIVER_COMPARATOR.compare(variant1, variant2)
        if (driverCompare != 0) {
            return driverCompare
        }
        val geneAlterationCompare = GENE_ALTERATION_COMPARATOR.compare(variant1, variant2)
        if (geneAlterationCompare != 0) {
            return geneAlterationCompare
        }
        val canonicalProteinImpactCompare =
            variant1.canonicalImpact().hgvsProteinImpact().compareTo(variant2.canonicalImpact().hgvsProteinImpact())
        return if (canonicalProteinImpactCompare != 0) {
            canonicalProteinImpactCompare
        } else variant1.canonicalImpact().hgvsCodingImpact().compareTo(variant2.canonicalImpact().hgvsCodingImpact())
    }

    companion object {
        private val DRIVER_COMPARATOR = DriverComparator()
        private val GENE_ALTERATION_COMPARATOR = GeneAlterationComparator()
    }
}
