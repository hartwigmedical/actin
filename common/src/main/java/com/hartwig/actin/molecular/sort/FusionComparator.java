package com.hartwig.actin.molecular.sort;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;

import org.jetbrains.annotations.NotNull;

public class FusionComparator implements Comparator<Fusion> {

    @Override
    public int compare(@NotNull Fusion fusion1, @NotNull Fusion fusion2) {
        boolean fusion1HasHighDL = fusion1.driverLikelihood() == DriverLikelihood.HIGH;
        boolean fusion2HasHighDL = fusion2.driverLikelihood() == DriverLikelihood.HIGH;

        int driverLikelihoodCompare = Boolean.compare(fusion2HasHighDL, fusion1HasHighDL);
        if (driverLikelihoodCompare != 0) {
            return driverLikelihoodCompare;
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
