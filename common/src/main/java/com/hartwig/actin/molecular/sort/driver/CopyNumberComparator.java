package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumberDriver;

import org.jetbrains.annotations.NotNull;

public class CopyNumberComparator implements Comparator<CopyNumberDriver> {

    private static final DriverLikelihoodComparator DRIVER_LIKELIHOOD_COMPARATOR = new DriverLikelihoodComparator();

    @Override
    public int compare(@NotNull CopyNumberDriver copyNumberDriver1, @NotNull CopyNumberDriver copyNumberDriver2) {
        int driverCompare =
                DRIVER_LIKELIHOOD_COMPARATOR.compare(copyNumberDriver1.driverLikelihood(), copyNumberDriver2.driverLikelihood());
        if (driverCompare != 0) {
            return driverCompare;
        }

        int geneCompare = copyNumberDriver1.gene().compareTo(copyNumberDriver2.gene());
        if (geneCompare != 0) {
            return geneCompare;
        }

        return copyNumberDriver1.event().compareTo(copyNumberDriver2.event());
    }
}
