package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class HasHadSystemicTherapyWithAnyIntentTest {

    private val referenceDate = LocalDate.of(2024, 1, 25)
    private val minDate = referenceDate.minusMonths(6)
    private val recentDate = referenceDate.minusMonths(3)
    private val olderDate = referenceDate.minusMonths(14)
    private val functionWithDate = HasHadSystemicTherapyWithAnyIntent(setOf(Intent.NEOADJUVANT, Intent.ADJUVANT), minDate, 6)
    private val functionWithoutDate = HasHadSystemicTherapyWithAnyIntent(setOf(Intent.NEOADJUVANT, Intent.ADJUVANT), null, null)
    private val functionWithoutIntents = HasHadSystemicTherapyWithAnyIntent(null, minDate, 6)

    @Test
    fun `Should fail with no treatment history`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithDate.evaluate(withTreatmentHistory(emptyList())))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithoutDate.evaluate(withTreatmentHistory(emptyList())))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithoutIntents.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail with systemic treatment with wrong intent (and correct date) only if intent is evaluated`() {
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
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutIntents.evaluate(patientRecord))
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
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionWithoutIntents.evaluate(patientRecord))
    }

    @Test
    fun `Should fail with systemic treatment with correct intent but too long ago`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue,
                    intents = setOf(Intent.ADJUVANT)
                )
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            functionWithDate.evaluate(
                patientRecord
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
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutIntents.evaluate(patientRecord))
    }

    @Test
    fun `Should pass with systemic treatment with correct intent and unknown or older dates`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        val patientRecordOldDate = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue,
                    intents = setOf(Intent.ADJUVANT)
                )
            )
        )
        val patientRecordUnknownDate = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    intents = setOf(Intent.ADJUVANT)
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutDate.evaluate(patientRecordOldDate))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutDate.evaluate(patientRecordUnknownDate))
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
        val therapyWithoutDate = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    intents = setOf(Intent.ADJUVANT)
                )
            )
        )
        val therapyWithoutStopDate = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    intents = setOf(Intent.ADJUVANT),
                    startYear = 2023
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionWithDate.evaluate(therapyWithoutDate))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionWithDate.evaluate(therapyWithoutStopDate))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionWithoutIntents.evaluate(therapyWithoutDate))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionWithoutIntents.evaluate(therapyWithoutStopDate))
    }

    @Test
    fun `Should be undetermined with treatment with missing intent, if intent is evaluated`() {
        val treatment = TreatmentTestFactory.treatment("treatment x", true)
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = null
                )
            )
        )
        val evaluationWithDate = functionWithDate.evaluate(patientRecord)
        val evaluationWithoutDate = functionWithoutDate.evaluate(patientRecord)

        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluationWithDate)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluationWithoutDate)
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionWithoutIntents.evaluate(patientRecord))

        listOf(evaluationWithDate, evaluationWithoutDate).forEach {
            assertThat(it.undeterminedGeneralMessages).containsExactly(
                "Has received systemic treatment (Treatment x) but undetermined if intent is adjuvant or neoadjuvant"
            )
        }
    }

    @Test
    fun `Should be undetermined with one too-old systemic treatment with correct intent and one recent systemic treatment with unknown intent`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        val patientRecord = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue,
                    intents = setOf(Intent.ADJUVANT)
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue,
                    intents = null
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, functionWithDate.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined with one recent systemic treatment with incorrect intent and one recent systemic treatment with unknown intent`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithDate.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = recentDate.year,
                            stopMonth = recentDate.monthValue,
                            intents = setOf(Intent.PALLIATIVE)
                        ),
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = recentDate.year,
                            stopMonth = recentDate.monthValue,
                            intents = null
                        )
                    )
                )
            )
        )
    }
}