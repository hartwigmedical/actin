package com.hartwig.actin.molecular.sort;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;

public class VariantComparator implements Comparator<Variant> {

    @Override
    public int compare(@NotNull Variant variant1, @NotNull Variant variant2) {
        int driverCompare = Double.compare(variant2.driverLikelihood(), variant1.driverLikelihood());
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
