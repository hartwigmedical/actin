package com.hartwig.actin.treatment.input.datamodel

import org.junit.Test

class TumorTypeInputTest {
    @Test
    fun canConvertAllTumorTypeInputs() {
        for (category in TumorTypeInput.values()) {
            assertEquals(category, TumorTypeInput.fromString(category.display()))
        }
    }
}