package com.hartwig.actin.molecular.sort.driver

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.createEmpty
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.createExhaustive
import org.junit.Assert
import org.junit.Test

class DriverComparatorTest {
    @Test
    fun canSortDrivers() {
        val driver1 = create(true, "event 1", DriverLikelihood.HIGH, createEmpty())
        val driver2 = create(true, "event 1", DriverLikelihood.MEDIUM, createEmpty())
        val driver3 = create(true, "event 2", DriverLikelihood.MEDIUM, createExhaustive())
        val driver4 = create(true, "event 2", DriverLikelihood.MEDIUM, createEmpty())
        val driver5 = create(false, "event 1", DriverLikelihood.HIGH, createEmpty())
        val drivers: List<Driver> = Lists.newArrayList(driver4, driver5, driver1, driver2, driver3)
        drivers.sort(DriverComparator())
        Assert.assertEquals(driver1, drivers[0])
        Assert.assertEquals(driver2, drivers[1])
        Assert.assertEquals(driver3, drivers[2])
        Assert.assertEquals(driver4, drivers[3])
        Assert.assertEquals(driver5, drivers[4])
    }

    companion object {
        private fun create(
            isReportable: Boolean, event: String, driverLikelihood: DriverLikelihood?,
            evidence: ActionableEvidence
        ): Driver {
            return object : Driver {
                override val isReportable: Boolean
                    get() = isReportable

                override fun event(): String {
                    return event
                }

                override fun driverLikelihood(): DriverLikelihood? {
                    return driverLikelihood
                }

                override fun evidence(): ActionableEvidence {
                    return evidence
                }

                override fun toString(): String {
                    return "$isReportable $event $driverLikelihood"
                }
            }
        }
    }
}