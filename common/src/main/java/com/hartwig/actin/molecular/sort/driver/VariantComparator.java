package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;

public class VariantComparator implements Comparator<Variant> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    @Override
    public int compare(@NotNull Variant variant1, @NotNull Variant variant2) {
        int driverCompare = DRIVER_COMPARATOR.compare(variant1, variant2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        int geneCompare = variant1.gene().compareTo(variant2.gene());
        if (geneCompare != 0) {
            return geneCompare;
        }

        // TODO
        return 0;
    }
}
