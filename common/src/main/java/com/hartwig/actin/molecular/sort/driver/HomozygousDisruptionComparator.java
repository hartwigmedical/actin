package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;

import org.jetbrains.annotations.NotNull;

public class HomozygousDisruptionComparator implements Comparator<HomozygousDisruption> {

    private static final DriverLikelihoodComparator DRIVER_LIKELIHOOD_COMPARATOR = new DriverLikelihoodComparator();

    @Override
    public int compare(@NotNull HomozygousDisruption homozygousDisruption1, @NotNull HomozygousDisruption homozygousDisruption2) {
        int driverCompare =
                DRIVER_LIKELIHOOD_COMPARATOR.compare(homozygousDisruption1.driverLikelihood(), homozygousDisruption2.driverLikelihood());
        if (driverCompare != 0) {
            return driverCompare;
        }

        return homozygousDisruption1.gene().compareTo(homozygousDisruption2.gene());
    }
}
