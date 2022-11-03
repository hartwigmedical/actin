package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumberDriver;

import org.jetbrains.annotations.NotNull;

public class CopyNumberComparator implements Comparator<CopyNumberDriver> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    @Override
    public int compare(@NotNull CopyNumberDriver copyNumberDriver1, @NotNull CopyNumberDriver copyNumberDriver2) {
        int driverCompare = DRIVER_COMPARATOR.compare(copyNumberDriver1, copyNumberDriver2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        int geneCompare = copyNumberDriver1.gene().compareTo(copyNumberDriver2.gene());
        if (geneCompare != 0) {
            return geneCompare;
        }

        // TODO
        return 0;
    }
}
