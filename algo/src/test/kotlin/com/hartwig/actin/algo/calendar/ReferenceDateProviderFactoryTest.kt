package com.hartwig.actin.algo.calendar

import com.hartwig.actin.TestDataFactory
import org.junit.Assert
import org.junit.Test

class ReferenceDateProviderFactoryTest {

    @Test
    fun canCreateAllFlavors() {
        val patient = TestDataFactory.createMinimalTestPatientRecord()

        val provider1 = ReferenceDateProviderFactory.create(patient, true)
        Assert.assertNotNull(provider1.date())
        Assert.assertFalse(provider1.isLive)

        val provider2 = ReferenceDateProviderFactory.create(patient, false)
        Assert.assertNotNull(provider2.date())
        Assert.assertTrue(provider2.isLive)
    }
}