package com.hartwig.actin.trial.input.datamodel

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TumorTypeInputTest {

    @Test
    fun `Should convert all tumor type inputs`() {
        for (category in TumorTypeInput.values()) {
            assertThat(TumorTypeInput.fromString(category.display())).isEqualTo(category)
        }
    }
}