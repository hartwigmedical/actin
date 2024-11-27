package com.hartwig.actin.algo.calendar

import com.hartwig.actin.datamodel.TestPatientFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReferenceDateProviderFactoryTest {

    @Test
    fun `Should create all flavors`() {
        val patient = TestPatientFactory.createMinimalTestWGSPatientRecord()

        val provider1 = ReferenceDateProviderFactory.create(patient, true)
        assertThat(provider1.date()).isNotNull()
        assertThat(provider1.isLive).isFalse()

        val provider2 = ReferenceDateProviderFactory.create(patient, false)
        assertThat(provider2.date()).isNotNull()
        assertThat(provider2.isLive).isTrue()
    }
}