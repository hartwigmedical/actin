package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.lastSystemicTreatment
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.maxSystemicTreatments
import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.minSystemicTreatments
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SystemicTreatmentAnalyserTest {
    @Test
    fun canCountSystemicTreatments() {
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEquals(0, minSystemicTreatments(treatments).toLong())
        assertEquals(0, maxSystemicTreatments(treatments).toLong())

        // Add one systemic treatment.
        treatments.add(TreatmentTestFactory.builder().name("treatment A").startYear(2022).startMonth(5).isSystemic(true).build())
        assertEquals(1, minSystemicTreatments(treatments).toLong())
        assertEquals(1, maxSystemicTreatments(treatments).toLong())

        // Add one non-systemic treatment prior
        treatments.add(TreatmentTestFactory.builder().name("treatment B").startYear(2022).startMonth(2).isSystemic(false).build())
        assertEquals(1, minSystemicTreatments(treatments).toLong())
        assertEquals(1, maxSystemicTreatments(treatments).toLong())

        // Add another systemic treatment prior
        treatments.add(TreatmentTestFactory.builder().name("treatment A").startYear(2021).startMonth(10).isSystemic(true).build())
        assertEquals(2, minSystemicTreatments(treatments).toLong())
        assertEquals(2, maxSystemicTreatments(treatments).toLong())

        // Add another systemic treatment prior
        treatments.add(TreatmentTestFactory.builder().name("treatment A").startYear(2021).startMonth(5).isSystemic(true).build())
        assertEquals(2, minSystemicTreatments(treatments).toLong())
        assertEquals(3, maxSystemicTreatments(treatments).toLong())

        // Add another systemic treatment without date
        treatments.add(TreatmentTestFactory.builder().name("treatment A").isSystemic(true).build())
        assertEquals(2, minSystemicTreatments(treatments).toLong())
        assertEquals(4, maxSystemicTreatments(treatments).toLong())

        // Add another systemic treatment with just year
        treatments.add(TreatmentTestFactory.builder().name("treatment A").startYear(2021).isSystemic(true).build())
        assertEquals(2, minSystemicTreatments(treatments).toLong())
        assertEquals(5, maxSystemicTreatments(treatments).toLong())

        // Add different systemic treatment with just year
        treatments.add(TreatmentTestFactory.builder().name("treatment C").startYear(2021).isSystemic(true).build())
        assertEquals(3, minSystemicTreatments(treatments).toLong())
        assertEquals(6, maxSystemicTreatments(treatments).toLong())

        // Make sure one older non-systemic doesn't screw up.
        treatments.add(TreatmentTestFactory.builder().name("treatment D").startYear(2019).startMonth(5).isSystemic(false).build())
        assertEquals(3, minSystemicTreatments(treatments).toLong())
        assertEquals(6, maxSystemicTreatments(treatments).toLong())
    }

    @Test
    fun canDetermineLastSystemicTreatment() {
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertNull(lastSystemicTreatment(treatments))

        treatments.add(TreatmentTestFactory.builder().isSystemic(false).build())
        assertNull(lastSystemicTreatment(treatments))

        treatments.add(TreatmentTestFactory.builder().isSystemic(true).startYear(2020).stopReason("reason 1").build())
        assertEquals("reason 1", lastSystemicTreatment(treatments)!!.stopReason())

        treatments.add(TreatmentTestFactory.builder().isSystemic(true).startYear(2021).stopReason("reason 2").build())
        assertEquals("reason 2", lastSystemicTreatment(treatments)!!.stopReason())

        treatments.add(TreatmentTestFactory.builder().isSystemic(true).startYear(2021).startMonth(1).stopReason("reason 3").build())
        assertEquals("reason 3", lastSystemicTreatment(treatments)!!.stopReason())

        treatments.add(TreatmentTestFactory.builder().isSystemic(true).startYear(2021).startMonth(10).stopReason("reason 4").build())
        assertEquals("reason 4", lastSystemicTreatment(treatments)!!.stopReason())

        treatments.add(TreatmentTestFactory.builder().isSystemic(true).startYear(2021).startMonth(8).stopReason("reason 5").build())
        assertEquals("reason 4", lastSystemicTreatment(treatments)!!.stopReason())
    }
}