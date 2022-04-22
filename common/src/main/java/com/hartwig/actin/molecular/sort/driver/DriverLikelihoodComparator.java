package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;

import org.jetbrains.annotations.NotNull;

public class DriverLikelihoodComparator implements Comparator<DriverLikelihood> {

    @Override
    public int compare(@NotNull DriverLikelihood driverLikelihood1, @NotNull DriverLikelihood driverLikelihood2) {
        switch (driverLikelihood1) {
            case HIGH: {
                return  driverLikelihood2 == DriverLikelihood.HIGH ? 0 : -1;
            }
            case MEDIUM: {
                if (driverLikelihood2 == DriverLikelihood.HIGH) {
                    return 1;
                } else if (driverLikelihood2 == DriverLikelihood.MEDIUM) {
                    return 0;
                } else {
                    return -1;
                }
            } case LOW: {
                return driverLikelihood2 == DriverLikelihood.LOW ? 0 : 1;
            } default: {
                throw new IllegalStateException("Cannot compare driverLikelihood: " + driverLikelihood1);
            }
        }
    }
}
