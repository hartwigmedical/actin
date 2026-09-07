package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasOvarianBorderlineTumorTest {
    
    private val function = HasOvarianBorderlineTumor(TestDoidModelFactory.createMinimalTestDoidModel())
    private val targetedType = HasOvarianBorderlineTumor.BORDERLINE_TERMS.first()

    @Test
    fun canEvaluate() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withDoids(null)),
            "Ovarian borderline tumor undetermined (no DOIDs)"
        )

        val wrongCancerType = TumorTestFactory.withDoidAndName("wrong", targetedType)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(wrongCancerType), "Has no ovarian borderline tumor")

        val genericType = TumorTestFactory.withDoidAndName(DoidConstants.OVARIAN_CANCER_DOID, "wrong")
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(genericType),
            "Has ovarian cancer - undetermined if may be a borderline tumor"
        )

        val correct = TumorTestFactory.withDoidAndName(DoidConstants.OVARIAN_CANCER_DOID, targetedType)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(correct), "Has ovarian borderline tumor")
    }
}