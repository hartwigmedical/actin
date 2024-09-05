package com.hartwig.actin.datamodel.molecular.sort.driver

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.createEmptyClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DriverComparatorTest {

    @Test
    fun `Should sort drivers`() {
        val driver1 = create(true, "event 1", DriverLikelihood.HIGH, createEmptyClinicalEvidence())
        val driver2 = create(true, "event 1", DriverLikelihood.MEDIUM, createEmptyClinicalEvidence())
        val driver3 = create(true, "event 2", DriverLikelihood.MEDIUM, createExhaustiveClinicalEvidence())
        val driver4 = create(true, "event 2", DriverLikelihood.MEDIUM, createEmptyClinicalEvidence())
        val driver5 = create(false, "event 1", DriverLikelihood.HIGH, createEmptyClinicalEvidence())

        val drivers = listOf(driver4, driver5, driver1, driver2, driver3).sortedWith(DriverComparator())

        assertThat(drivers[0]).isEqualTo(driver1)
        assertThat(drivers[1]).isEqualTo(driver2)
        assertThat(drivers[2]).isEqualTo(driver3)
        assertThat(drivers[3]).isEqualTo(driver4)
        assertThat(drivers[4]).isEqualTo(driver5)
    }

    private fun create(
        isReportable: Boolean, event: String, driverLikelihood: DriverLikelihood?,
        evidence: ClinicalEvidence
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

            override val evidence: ClinicalEvidence
                get() = evidence
        }
    }
}