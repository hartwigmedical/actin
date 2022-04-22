package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Fusion;

import org.jetbrains.annotations.NotNull;

public class FusionComparator implements Comparator<Fusion> {

    private static final DriverLikelihoodComparator DRIVER_LIKELIHOOD_COMPARATOR = new DriverLikelihoodComparator();

    @Override
    public int compare(@NotNull Fusion fusion1, @NotNull Fusion fusion2) {
        int driverCompare = DRIVER_LIKELIHOOD_COMPARATOR.compare(fusion1.driverLikelihood(), fusion2.driverLikelihood());
        if (driverCompare != 0) {
            return driverCompare;
        }

        int fiveGeneCompare = fusion1.fiveGene().compareTo(fusion2.fiveGene());
        if (fiveGeneCompare != 0) {
            return fiveGeneCompare;
        }

        int threeGeneCompare = fusion1.threeGene().compareTo(fusion2.threeGene());
        if (threeGeneCompare != 0) {
            return threeGeneCompare;
        }

        return fusion1.details().compareTo(fusion2.details());
    }
}
