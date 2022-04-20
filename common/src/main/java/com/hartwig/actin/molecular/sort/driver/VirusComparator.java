package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Virus;

import org.jetbrains.annotations.NotNull;

public class VirusComparator implements Comparator<Virus> {

    @Override
    public int compare(@NotNull Virus virus1, @NotNull Virus virus2) {
        boolean virus1HasHighDL = virus1.driverLikelihood() == DriverLikelihood.HIGH;
        boolean virus2HasHighDL = virus2.driverLikelihood() == DriverLikelihood.HIGH;

        int driverLikelihoodCompare = Boolean.compare(virus2HasHighDL, virus1HasHighDL);
        if (driverLikelihoodCompare != 0) {
            return driverLikelihoodCompare;
        }

        int nameCompare = virus1.name().compareTo(virus2.name());
        if (nameCompare != 0) {
            return nameCompare;
        }

        return virus1.event().compareTo(virus2.event());
    }
}
