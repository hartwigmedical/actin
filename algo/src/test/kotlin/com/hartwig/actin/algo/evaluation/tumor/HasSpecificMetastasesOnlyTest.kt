package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
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
    fun `Should pass when patient has liver metastases only`() {
        val record = TumorTestFactory.withTumorDetails(withNoOutsideLesions(hasLiverLesions = true))

        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverMetastasesOnly.evaluate(record),
            "Only liver metastases in provided lesions"
        )
        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record),
            "Only liver and/or lymph node and/or lung metastases in provided lesions"
        )
    }

    @Test
    fun `Should pass when patient has liver metastases only and only suspected lesions data is missing`() {
        val record = TumorTestFactory.withTumorDetails(
            withNoOutsideLesions(hasLiverLesions = true).copy(
                otherSuspectedLesions = null,
                hasSuspectedBrainLesions = null
            )
        )

        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverMetastasesOnly.evaluate(record),
            "Only liver metastases in provided lesions"
        )
        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record),
            "Only liver and/or lymph node and/or lung metastases in provided lesions"
        )
    }

    @Test
    fun `Should pass when patient has only lymph node metastases`() {
        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(TumorTestFactory.withTumorDetails(withNoOutsideLesions(hasLymphNodeLesions = true))),
            "Only liver and/or lymph node and/or lung metastases in provided lesions"
        )
    }

    @Test
    fun `Should pass when patient has liver, lymph node and lung metastases only`() {
        assertEvaluation(
            EvaluationResult.PASS,
            hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(
                TumorTestFactory.withTumorDetails(
                    withNoOutsideLesions(
                        hasLiverLesions = true,
                        hasLymphNodeLesions = true,
                        hasLungLesions = true
                    )
                )
            ),
            "Only liver and/or lymph node and/or lung metastases in provided lesions"
        )
    }

    @Test
    fun `Should fail when patient has no liver metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            hasLiverMetastasesOnly.evaluate(TumorTestFactory.withConfirmedLesions(hasLiverLesions = false)),
            "Metastases in provided lesions are not limited to liver"
        )
    }

    @Test
    fun `Should fail when patient has no liver, lymph node or lung metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(
                TumorTestFactory.withConfirmedLesions(
                    hasLiverLesions = false,
                    hasLymphNodeLesions = false,
                    hasLungLesions = false
                )
            ),
            "Metastases in provided lesions are not limited to liver and/or lymph node and/or lung"
        )
    }

    @Test
    fun `Should fail when patient has liver lesion but also bone metastases`() {
        val record = TumorTestFactory.withConfirmedLesions(hasLiverLesions = true, hasBoneLesions = true)

        assertEvaluation(
            EvaluationResult.FAIL,
            hasLiverMetastasesOnly.evaluate(record),
            "Metastases in provided lesions are not limited to liver"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record),
            "Metastases in provided lesions are not limited to liver and/or lymph node and/or lung"
        )
    }

    @Test
    fun `Should fail when patient has liver lesion but also other lesion`() {
        val record = TumorTestFactory.withTumorDetails(withNoOutsideLesions(hasLiverLesions = true).copy(otherLesions = listOf("skin")))

        assertEvaluation(
            EvaluationResult.FAIL,
            hasLiverMetastasesOnly.evaluate(record),
            "Metastases in provided lesions are not limited to liver"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record),
            "Metastases in provided lesions are not limited to liver and/or lymph node and/or lung"
        )
    }

    @Test
    fun `Should be undetermined when only suspected liver metastases exist`() {
        val record = TumorTestFactory.withBoneAndSuspectedLiverLesions(hasBoneLesions = false, hasSuspectedLiverLesions = true)
        val evaluationSingle = hasLiverMetastasesOnly.evaluate(record)
        val evaluationMultiple = hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record)

        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluationSingle,
            "Undetermined if only liver metastases based on provided lesions (suspected lesions provided and/or missing lesion data)"
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluationMultiple,
            "Undetermined if only liver and/or lymph node and/or lung metastases based on provided lesions (suspected lesions provided and/or missing lesion data)"
        )
    }

    @Test
    fun `Should be undetermined when liver lesion is present but suspected bone lesions exist`() {
        val record = TumorTestFactory.withTumorDetails(withNoOutsideLesions(hasLiverLesions = true).copy(hasSuspectedBoneLesions = true))
        val evaluationSingle = hasLiverMetastasesOnly.evaluate(record)
        val evaluationMultiple = hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record)

        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluationSingle,
            "Undetermined if only liver metastases based on provided lesions (suspected lesions provided and/or missing lesion data)"
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluationMultiple,
            "Undetermined if only liver and/or lymph node and/or lung metastases based on provided lesions (suspected lesions provided and/or missing lesion data)"
        )
    }

    @Test
    fun `Should be undetermined when liver lesion is present but suspected other lesions exist`() {
        val record =
            TumorTestFactory.withTumorDetails(withNoOutsideLesions(hasLiverLesions = true).copy(otherSuspectedLesions = listOf("lesion")))
        val evaluationSingle = hasLiverMetastasesOnly.evaluate(record)
        val evaluationMultiple = hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record)

        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluationSingle,
            "Undetermined if only liver metastases based on provided lesions (suspected lesions provided and/or missing lesion data)"
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluationMultiple,
            "Undetermined if only liver and/or lymph node and/or lung metastases based on provided lesions (suspected lesions provided and/or missing lesion data)"
        )
    }

    @Test
    fun `Should be undetermined when liver lesion is present but some lesion data is missing`() {
        val record = TumorTestFactory.withLiverLesions(true)
        val evaluationSingle = hasLiverMetastasesOnly.evaluate(record)
        val evaluationMultiple = hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record)

        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluationSingle,
            "Undetermined if only liver metastases based on provided lesions"
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluationMultiple,
            "Undetermined if only liver and/or lymph node and/or lung metastases based on provided lesions"
        )
    }

    @Test
    fun `Should be undetermined when lesion data is missing`() {
        val record = TumorTestFactory.withLiverLesions(null)
        val evaluationSingle = hasLiverMetastasesOnly.evaluate(record)
        val evaluationMultiple = hasLiverAndOrLymphNodeAndOrLungMetastasesOnly.evaluate(record)

        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluationSingle,
            "Undetermined if only liver metastases based on provided lesions"
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluationMultiple,
            "Undetermined if only liver and/or lymph node and/or lung metastases based on provided lesions"
        )
    }

    private fun withNoOutsideLesions(
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