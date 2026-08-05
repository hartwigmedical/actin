package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasOvarianBorderlineTumorTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).tumor
    private val function = HasOvarianBorderlineTumor(TestDoidModelFactory.createMinimalTestDoidModel(), labels)
    private val targetedType = HasOvarianBorderlineTumor.BORDERLINE_TERMS.first()

    @Test
    fun canEvaluate() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))

        val wrongCancerType = TumorTestFactory.withDoidAndName("wrong", targetedType)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(wrongCancerType))

        val genericType = TumorTestFactory.withDoidAndName(DoidConstants.OVARIAN_CANCER_DOID, "wrong")
        assertEvaluation(EvaluationResult.WARN, function.evaluate(genericType))

        val correct = TumorTestFactory.withDoidAndName(DoidConstants.OVARIAN_CANCER_DOID, targetedType)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(correct))
    }
}