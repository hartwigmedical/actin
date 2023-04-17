package com.hartwig.actin.soc.calendar

import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.soc.calendar.ReferenceDateProviderFactory.create
import org.junit.Assert
import org.junit.Test

class ReferenceDateProviderFactoryTest {
    @Test
    fun canCreateAllFlavors() {
        val clinical = TestClinicalFactory.createMinimalTestClinicalRecord()
        val provider1 = create(clinical, true)
        Assert.assertNotNull(provider1.date())
        Assert.assertFalse(provider1.isLive)
        val provider2 = create(clinical, false)
        Assert.assertNotNull(provider2.date())
        Assert.assertTrue(provider2.isLive)
    }
}