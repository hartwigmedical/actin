package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;

import org.jetbrains.annotations.NotNull;

public class CopyNumberComparator implements Comparator<CopyNumber> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    private static final GeneAlterationComparator GENE_ALTERATION_COMPARATOR = new GeneAlterationComparator();

    @Override
    public int compare(@NotNull CopyNumber copyNumber1, @NotNull CopyNumber copyNumber2) {
        int driverCompare = DRIVER_COMPARATOR.compare(copyNumber1, copyNumber2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        return GENE_ALTERATION_COMPARATOR.compare(copyNumber1, copyNumber2);
    }
}
