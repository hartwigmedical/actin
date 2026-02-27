package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class IsEligibleForLocalLiverTreatmentTest {

    private val function = IsEligibleForLocalLiverTreatment(TestDoidModelFactory.createMinimalTestDoidModel())

    @Test
    fun `Should fail when no liver lesions`() {
        val patientRecord = TumorTestFactory.withLiverLesions(false)
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined when data regarding liver lesions is missing`() {
        val patientRecord = TumorTestFactory.withLiverLesions(null)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined when patient has liver lesions`() {
        val patientRecord = TumorTestFactory.withLiverLesions(true)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate to undetermined when patient has liver cancer but no liver lesions`() {
        val patientRecord = TumorTestFactory.withDoidsAndLiverLesions(setOf(DoidConstants.LIVER_CANCER_DOID), false)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should fail when patient has bone cancer but no liver lesions`() {
        val patientRecord = TumorTestFactory.withDoidsAndLiverLesions(setOf(DoidConstants.BONE_CANCER_DOID), false)
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }
}