package com.hartwig.actin.clinical.feed.medication

import com.hartwig.actin.clinical.feed.medication.MedicationEntryCreator.Companion.isActive
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MedicationEntryCreatorTest {
    @Test
    fun shouldInterpretIfMedicationIsActive() {
        assertThat(isActive("stopped")!!).isFalse()
        assertThat(isActive("active")!!).isTrue()
        assertThat(isActive(Strings.EMPTY)).isNull()
    }
}