package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.ImmutablePriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.PD_LABEL
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressiveDiseaseFunctionsTest {
    @Test
    fun shouldReturnTrueWhenPDAndStopReasonIsNull() {
        assertEquals(true, treatmentResultedInPDOption(treatment(null, PD_LABEL)))
    }

    @Test
    fun shouldReturnTrueWhenPDAndBestResponseIsNull() {
        assertEquals(true, treatmentResultedInPDOption(treatment(PD_LABEL, null)))
    }

    @Test
    fun shouldBeEmptyWhenNotPDAndStopReasonIsNull() {
        assertNull(treatmentResultedInPDOption(treatment(null, "other")))
    }

    @Test
    fun shouldBeEmptyWhenNotPDAndBestResponseIsNull() {
        assertNull(treatmentResultedInPDOption(treatment("other", null)))
    }

    @Test
    fun shouldReturnTrueWhenStopReasonIsPD() {
        assertEquals(true, treatmentResultedInPDOption(treatment(PD_LABEL, "other")))
    }

    @Test
    fun shouldReturnTrueWhenBestResponseIsPD() {
        assertEquals(true, treatmentResultedInPDOption(treatment("other", PD_LABEL)))
    }

    @Test
    fun shouldReturnFalseWhenStopReasonAndBestResponseAreKnownAndNotPD() {
        assertEquals(false, treatmentResultedInPDOption(treatment("other", "something else")))
    }

    companion object {
        private fun treatment(stopReason: String?, bestResponse: String?): PriorTumorTreatment {
            return ImmutablePriorTumorTreatment.builder()
                .name("test treatment")
                .isSystemic(true)
                .stopReason(stopReason)
                .bestResponse(bestResponse)
                .build()
        }
    }
}