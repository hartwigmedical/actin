package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val REQUESTED_INTENT = setOf(Intent.ADJUVANT)
private val WRONG_INTENT = setOf(Intent.PALLIATIVE)
private val SYSTEMIC_TREATMENT = TreatmentTestFactory.treatment("systemic treatment", true)
private val NON_SYSTEMIC_TREATMENT = TreatmentTestFactory.treatment("non systemic treatment", false)

class HasHadSystemicTherapyWithAnyIntentTest {

    private val referenceDate = LocalDate.of(2024, 1, 25)
    private val refDate = referenceDate.minusMonths(6)
    private val recentDate = referenceDate.minusMonths(3)
    private val olderDate = referenceDate.minusMonths(14)
    private val functionEvaluatingWithinWeeks =
        HasHadSystemicTherapyWithAnyIntent(REQUESTED_INTENT, refDate, 6, true)
    private val functionEvaluatingBeforeWeeks =
        HasHadSystemicTherapyWithAnyIntent(REQUESTED_INTENT, refDate, 6, false)
    private val functionWithoutDate = HasHadSystemicTherapyWithAnyIntent(REQUESTED_INTENT, null, null, null)
    private val functionWithoutIntentsAndWithinWeeks = HasHadSystemicTherapyWithAnyIntent(null, refDate, 6, true)

