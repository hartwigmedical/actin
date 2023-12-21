package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.createEmpty
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.createExhaustive
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DriverComparatorTest {

    @Test
    fun `Should sort drivers`() {
        val driver1 = create(true, "event 1", DriverLikelihood.HIGH, createEmpty())
        val driver2 = create(true, "event 1", DriverLikelihood.MEDIUM, createEmpty())
        val driver3 = create(true, "event 2", DriverLikelihood.MEDIUM, createExhaustive())
        val driver4 = create(true, "event 2", DriverLikelihood.MEDIUM, createEmpty())
        val driver5 = create(false, "event 1", DriverLikelihood.HIGH, createEmpty())

        val drivers = listOf(driver4, driver5, driver1, driver2, driver3).sortedWith(DriverComparator())

        assertThat(drivers[0]).isEqualTo(driver1)
        assertThat(drivers[1]).isEqualTo(driver2)
        assertThat(drivers[2]).isEqualTo(driver3)
        assertThat(drivers[3]).isEqualTo(driver4)
        assertThat(drivers[4]).isEqualTo(driver5)
    }

    private fun create(
        isReportable: Boolean, event: String, driverLikelihood: DriverLikelihood?,
        evidence: ActionableEvidence
    ): Driver {
        return object : Driver {
            override fun toString(): String {
                return "$isReportable $event $driverLikelihood"
            }

            override val isReportable: Boolean
                get() = isReportable

            override val event: String
                get() = event

            override val driverLikelihood: DriverLikelihood?
                get() = driverLikelihood

            override val evidence: ActionableEvidence
                get() = evidence
        }
    }
}