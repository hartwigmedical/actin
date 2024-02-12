package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory.withCnsOrBrainLesionsAndOncologicalHistory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import org.junit.Test

class HasReceivedPotentialSystemicTherapyForBrainMetastasesTest {

    private val function = HasReceivedPotentialSystemicTherapyForBrainMetastases()
    private val systemicTherapy = TreatmentTestFactory.treatment("Systemic treatment", isSystemic = true)
    private val systemicTreatment = TreatmentTestFactory.treatmentHistoryEntry(listOf(systemicTherapy))
    private val nonSystemicTherapy = TreatmentTestFactory.treatment("Local treatment", isSystemic = false)
    private val nonSystemicTreatment = TreatmentTestFactory.treatmentHistoryEntry(listOf(nonSystemicTherapy))

    @Test
    fun `Should warn if brain metastases present and received any systemic anti-cancer therapy`() {
        val clinicalRecord = withCnsOrBrainLesionsAndOncologicalHistory(hasCnsLesions = false, hasBrainLesions = true, systemicTreatment)
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(clinicalRecord))
    }

    @Test
    fun `Should warn if CNS metastases present and received any systemic anti-cancer therapy`() {
        val clinicalRecord = withCnsOrBrainLesionsAndOncologicalHistory(hasCnsLesions = true, hasBrainLesions = false, systemicTreatment)
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(clinicalRecord))
    }

    @Test
    fun `Should fail if brain metastases present but did not receive any systemic anti-cancer therapy`() {
        val clinicalRecord = withCnsOrBrainLesionsAndOncologicalHistory(hasCnsLesions = true, hasBrainLesions = true, nonSystemicTreatment)
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(clinicalRecord))
    }

    @Test
    fun `Should fail if no brain or CNS metastases present`() {
        val clinicalRecord = withCnsOrBrainLesionsAndOncologicalHistory(hasCnsLesions = false, hasBrainLesions = false, systemicTreatment)
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(clinicalRecord))
    }

    @Test
    fun `Should fail if oncological history empty`() {
        val clinicalRecord = withCnsOrBrainLesionsAndOncologicalHistory(
            hasCnsLesions = true, hasBrainLesions = true,
            TreatmentTestFactory.treatmentHistoryEntry(emptyList())
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(clinicalRecord))
    }
}