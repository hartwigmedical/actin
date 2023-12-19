package com.hartwig.actin.molecular.sort.driver

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import org.junit.Assert
import org.junit.Test

class CopyNumberComparatorTest {
    @Test
    fun canSortCopyNumbers() {
        val driver1: CopyNumber = TestCopyNumberFactory.builder().driverLikelihood(DriverLikelihood.HIGH).gene("MYC").build()
        val driver2: CopyNumber = TestCopyNumberFactory.builder().driverLikelihood(DriverLikelihood.MEDIUM).gene("MYC").build()
        val driver3: CopyNumber = TestCopyNumberFactory.builder().driverLikelihood(DriverLikelihood.MEDIUM).gene("NTRK").build()
        val copyNumberDrivers: List<CopyNumber> = Lists.newArrayList(driver2, driver1, driver3)
        copyNumberDrivers.sort(CopyNumberComparator())
        Assert.assertEquals(driver1, copyNumberDrivers[0])
        Assert.assertEquals(driver2, copyNumberDrivers[1])
        Assert.assertEquals(driver3, copyNumberDrivers[2])
    }
}