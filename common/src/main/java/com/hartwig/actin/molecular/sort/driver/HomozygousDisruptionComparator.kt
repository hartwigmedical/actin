package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;

import org.jetbrains.annotations.NotNull;

public class HomozygousDisruptionComparator implements Comparator<HomozygousDisruption> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    private static final GeneAlterationComparator GENE_ALTERATION_COMPARATOR = new GeneAlterationComparator();

    @Override
    public int compare(@NotNull HomozygousDisruption homozygousDisruption1, @NotNull HomozygousDisruption homozygousDisruption2) {
        int driverCompare = DRIVER_COMPARATOR.compare(homozygousDisruption1, homozygousDisruption2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        return GENE_ALTERATION_COMPARATOR.compare(homozygousDisruption1, homozygousDisruption2);
    }
}
