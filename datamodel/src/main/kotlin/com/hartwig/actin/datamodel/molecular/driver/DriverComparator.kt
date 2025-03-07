package com.hartwig.actin.datamodel.molecular.driver

class DriverComparator : Comparator<Driver> {

    private val comparator = Comparator.comparing(Driver::isReportable, reverseOrder())
        .thenComparing(Driver::driverLikelihood, DriverLikelihoodComparator())
        .thenComparing(Driver::event)
        .thenComparing(Driver::evidence)

    override fun compare(driver1: Driver, driver2: Driver): Int {
        return comparator.compare(driver1, driver2)
    }
}