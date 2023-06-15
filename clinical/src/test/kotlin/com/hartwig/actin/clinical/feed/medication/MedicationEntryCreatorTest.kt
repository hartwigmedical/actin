package com.hartwig.actin.clinical.feed.medication

import com.hartwig.actin.clinical.feed.medication.MedicationEntryCreator.Companion.isActive
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class MedicationEntryCreatorTest {
    @Test
    fun canInterpretActiveField() {
        Assert.assertFalse(isActive("stopped")!!)
        Assert.assertTrue(isActive("active")!!)
        Assert.assertNull(isActive(Strings.EMPTY))
    }
}