package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CopyNumberComparatorTest {

    @Test
    fun `Should sort copy numbers`() {
        val driver1 = TestCopyNumberFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.HIGH, gene = "MYC")
        val driver2 = TestCopyNumberFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.MEDIUM, gene = "MYC")
        val driver3 = TestCopyNumberFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.MEDIUM, gene = "NTRK")

        val copyNumberDrivers = listOf(driver2, driver1, driver3).sortedWith(CopyNumberComparator())

        assertThat(copyNumberDrivers[0]).isEqualTo(driver1)
        assertThat(copyNumberDrivers[1]).isEqualTo(driver2)
        assertThat(copyNumberDrivers[2]).isEqualTo(driver3)
    }
}