package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Disruption;

import org.jetbrains.annotations.NotNull;

public class DisruptionComparator implements Comparator<Disruption> {

    private static final DriverLikelihoodComparator DRIVER_LIKELIHOOD_COMPARATOR = new DriverLikelihoodComparator();

    @Override
    public int compare(@NotNull Disruption disruption1, @NotNull Disruption disruption2) {
        int driverCompare = DRIVER_LIKELIHOOD_COMPARATOR.compare(disruption1.driverLikelihood(), disruption2.driverLikelihood());
        if (driverCompare != 0) {
            return driverCompare;
        }

        int homozygousCompare = Boolean.compare(disruption2.isHomozygous(), disruption1.isHomozygous());
        if (homozygousCompare != 0) {
            return homozygousCompare;
        }

        int geneCompare = disruption1.gene().compareTo(disruption2.gene());
        if (geneCompare != 0) {
            return geneCompare;
        }

        return disruption1.details().compareTo(disruption2.details());
    }
}
