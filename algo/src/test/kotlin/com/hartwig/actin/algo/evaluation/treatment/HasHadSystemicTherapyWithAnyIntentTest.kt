package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import org.junit.Test
import java.time.LocalDate

class HasHadSystemicTherapyWithAnyIntentTest {

    private val minDate = LocalDate.now().minusMonths(6)
    private val recentDate = LocalDate.now().minusMonths(3)
    private val olderDate = LocalDate.now().minusMonths(14)
    private val functionWithDate = HasHadSystemicTherapyWithAnyIntent(setOf(Intent.NEOADJUVANT, Intent.ADJUVANT), minDate, 6)
    private val functionWithoutDate = HasHadSystemicTherapyWithAnyIntent(setOf(Intent.NEOADJUVANT, Intent.ADJUVANT), null, null)

    @Test
    fun `Should fail with no treatment history`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithDate.evaluate(withTreatmentHistory(emptyList())))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithoutDate.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail with systemic treatment with wrong intent (and correct date)`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = setOf(Intent.PALLIATIVE)
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithDate.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithoutDate.evaluate(patientRecord))

    }

    @Test
    fun `Should fail with non-systemic treatment with correct intent (and correct date)`() {
        val treatment = TreatmentTestFactory.treatment("non systemic treatment", false)
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = setOf(Intent.ADJUVANT)
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithDate.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithoutDate.evaluate(patientRecord))
    }

    @Test
    fun `Should fail with systemic treatment with correct intent but too long ago`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            functionWithDate.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = olderDate.year,
                            stopMonth = olderDate.monthValue,
                            intents = setOf(Intent.ADJUVANT)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with recent systemic treatment with correct intent`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = setOf(Intent.ADJUVANT)
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithDate.evaluate(patientRecord))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutDate.evaluate(patientRecord))
    }

    @Test
    fun `Should pass with one recent systemic treatment and one non-recent systemic treatment, both with correct intent`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            functionWithDate.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = recentDate.year,
                            stopMonth = recentDate.monthValue,
                            intents = setOf(Intent.ADJUVANT)
                        ),
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = olderDate.year,
                            stopMonth = olderDate.monthValue,
                            intents = setOf(Intent.NEOADJUVANT)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should be undetermined with systemic treatment with correct intent but without (stop)date`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithDate.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            intents = setOf(Intent.ADJUVANT)
                        )
                    )
                )
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithDate.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            intents = setOf(Intent.ADJUVANT),
                            startYear = 2023
                        )
                    )
                )
            )
        )
    }
}