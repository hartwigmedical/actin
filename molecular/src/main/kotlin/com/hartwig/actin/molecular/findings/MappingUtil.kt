package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.hmftools.datamodel.driver.DriverInterpretation
import com.hartwig.hmftools.datamodel.finding.Driver
import com.hartwig.hmftools.datamodel.finding.ReportedStatus

object MappingUtil {

    internal fun determineReported(driver: Driver): Boolean {
        return driver.reportedStatus() == ReportedStatus.REPORTED
    }

    internal fun determineDriverLikelihood(driver: Driver): DriverLikelihood? {
        return when (driver.driverInterpretation()) {
            DriverInterpretation.HIGH -> DriverLikelihood.HIGH

            DriverInterpretation.MEDIUM -> DriverLikelihood.MEDIUM

            DriverInterpretation.LOW -> DriverLikelihood.LOW

            DriverInterpretation.UNKNOWN -> null
        }
    }
}