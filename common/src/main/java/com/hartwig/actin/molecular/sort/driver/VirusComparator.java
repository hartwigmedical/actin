package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Virus;

import org.jetbrains.annotations.NotNull;

public class VirusComparator implements Comparator<Virus> {

    private static final DriverLikelihoodComparator DRIVER_LIKELIHOOD_COMPARATOR = new DriverLikelihoodComparator();

    @Override
    public int compare(@NotNull Virus virus1, @NotNull Virus virus2) {
        int driverCompare = DRIVER_LIKELIHOOD_COMPARATOR.compare(virus1.driverLikelihood(), virus2.driverLikelihood());
        if (driverCompare != 0) {
            return driverCompare;
        }

        int nameCompare = virus1.name().compareTo(virus2.name());
        if (nameCompare != 0) {
            return nameCompare;
        }

        return virus1.event().compareTo(virus2.event());
    }
}
