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

        int geneCompare = disruption1.gene().compareTo(disruption2.gene());
        if (geneCompare != 0) {
            return geneCompare;
        }

        int typeCompare = disruption1.type().compareTo(disruption2.type());
        if (typeCompare != 0) {
            return typeCompare;
        }

        int junctionCompare = Double.compare(disruption1.junctionCopyNumber(), disruption2.junctionCopyNumber());
        if (junctionCompare != 0) {
            return junctionCompare;
        }

        int undisruptedCompare = Double.compare(disruption1.undisruptedCopyNumber(), disruption2.undisruptedCopyNumber());
        if (undisruptedCompare != 0) {
            return undisruptedCompare;
        }

        return disruption1.range().compareTo(disruption2.range());
    }
}
