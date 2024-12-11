package com.hartwig.actin.datamodel.trial

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialSourceTest {

    @Test
    fun `Should return trial source based on description`() {
        assertThat(TrialSource.fromDescription(null)).isEqualTo(null)
        assertThat(TrialSource.fromDescription("")).isEqualTo(null)
        assertThat(TrialSource.fromDescription("Erasmus MC")).isEqualTo(TrialSource.EMC)
        assertThat(TrialSource.fromDescription("NKI-Avl")).isEqualTo(TrialSource.NKI)
        assertThat(TrialSource.fromDescription("Longkankeronderzoek")).isEqualTo(TrialSource.LKO)
        assertThat(TrialSource.fromDescription("")).isEqualTo(null)
    }
}