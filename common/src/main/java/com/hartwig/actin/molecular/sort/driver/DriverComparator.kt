package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.sort.evidence.ActionableEvidenceComparator
import java.lang.Boolean
import kotlin.Comparator
import kotlin.Int

class DriverComparator : Comparator<Driver> {
    override fun compare(driver1: Driver, driver2: Driver): Int {
        val reportableCompare = Boolean.compare(driver2.isReportable, driver1.isReportable)
        if (reportableCompare != 0) {
            return reportableCompare
        }
        val likelihoodCompare = DRIVER_LIKELIHOOD_COMPARATOR.compare(driver1.driverLikelihood(), driver2.driverLikelihood())
        if (likelihoodCompare != 0) {
            return likelihoodCompare
        }
        val eventCompare = driver1.event().compareTo(driver2.event())
        return if (eventCompare != 0) {
            eventCompare
        } else ACTIONABLE_EVIDENCE_COMPARATOR.compare(driver1.evidence(), driver2.evidence())
    }

    companion object {
        private val DRIVER_LIKELIHOOD_COMPARATOR = DriverLikelihoodComparator()
        private val ACTIONABLE_EVIDENCE_COMPARATOR = ActionableEvidenceComparator()
    }
}
