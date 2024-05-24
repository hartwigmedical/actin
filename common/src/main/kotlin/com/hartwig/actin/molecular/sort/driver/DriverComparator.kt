package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.sort.evidence.ActionableEvidenceComparator

class DriverComparator : Comparator<Driver> {

    private val comparator = Comparator.comparing(Driver::isReportable, reverseOrder())
        .thenComparing(Driver::driverLikelihood, DriverLikelihoodComparator())
        .thenComparing(Driver::event)
        .thenComparing(Driver::evidence, ActionableEvidenceComparator())
    
    override fun compare(driver1: Driver, driver2: Driver): Int {
        return comparator.compare(driver1, driver2)
    }
}
