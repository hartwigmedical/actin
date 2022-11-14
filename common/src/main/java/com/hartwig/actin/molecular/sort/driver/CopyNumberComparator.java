package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumberDriver;

import org.jetbrains.annotations.NotNull;

public class CopyNumberComparator implements Comparator<CopyNumberDriver> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    private static final GeneAlterationComparator GENE_ALTERATION_COMPARATOR = new GeneAlterationComparator();

    @Override
    public int compare(@NotNull CopyNumberDriver copyNumberDriver1, @NotNull CopyNumberDriver copyNumberDriver2) {
        int driverCompare = DRIVER_COMPARATOR.compare(copyNumberDriver1, copyNumberDriver2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        return GENE_ALTERATION_COMPARATOR.compare(copyNumberDriver1, copyNumberDriver2);
    }
}
