package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Fusion;

import org.jetbrains.annotations.NotNull;

public class FusionComparator implements Comparator<Fusion> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    @Override
    public int compare(@NotNull Fusion fusion1, @NotNull Fusion fusion2) {
        int driverCompare = DRIVER_COMPARATOR.compare(fusion1, fusion2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        int geneStartCompare = fusion1.geneStart().compareTo(fusion2.geneStart());
        if (geneStartCompare != 0) {
            return geneStartCompare;
        }

        int geneEndCompare = fusion1.geneEnd().compareTo(fusion2.geneEnd());
        if (geneEndCompare != 0) {
            return geneEndCompare;
        }

        // TODO
        return 0;
    }
}
