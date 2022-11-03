package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Driver;

import org.jetbrains.annotations.NotNull;

public class DriverComparator implements Comparator<Driver> {

    private static final DriverLikelihoodComparator DRIVER_LIKELIHOOD_COMPARATOR = new DriverLikelihoodComparator();

    @Override
    public int compare(@NotNull Driver driver1, @NotNull  Driver driver2) {
        int likelihoodCompare = DRIVER_LIKELIHOOD_COMPARATOR.compare(driver1.driverLikelihood(), driver2.driverLikelihood());
        if (likelihoodCompare != 0) {
            return likelihoodCompare;
        }

        // TODO Expand?
        return Integer.compare(driver1.evidence().approvedTreatments().size(), driver2.evidence().approvedTreatments().size());
    }
}
