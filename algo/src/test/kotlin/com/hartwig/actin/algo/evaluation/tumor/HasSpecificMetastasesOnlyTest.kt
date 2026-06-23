package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasSpecificMetastasesOnlyTest {

    private val hasLiverMetastasesOnly =
        HasSpecificMetastasesOnly(listOf(TumorDetails::hasLiverLesions), listOf(TumorDetails::hasSuspectedLiverLesions), "liver")
    private val hasLiverAndOrLymphNodeAndOrLungMetastasesOnly = HasSpecificMetastasesOnly(
        listOf(TumorDetails::hasLiverLesions, TumorDetails::hasLymphNodeLesions, TumorDetails::hasLungLesions),
        listOf(TumorDetails::hasSuspectedLiverLesions, TumorDetails::hasSuspectedLymphNodeLesions, TumorDetails::hasSuspectedLungLesions),
        "liver and/or lymph node and/or lung"
    )

    @Test
    fun `Should pass when patient has liver metastases only for both functions`() {
        val record = TumorTestFactory.withTumorDetails(noOutsideLesions(hasLiverLesions = true))

        assertEvaluation(EvaluationResult.PASS, hasLiverMetastasesOnly.evaluate(record))
        assertEvaluation(EvaluationResult.PASS, hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record))
    }

    @Test
    fun `Should pass when patient has liver metastases only and only suspected lesions data is missing for both functions`() {
        val record = TumorTestFactory.withTumorDetails(
            noOutsideLesions(hasLiverLesions = true).copy(
                otherSuspectedLesions = null,
                hasSuspectedBrainLesions = null
            )
        )

        assertEvaluation(EvaluationResult.PASS, hasLiverMetastasesOnly.evaluate(record))
        assertEvaluation(EvaluationResult.PASS, hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record))
    }

    @Test
    fun `Should pass when patient has lymph node metastases for hasLiverAndOrLymphNodeAndOrLungMetastasesOnly`() {
        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(TumorTestFactory.withTumorDetails(noOutsideLesions(hasLymphNodeLesions = true)))
        )
    }

    @Test
    fun `Should pass when patient has liver, lymph node and lung metastases for hasLiverAndOrLymphNodeAndOrLungMetastasesOnly`() {
        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(
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
    fun `Should fail when patient has no liver metastases for hasLiverMetastasesOnly`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            hasLiverMetastasesOnly.evaluate(TumorTestFactory.withConfirmedLesions(hasLiverLesions = false))
        )
    }

    @Test
    fun `Should fail when patient has no liver, lymph node or lung metastases for hasLiverAndOrLymphNodeAndOrLungMetastasesOnly`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(
                TumorTestFactory.withConfirmedLesions(
                    hasLiverLesions = false,
                    hasLymphNodeLesions = false,
                    hasLungLesions = false
                )
            )
        )
    }

    @Test
    fun `Should fail when patient has liver lesion but also bone metastases for both functions`() {
        val record = TumorTestFactory.withConfirmedLesions(hasLiverLesions = true, hasBoneLesions = true)

        assertEvaluation(EvaluationResult.FAIL, hasLiverMetastasesOnly.evaluate(record))
        assertEvaluation(EvaluationResult.FAIL, hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record))
    }

    @Test
    fun `Should fail when patient has liver lesion but also other lesion for both functions`() {
        val record = TumorTestFactory.withConfirmedLesions(hasLiverLesions = true, otherLesions = listOf("skin"))

        assertEvaluation(EvaluationResult.FAIL, hasLiverMetastasesOnly.evaluate(record))
        assertEvaluation(EvaluationResult.FAIL, hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record))
    }

    @Test
    fun `Should be undetermined when only suspected liver metastases exist for both functions`() {
        val record = TumorTestFactory.withBoneAndSuspectedLiverLesions(false, true)
        val evaluationSingle = hasLiverMetastasesOnly.evaluate(record)
        val evaluationMultiple = hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record)

        assertEvaluation(EvaluationResult.UNDETERMINED, evaluationSingle)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluationMultiple)

        assertThat(evaluationSingle.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has only liver metastases (suspected lesions presence and/or missing lesion data)")
        assertThat(evaluationMultiple.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has only liver and/or lymph node and/or lung metastases (suspected lesions presence and/or missing lesion data)")
    }

    @Test
    fun `Should be undetermined when liver lesion is present but suspected bone lesions exist`() {
        val record = TumorTestFactory.withTumorDetails(noOutsideLesions(hasLiverLesions = true).copy(hasSuspectedBoneLesions = true))
        val evaluationSingle = hasLiverMetastasesOnly.evaluate(record)
        val evaluationMultiple = hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record)

        assertEvaluation(EvaluationResult.UNDETERMINED, evaluationSingle)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluationMultiple)

        assertThat(evaluationSingle.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has only liver metastases (suspected lesions presence and/or missing lesion data)")
        assertThat(evaluationMultiple.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has only liver and/or lymph node and/or lung metastases (suspected lesions presence and/or missing lesion data)")
    }

    @Test
    fun `Should be undetermined when liver lesion is present but suspected other lesions exist`() {
        val record =
            TumorTestFactory.withTumorDetails(noOutsideLesions(hasLiverLesions = true).copy(otherSuspectedLesions = listOf("lesion")))
        val evaluationSingle = hasLiverMetastasesOnly.evaluate(record)
        val evaluationMultiple = hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record)

        assertEvaluation(EvaluationResult.UNDETERMINED, evaluationSingle)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluationMultiple)
    }

    @Test
    fun `Should be undetermined when liver lesion is present but some lesion data is missing`() {
        val record = TumorTestFactory.withLiverLesions(true)
        val evaluationSingle = hasLiverMetastasesOnly.evaluate(record)
        val evaluationMultiple = hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record)

        assertEvaluation(EvaluationResult.UNDETERMINED, evaluationSingle)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluationMultiple)

        assertThat(evaluationSingle.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has only liver metastases (missing lesion data)")
        assertThat(evaluationMultiple.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has only liver and/or lymph node and/or lung metastases (missing lesion data)")
    }

    @Test
    fun `Should be undetermined when lesion data is missing`() {
        val record = TumorTestFactory.withLiverLesions(null)
        val evaluationSingle = hasLiverMetastasesOnly.evaluate(record)
        val evaluationMultiple = hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record)

        assertEvaluation(EvaluationResult.UNDETERMINED, evaluationSingle)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluationMultiple)
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