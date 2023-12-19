package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;

import org.jetbrains.annotations.Nullable;

public class DriverLikelihoodComparator implements Comparator<DriverLikelihood> {

    @Override
    public int compare(@Nullable DriverLikelihood driverLikelihood1, @Nullable DriverLikelihood driverLikelihood2) {
        if (driverLikelihood1 == null && driverLikelihood2 == null) {
            return 0;
        } else if (driverLikelihood1 == null) {
            return 1;
        } else if (driverLikelihood2 == null) {
            return -1;
        }

        switch (driverLikelihood1) {
            case HIGH: {
                return driverLikelihood2 == DriverLikelihood.HIGH ? 0 : -1;
            }
            case MEDIUM: {
                if (driverLikelihood2 == DriverLikelihood.HIGH) {
                    return 1;
                } else if (driverLikelihood2 == DriverLikelihood.MEDIUM) {
                    return 0;
                } else {
                    return -1;
                }
            }
            case LOW: {
                return driverLikelihood2 == DriverLikelihood.LOW ? 0 : 1;
            }
            default: {
                throw new IllegalStateException("Cannot interpret driver Likelihood: " + driverLikelihood1);
            }
        }
    }
}
