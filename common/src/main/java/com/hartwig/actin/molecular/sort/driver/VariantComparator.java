package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;

public class VariantComparator implements Comparator<Variant> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    private static final GeneAlterationComparator GENE_ALTERATION_COMPARATOR = new GeneAlterationComparator();

    @Override
    public int compare(@NotNull Variant variant1, @NotNull Variant variant2) {
        int driverCompare = DRIVER_COMPARATOR.compare(variant1, variant2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        int geneAlterationCompare = GENE_ALTERATION_COMPARATOR.compare(variant1, variant2);
        if (geneAlterationCompare != 0) {
            return geneAlterationCompare;
        }

        int canonicalProteinImpactCompare =
                variant1.canonicalImpact().hgvsProteinImpact().compareTo(variant2.canonicalImpact().hgvsProteinImpact());
        if (canonicalProteinImpactCompare != 0) {
            return canonicalProteinImpactCompare;
        }

        return variant1.canonicalImpact().hgvsCodingImpact().compareTo(variant2.canonicalImpact().hgvsCodingImpact());
    }
}
