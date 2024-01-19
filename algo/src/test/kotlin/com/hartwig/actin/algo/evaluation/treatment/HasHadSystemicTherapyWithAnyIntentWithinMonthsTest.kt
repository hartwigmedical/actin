package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import org.junit.Test
import java.time.LocalDate

class HasHadSystemicTherapyWithAnyIntentWithinMonthsTest {

    private val minDate = LocalDate.of(2023, 6, 1)
    val function = HasHadSystemicTherapyWithAnyIntentWithinMonths(setOf(Intent.NEOADJUVANT, Intent.ADJUVANT), minDate, 6)

    @Test
    fun `Should fail with no treatment history`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail with recent systemic palliative treatment`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = 2023,
                            stopMonth = 12,
                            intents = setOf(Intent.PALLIATIVE)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail with recent non-systemic adjuvant treatment`() {
        val treatment = TreatmentTestFactory.treatment("non systemic treatment", false)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = 2023,
                            stopMonth = 12,
                            intents = setOf(Intent.ADJUVANT)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail with systemic adjuvant treatment too long ago`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = 2020,
                            stopMonth = 12,
                            intents = setOf(Intent.ADJUVANT)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with recent systemic adjuvant treatment`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = 2023,
                            stopMonth = 12,
                            intents = setOf(Intent.ADJUVANT)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with recent systemic adjuvant treatment and non recent systemic neoadjuvant treatment`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = 2023,
                            stopMonth = 12,
                            intents = setOf(Intent.ADJUVANT)
                        ),
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(treatment),
                            stopYear = 2020,
                            stopMonth = 12,
                            intents = setOf(Intent.NEOADJUVANT)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should be undetermined with systemic adjuvant treatment without date`() {
        val treatment = TreatmentTestFactory.treatment("systemic treatment", true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
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
    }
}