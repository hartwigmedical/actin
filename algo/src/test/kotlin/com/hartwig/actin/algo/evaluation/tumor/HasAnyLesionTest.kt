package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasAnyLesionTest {

    private val function = HasAnyLesion()

    @Test
    fun `Should pass if any type of categorical lesions present`() {
        listOf(
            TumorTestFactory.withBoneLesions(true),
            TumorTestFactory.withLiverLesions(true),
            TumorTestFactory.withCnsLesions(true),
            TumorTestFactory.withBrainLesions(true),
            TumorTestFactory.withLungLesions(true),
            TumorTestFactory.withLymphNodeLesions(true),
        ).forEach { assertEvaluation(EvaluationResult.PASS, function.evaluate(it)) }
    }

    @Test
    fun `Should pass if at least other lesions are present`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withOtherLesions(listOf("other"))))
    }

    @Test
    fun `Should warn if only suspected lesions are present`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withOtherSuspectedLesions(listOf("lesion"))))
    }

    @Test
    fun `Should be undetermined if all lesion localizations are undetermined`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withConfirmedLesions()))
    }

    @Test
    fun `Should be undetermined if some lesion localizations are undetermined and others are false`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withConfirmedLesions(false, false)))
    }

    @Test
    fun `Should fail if all lesions localizations are false`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withConfirmedLesions(
                    false, false, false, false, false, false,
                    emptyList()
                )
            )
        )
    }
}