package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.hmftools.datamodel.driver.DriverInterpretation
import com.hartwig.hmftools.datamodel.finding.Driver

object MappingUtil {

    internal fun determineDriverLikelihood(driver: Driver): DriverLikelihood? {
        return when (driver.driverInterpretation()) {
            DriverInterpretation.HIGH -> DriverLikelihood.HIGH

            DriverInterpretation.MEDIUM -> DriverLikelihood.MEDIUM

            DriverInterpretation.LOW -> DriverLikelihood.LOW

            DriverInterpretation.UNKNOWN -> null
        }
    }

    internal fun <T : Driver> includedInGeneFilter(driver: T, geneFilter: GeneFilter, inclusionFilter: (T) -> Boolean = {true}) : Boolean {
        val included = driver.genes().all{ geneFilter.include(it) }
        if (!included && driver.isReported && inclusionFilter.invoke(driver)) {
            throw IllegalStateException(
                "Filtered a reported driver event '${driver.event()}' through gene filtering."
                        + " Please make sure all genes for event '${driver.genes()}' are configured as a known genes."
            )
        }
        return true
    }
}