package com.hartwig.actin.molecular.sort.driver

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import org.junit.Assert
import org.junit.Test

class DriverLikelihoodComparatorTest {
    @Test
    fun canSortDriverLikelihoods() {
        val high = DriverLikelihood.HIGH
        val medium = DriverLikelihood.MEDIUM
        val low = DriverLikelihood.LOW
        val nothing: DriverLikelihood? = null
        val driverLikelihoods: List<DriverLikelihood?> = Lists.newArrayList(medium, low, high, nothing)
        driverLikelihoods.sort(DriverLikelihoodComparator())
        Assert.assertEquals(high, driverLikelihoods[0])
        Assert.assertEquals(medium, driverLikelihoods[1])
        Assert.assertEquals(low, driverLikelihoods[2])
        Assert.assertEquals(nothing, driverLikelihoods[3])
    }
}