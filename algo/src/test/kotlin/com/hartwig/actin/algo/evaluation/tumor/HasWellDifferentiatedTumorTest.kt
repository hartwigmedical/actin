package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.HasWellDifferentiatedTumor.Companion.OTHER_DIFFERENTIATION_TERMS
import com.hartwig.actin.algo.evaluation.tumor.HasWellDifferentiatedTumor.Companion.WELL_DIFFERENTIATED_TERMS
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasWellDifferentiatedTumorTest {

    val function = HasWellDifferentiatedTumor()
    val wellDifferentiatedType = WELL_DIFFERENTIATED_TERMS.first()
    val otherDifferentiationType = OTHER_DIFFERENTIATION_TERMS.first()

    @Test
    fun `Should pass if tumor is of well-differentiated type`() {
        val tumor = TumorTestFactory.withDoidAndName("doid", "name with $wellDifferentiatedType")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumor), "Tumor is well-differentiated")
    }

    @Test
    fun `Should evaluate to undetermined if tumor has unknown of differentiation type`() {
        val tumor = TumorTestFactory.withDoidAndName("doid", "name without")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumor), "Undetermined if well-differentiated tumor")
    }

    @Test
    fun `Should fail if tumor is of other differentiation type`() {
        val tumor = TumorTestFactory.withDoidAndName("doid", "name with $otherDifferentiationType")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(tumor), "Tumor is not well-differentiated")
    }
}