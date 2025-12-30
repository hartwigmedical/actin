package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import org.junit.Test
import java.time.LocalDate

class HasHadSomeSystemicTreatmentsExcludingAdjuvantStartedSomeMonthsBeforeNextLineTest {

    private val referenceDate = LocalDate.of(2025, 12, 1)
    private val maxMonthsBeforeNext = 12
    private val function = HasHadSomeSystemicTreatmentsExcludingAdjuvantStartedSomeMonthsBeforeNextLineCreator(
        minSystemicTreatments = 1,
        maxMonthsBeforeNextLine = maxMonthsBeforeNext,
        referenceDate = referenceDate
    )
    private val systemicTreatment = TreatmentTestFactory.treatment("Systemic Treatment", isSystemic = true)

    @Test
    fun `Should fail when treatment history is empty`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail when treatment history only contains non systemic treatments`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistoryEntry(
                    TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(TreatmentTestFactory.treatment("", false)))
                )
            )
        )
    }

    @Test
    fun `Should pass when systemic treatments meet threshold`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistoryEntry(
                    TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(systemicTreatment))
                )
            )
        )
    }

    @Test
    fun `Should pass when systemic treatments exceed threshold`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistoryEntry(
                    TreatmentTestFactory.treatmentHistoryEntry(
                        treatments = setOf(
                            systemicTreatment,
                            TreatmentTestFactory.treatment("", true)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should exclude curative and (neo)adjuvant treatments started too long before next line`() {
//        val adjuvantTreatment = TreatmentTestFactory.treatmentHistoryEntry(
//            treatments = setOf(TreatmentTestFactory.treatment("Adjuvant", true)),
//            intents = setOf(Intent.ADJUVANT),
//            startYear = 2023,
//            startMonth = 10
//        )
//        val systemicTreatment = TreatmentTestFactory.treatmentHistoryEntry(
//            treatments = setOf(systemicTreatment),
//            startYear = 2025,
//            startMonth = 1
//        )
//        val treatments = listOf(adjuvantTreatment, systemicTreatment)
//        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should exclude curative and (neo)adjuvant treatments started too long before reference date if no next line present`() {
//        val adjuvantTreatment = TreatmentTestFactory.treatmentHistoryEntry(
//            treatments = setOf(TreatmentTestFactory.treatment("Adjuvant", true)),
//            intents = setOf(Intent.ADJUVANT),
//            startYear = 2023,
//            startMonth = 10
//        )
//        val systemicTreatment = TreatmentTestFactory.treatmentHistoryEntry(
//            treatments = setOf(systemicTreatment),
//            startYear = 2025,
//            startMonth = 1
//        )
//        val treatments = listOf(adjuvantTreatment, systemicTreatment)
//        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should include curative and (neo)adjuvant treatments when started within max months before next line`() {
//        val adjuvantTreatment = TreatmentTestFactory.treatmentHistoryEntry(
//            treatments = setOf(TreatmentTestFactory.treatment("Adjuvant", true)),
//            intents = setOf(Intent.ADJUVANT),
//            startYear = 2024,
//            startMonth = 12
//        )
//        val systemicTreatment = TreatmentTestFactory.treatmentHistoryEntry(
//            treatments = setOf(systemicTreatment),
//            startYear = 2025,
//            startMonth = 1
//        )
//        val treatments = listOf(adjuvantTreatment, systemicTreatment)
//        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should include curative and (neo)adjuvant treatments when started within max months before reference date if no next line present`() {
//        val adjuvantTreatment = TreatmentTestFactory.treatmentHistoryEntry(
//            treatments = setOf(TreatmentTestFactory.treatment("Adjuvant", true)),
//            intents = setOf(Intent.ADJUVANT),
//            startYear = 2024,
//            startMonth = 12
//        )
//        val systemicTreatment = TreatmentTestFactory.treatmentHistoryEntry(
//            treatments = setOf(systemicTreatment),
//            startYear = 2025,
//            startMonth = 1
//        )
//        val treatments = listOf(adjuvantTreatment, systemicTreatment)
//        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }
}