package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.LesionData.Companion.fromString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LesionDataTest {

    @Test
    fun `Should return null for empty response`() {
        assertResultForInput("", "", null, null)
    }

    @Test
    fun `Should return null for unknown response`() {
        assertResultForInput("unknown", "n.v.t.", null, null)
    }

    @Test
    fun `Should return true for affirmative response and accurately report if active`() {
        assertResultForInput("YES", "yes", true, true)
        assertResultForInput("YES", "no", true, false)
        assertResultForInput("YES", "unknown ", true, null)
        assertResultForInput("YES", "", true, null)
    }

    @Test
    fun `Should return false for negative response and report inactive if provided`() {
        assertResultForInput("NO", "no", false, false)
        assertResultForInput("NO", "unknown", false, null)
        assertResultForInput("NO", "", false, null)
    }

    private fun assertResultForInput(presentInput: String, activeInput: String, present: Boolean?, active: Boolean?) {
        val lesionData = fromString("subject", presentInput, activeInput)
        assertThat(lesionData.curated?.present()).isEqualTo(present)
        assertThat(lesionData.curated?.active()).isEqualTo(active)
    }
}