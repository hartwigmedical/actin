package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasNonSquamousNSCLCTest {
    @Test
    fun `Should return undetermined when no tumor doids configured`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function().evaluate(TestPatientFactory.createMinimalTestPatientRecord())
        )
    }

    @Test
    fun `Should return fail when tumor is not lung`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function().evaluate(TumorTestFactory.withDoids("wrong"))
        )
    }

    @Test
    fun `Should return fail when squamous NSCLC type`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function().evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_SQUAMOUS_CELL_CARCINOMA_DOID))
        )
    }

    @Test
    fun `Should return fail when adenosquamous NSCLC type`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function().evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_ADENOSQUAMOUS_CARCINOMA_DOID))
        )
    }

    @Test
    fun `Should return pass when known non-squamous NSCLC type`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function().evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_ADENOCARCINOMA_DOID))
        )
    }

    @Test
    fun `Should return pass when known non-squamous NSCLC type with other random DOID`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function().evaluate(
                TumorTestFactory.withDoids(DoidConstants.LUNG_ADENOCARCINOMA_DOID, "random DOID")
            )
        )
    }

    @Test
    fun `Should return undetermined when lung cancer that is potentially non-squamous NSCLC`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function().evaluate(
                TumorTestFactory.withDoids(DoidConstants.LUNG_CANCER_DOID)
            )
        )
    }

    @Test
    fun `Should return fail when lung cancer that is not potentially non-squamous NSCLC`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function().evaluate(
                TumorTestFactory.withDoids(DoidConstants.LUNG_SARCOMA)
            )
        )
    }

    companion object {
        private fun function(): HasNonSquamousNSCLC {
            val doidModel: DoidModel = TestDoidModelFactory.createMinimalTestDoidModel()
            return HasNonSquamousNSCLC(doidModel)
        }
    }
}