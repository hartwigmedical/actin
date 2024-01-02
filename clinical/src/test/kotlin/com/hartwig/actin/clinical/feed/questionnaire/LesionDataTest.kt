package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.feed.questionnaire.LesionData.Companion.fromString
import org.junit.Assert
import org.junit.Test

class LesionDataTest {
    @Test
    fun shouldReturnNullForEmptyResponse() {
        assertResultForInput("", "", null, null)
    }

    @Test
    fun shouldReturnNullForUnknownResponse() {
        assertResultForInput("unknown", "n.v.t.", null, null)
    }

    @Test
    fun shouldReturnTrueForAffirmativeResponseAndAccuratelyReportIfActive() {
        assertResultForInput("YES", "yes", true, true)
        assertResultForInput("YES", "no", true, false)
        assertResultForInput("YES", "unknown ", true, null)
        assertResultForInput("YES", "", true, null)
    }

    @Test
    fun shouldReturnFalseForNegativeResponseAndReportInactiveIfProvided() {
        assertResultForInput("NO", "no", false, false)
        assertResultForInput("NO", "unknown", false, null)
        assertResultForInput("NO", "", false, null)
    }

    private fun assertResultForInput(
        presentInput: String, activeInput: String, present: Boolean?,
        active: Boolean?
    ) {
        val lesionData = fromString("subject", presentInput, activeInput)
        Assert.assertEquals(present, lesionData.curated?.present())
        Assert.assertEquals(active, lesionData.curated?.active())
    }
}