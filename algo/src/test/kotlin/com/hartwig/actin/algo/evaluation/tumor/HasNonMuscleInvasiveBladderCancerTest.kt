package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.HasNonMuscleInvasiveBladderCancer.Companion.NON_MUSCLE_INVASIVE_TERMS
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasNonMuscleInvasiveBladderCancerTest {

    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val function = HasNonMuscleInvasiveBladderCancer(doidModel)
    val targetType = NON_MUSCLE_INVASIVE_TERMS.first()

    @Test
    fun `Should evaluate to undetermined if there are no tumor doids configured`() {
        val tumor = TumorTestFactory.withDoids(null)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumor))
    }

    @Test
    fun `Should pass if tumor is bladder cancer of non muscle invasive type`() {
        val tumor = TumorTestFactory.withDoidAndName(DoidConstants.URINARY_BLADDER_CANCER_DOID, "name with $targetType")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumor))
    }

    @Test
    fun `Should evaluate to undetermined if tumor is bladder cancer but unknown of non muscle invasive type`() {
        val tumor = TumorTestFactory.withDoidAndName(DoidConstants.URINARY_BLADDER_CANCER_DOID, "name without")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumor))
    }

    @Test
    fun `Should fail if tumor type is not bladder cancer`() {
        val tumor = TumorTestFactory.withDoids(DoidConstants.BRAIN_CANCER_DOID)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(tumor))
    }
}