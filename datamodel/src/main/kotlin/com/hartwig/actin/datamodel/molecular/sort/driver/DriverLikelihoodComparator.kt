package com.hartwig.actin.datamodel.molecular.sort.driver

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood

class DriverLikelihoodComparator : Comparator<DriverLikelihood?> {

    override fun compare(driverLikelihood1: DriverLikelihood?, driverLikelihood2: DriverLikelihood?): Int {
        if (driverLikelihood1 == null && driverLikelihood2 == null) {
            return 0
        } else if (driverLikelihood1 == null) {
            return 1
        } else if (driverLikelihood2 == null) {
            return -1
        }
        return when (driverLikelihood1) {
            DriverLikelihood.HIGH -> {
                if (driverLikelihood2 == DriverLikelihood.HIGH) 0 else -1
            }

            DriverLikelihood.MEDIUM -> {
                when (driverLikelihood2) {
                    DriverLikelihood.HIGH -> {
                        1
                    }

                    DriverLikelihood.MEDIUM -> {
                        0
                    }

                    else -> {
                        -1
                    }
                }
            }

            DriverLikelihood.LOW -> {
                if (driverLikelihood2 == DriverLikelihood.LOW) 0 else 1
            }
        }
    }
}
