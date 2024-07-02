package com.hartwig.actin.molecular.datamodel

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DriverLikelihoodTest {
    @Test
    fun `Should determine driver likelihood for all purple driver likelihoods`() {
        assertThat(DriverLikelihood.from(null)).isNull()
        assertThat(DriverLikelihood.from(1.0)).isEqualTo(DriverLikelihood.HIGH)
        assertThat(DriverLikelihood.from(0.8)).isEqualTo(DriverLikelihood.HIGH)
        assertThat(DriverLikelihood.from(0.5)).isEqualTo(DriverLikelihood.MEDIUM)
        assertThat(DriverLikelihood.from(0.2)).isEqualTo(DriverLikelihood.MEDIUM)
        assertThat(DriverLikelihood.from(0.0)).isEqualTo(DriverLikelihood.LOW)
    }
}