    @Test
    fun `Should fail with no treatment history`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionEvaluatingWithinWeeks.evaluate(withTreatmentHistory(emptyList())))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionEvaluatingBeforeWeeks.evaluate(withTreatmentHistory(emptyList())))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithoutDate.evaluate(withTreatmentHistory(emptyList())))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutIntentsAndWithinWeeks.evaluate(withTreatmentHistory(emptyList()))
        )
    }

    @Test
    fun `Should fail with systemic treatment with wrong intent (and correct date) only if intent is evaluated`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = WRONG_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionEvaluatingWithinWeeks.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithoutDate.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutIntentsAndWithinWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should fail with systemic treatment with wrong intent (and correct date) only if intent is evaluated also for not within weeks`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue,
                    intents = WRONG_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionEvaluatingBeforeWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should fail with non-systemic treatment with correct intent (and correct date)`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(NON_SYSTEMIC_TREATMENT),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = REQUESTED_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionEvaluatingWithinWeeks.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithoutDate.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithoutIntentsAndWithinWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should fail with non-systemic treatment with correct intent (and correct date) also for not within weeks`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(NON_SYSTEMIC_TREATMENT),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue,
                    intents = REQUESTED_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionEvaluatingBeforeWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should fail with systemic treatment with correct intent but too long ago if evaluating within weeks and pass if evaluating before weeks`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue,
                    intents = REQUESTED_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionEvaluatingWithinWeeks.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionEvaluatingBeforeWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should fail with systemic treatment with correct intent but too long ago according to start date if evaluating within weeks and undetermined if evaluating before weeks`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    startYear = olderDate.year,
                    startMonth = olderDate.monthValue,
                    intents = REQUESTED_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionEvaluatingWithinWeeks.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingBeforeWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should pass with systemic treatment with correct intent with recent stop date if evaluating before weeks and fail if evaluating not before weeks`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = REQUESTED_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionEvaluatingWithinWeeks.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionEvaluatingBeforeWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should pass with systemic treatment with correct intent with recent start date if evaluating before weeks and undetermined if evaluating not before weeks`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    startYear = recentDate.year,
                    startMonth = recentDate.monthValue,
                    intents = REQUESTED_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionEvaluatingWithinWeeks.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingBeforeWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should pass with recent systemic treatment with correct intent and within weeks unless not evaluating within weeks`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = REQUESTED_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionEvaluatingWithinWeeks.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionEvaluatingBeforeWeeks.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutDate.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutIntentsAndWithinWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should pass with systemic treatment with correct intent and unknown or older dates if evaluating without date`() {
        val patientRecordOldDate = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue,
                    intents = REQUESTED_INTENT
                )
            )
        )
        val patientRecordUnknownDate = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    intents = REQUESTED_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutDate.evaluate(patientRecordOldDate))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutDate.evaluate(patientRecordUnknownDate))
    }

    @Test
    fun `Should pass with one recent systemic treatment and one non-recent systemic treatment, both with correct intent`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = REQUESTED_INTENT
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue,
                    intents = REQUESTED_INTENT
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionEvaluatingWithinWeeks.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionEvaluatingBeforeWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate to undetermined with systemic treatment with correct intent but without (stop)date`() {
        val therapyWithoutDate = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    intents = REQUESTED_INTENT
                )
            )
        )
        val therapyWithoutStopDate = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    intents = REQUESTED_INTENT,
                    startYear = 2023
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingWithinWeeks.evaluate(therapyWithoutDate))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingWithinWeeks.evaluate(therapyWithoutStopDate))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingBeforeWeeks.evaluate(therapyWithoutDate))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingBeforeWeeks.evaluate(therapyWithoutStopDate))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionWithoutIntentsAndWithinWeeks.evaluate(therapyWithoutDate))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithoutIntentsAndWithinWeeks.evaluate(therapyWithoutStopDate)
        )
    }

    @Test
    fun `Should evaluate to undetermined with treatment with missing intent if intent is evaluated and evaluating within weeks`() {
        val treatmentX = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("TREATMENT X", true)),
            stopYear = recentDate.year,
            stopMonth = recentDate.monthValue,
            intents = null
        )
        val treatmentY = treatmentX.copy(treatments = setOf(TreatmentTestFactory.treatment("treatment y", true)))
        val patientRecord = withTreatmentHistory(listOf(treatmentX, treatmentY))
        val evaluationWithWeeksAgoDate = functionEvaluatingWithinWeeks.evaluate(patientRecord)
        val evaluationWithoutDate = functionWithoutDate.evaluate(patientRecord)

        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluationWithWeeksAgoDate)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluationWithoutDate)
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutIntentsAndWithinWeeks.evaluate(patientRecord))

        listOf(evaluationWithWeeksAgoDate, evaluationWithoutDate).forEach {
            assertThat(it.undeterminedMessages).containsExactly(
                "Has received systemic treatment (Treatment x and Treatment y) but undetermined if intent is adjuvant"
            )
        }
    }

    @Test
    fun `Should evaluate to undetermined with treatment with missing intent if intent is evaluated also if not evaluating within weeks`() {
        val treatmentX = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("Treatment X", true)),
            stopYear = olderDate.year,
            stopMonth = olderDate.monthValue,
            intents = null
        )
        val treatmentY = treatmentX.copy(treatments = setOf(TreatmentTestFactory.treatment("Treatment y", true)))
        val patientRecord = withTreatmentHistory(listOf(treatmentX, treatmentY))

        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingBeforeWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate to undetermined with one too-old systemic treatment with correct intent and one recent systemic treatment with unknown intent and within weeks evaluated`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue,
                    intents = setOf(Intent.ADJUVANT)
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = null
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingWithinWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate to undetermined with one too-old systemic treatment with correct intent and one recent systemic treatment with unknown intent and not within weeks evaluated`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = REQUESTED_INTENT
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(SYSTEMIC_TREATMENT),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue,
                    intents = null
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingBeforeWeeks.evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate to undetermined with one recent systemic treatment with incorrect intent and one recent systemic treatment with unknown intent and evaluating within weeks`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionEvaluatingWithinWeeks.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(SYSTEMIC_TREATMENT),
                            stopYear = recentDate.year,
                            stopMonth = recentDate.monthValue,
                            intents = WRONG_INTENT
                        ),
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(SYSTEMIC_TREATMENT),
                            stopYear = recentDate.year,
                            stopMonth = recentDate.monthValue,
                            intents = null
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined with one non-recent systemic treatment with incorrect intent and one recent systemic treatment with unknown intent and not evaluating within weeks`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionEvaluatingBeforeWeeks.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(SYSTEMIC_TREATMENT),
                            stopYear = olderDate.year,
                            stopMonth = olderDate.monthValue,
                            intents = setOf(Intent.PALLIATIVE)
                        ),
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(SYSTEMIC_TREATMENT),
                            stopYear = olderDate.year,
                            stopMonth = olderDate.monthValue,
                            intents = null
                        )
                    )
                )
            )
        )
    }
}