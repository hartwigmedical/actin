package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DriverLikelihoodComparatorTest {

    @Test
    fun `Should sort driver likelihoods`() {
        val high = DriverLikelihood.HIGH
        val medium = DriverLikelihood.MEDIUM
        val low = DriverLikelihood.LOW
        val nothing: DriverLikelihood? = null
        val driverLikelihoods = listOf(medium, low, high, nothing).sortedWith(DriverLikelihoodComparator())

        assertThat(driverLikelihoods[0]).isEqualTo(high)
        assertThat(driverLikelihoods[1]).isEqualTo(medium)
        assertThat(driverLikelihoods[2]).isEqualTo(low)
        assertThat(driverLikelihoods[3]).isEqualTo(nothing)
    }
}