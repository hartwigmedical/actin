package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasLiverAndOrLymphNodeAndOrLungMetastasesOnlyTest {

    private val function = HasLiverAndOrLymphNodeAndOrLungMetastasesOnly()

    @Test
    fun `Should pass when patient has liver metastases only`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withTumorDetails(noOutsideLesions(hasLiverLesions = true)))
        )
    }

    @Test
    fun `Should pass when patient has lymph node metastases only`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withTumorDetails(noOutsideLesions(hasLymphNodeLesions = true)))
        )
    }

    @Test
    fun `Should pass when patient has lung metastases only`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withTumorDetails(noOutsideLesions(hasLungLesions = true)))
        )
    }

    @Test
    fun `Should pass when patient has liver, lymph node and lung metastases only`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withTumorDetails(
                    noOutsideLesions(
                        hasLiverLesions = true,
                        hasLymphNodeLesions = true,
                        hasLungLesions = true
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when patient has no liver, lymph node or lung metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withConfirmedLesions(
                    hasLiverLesions = false,
                    hasLymphNodeLesions = false,
                    hasLungLesions = false
                )
            )
        )
    }

    @Test
    fun `Should fail when patient has target lesion but also bone metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withConfirmedLesions(hasLiverLesions = true, hasBoneLesions = true))
        )
    }

    @Test
    fun `Should fail when patient has target lesion but also brain metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withConfirmedLesions(hasLiverLesions = true, hasBrainLesions = true))
        )
    }

    @Test
    fun `Should fail when patient has target lesion but also CNS metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withConfirmedLesions(hasLiverLesions = true, hasCnsLesions = true))
        )
    }

    @Test
    fun `Should fail when patient has target lesion but also other lesions`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withConfirmedLesions(hasLiverLesions = true, otherLesions = listOf("skin")))
        )
    }

    @Test
    fun `Should be undetermined when target lesion is present but suspected lesions exist`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withTumorDetails(
                noOutsideLesions(hasLiverLesions = true).copy(hasSuspectedBoneLesions = true)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has only liver and/or lymph node and/or lung metastases (suspected lesions presence and/or missing lesion data)")
    }

    @Test
    fun `Should be undetermined when target lesion is present but some lesion data is missing`() {
        val evaluation = function.evaluate(TumorTestFactory.withLiverLesions(true))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has only liver and/or lymph node and/or lung metastases (missing lesion data)")
    }

    @Test
    fun `Should be undetermined when lesion data is missing`() {
        val evaluation = function.evaluate(TumorTestFactory.withLiverLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has only liver and/or lymph node and/or lung metastases (missing lesion data)")
    }

    private fun noOutsideLesions(
        hasLiverLesions: Boolean = false,
        hasLymphNodeLesions: Boolean = false,
        hasLungLesions: Boolean = false
    ): TumorDetails {
        return TumorDetails(
            hasLiverLesions = hasLiverLesions,
            hasLymphNodeLesions = hasLymphNodeLesions,
            hasLungLesions = hasLungLesions,
            hasBoneLesions = false,
            hasBrainLesions = false,
            hasCnsLesions = false,
            otherLesions = emptyList(),
            hasSuspectedBoneLesions = false,
            hasSuspectedBrainLesions = false,
            hasSuspectedCnsLesions = false,
            otherSuspectedLesions = emptyList()
        )
    }
}
