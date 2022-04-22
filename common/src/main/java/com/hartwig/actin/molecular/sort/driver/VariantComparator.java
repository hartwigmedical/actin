package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;

public class VariantComparator implements Comparator<Variant> {

    private static final DriverLikelihoodComparator DRIVER_LIKELIHOOD_COMPARATOR = new DriverLikelihoodComparator();

    @Override
    public int compare(@NotNull Variant variant1, @NotNull Variant variant2) {
        int driverCompare = DRIVER_LIKELIHOOD_COMPARATOR.compare(variant1.driverLikelihood(), variant2.driverLikelihood());
        if (driverCompare != 0) {
            return driverCompare;
        }

        int geneCompare = variant1.gene().compareTo(variant2.gene());
        if (geneCompare != 0) {
            return geneCompare;
        }

        return variant1.event().compareTo(variant2.event());
    }
}
