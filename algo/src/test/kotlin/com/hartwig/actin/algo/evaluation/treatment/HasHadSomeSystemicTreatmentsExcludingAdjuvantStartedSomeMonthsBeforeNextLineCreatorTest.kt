package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import org.junit.Test
import java.time.LocalDate

class HasHadSomeSystemicTreatmentsExcludingAdjuvantStartedSomeMonthsBeforeNextLineTest {

    private val referenceDate = LocalDate.of(2025, 12, 1)
    private val nonRecentDate = referenceDate.minusYears(5)
    private val maxMonthsBeforeNext = 12
    private val function = HasHadSomeSystemicTreatmentsExcludingCurativeNeoadjuvantOrAdjuvantStoppedSomeMonthsBeforeNextLine(
        minSystemicTreatments = 2,
        maxMonthsBeforeNextLine = maxMonthsBeforeNext,
        referenceDate = referenceDate
    )
    private val systemicTreatment = TreatmentTestFactory.treatment("Systemic Treatment", isSystemic = true)
    private val nonRecentPalliativeSystemic =
        TreatmentTestFactory.treatmentHistoryEntry(
            setOf(systemicTreatment),
            intents = setOf(Intent.PALLIATIVE),
            startYear = nonRecentDate.year,
            startMonth = nonRecentDate.monthValue
        )
    private val recentPalliativeSystemic = nonRecentPalliativeSystemic.copy(
        treatments = setOf(TreatmentTestFactory.treatment("Other", isSystemic = true)),
        startYear = referenceDate.year,
        startMonth = referenceDate.monthValue
    )
    private val nonRecentCurativeNeoadjuvantAdjuvantTreatments = listOf(
        TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.treatment("cur", true)),
            intents = setOf(Intent.CURATIVE),
            startYear = nonRecentDate.year,
            startMonth = nonRecentDate.monthValue
        ),
        TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.treatment("adj", true)),
            intents = setOf(Intent.ADJUVANT),
            startYear = nonRecentDate.minusYears(2).year,
            startMonth = nonRecentDate.minusYears(2).monthValue
        ),
        TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.treatment("neo", true)),
            intents = setOf(Intent.NEOADJUVANT),
            startYear = nonRecentDate.minusYears(4).year,
            startMonth = nonRecentDate.minusYears(4).monthValue
        )
    )

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
                    TreatmentTestFactory.treatmentHistoryEntry(
                        treatments = setOf(TreatmentTestFactory.treatment("", false)),
                        intents = setOf(Intent.PALLIATIVE)
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when threshold is not met`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistoryEntry(
                    TreatmentTestFactory.treatmentHistoryEntry(
                        treatments = setOf(TreatmentTestFactory.treatment("", false)),
                        intents = setOf(Intent.PALLIATIVE)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass when systemic treatments meet threshold`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistory(listOf(nonRecentPalliativeSystemic, recentPalliativeSystemic))
            )
        )
    }

    @Test
    fun `Should pass when systemic treatments exceed threshold`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistory(
                    listOf(
                        nonRecentPalliativeSystemic,
                        recentPalliativeSystemic,
                        nonRecentPalliativeSystemic.copy(treatments = setOf(TreatmentTestFactory.treatment("Another", isSystemic = true)))
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when history contains more lines than threshold but only curative and (neo)adjuvant treatments started too long before next line`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(nonRecentCurativeNeoadjuvantAdjuvantTreatments))
        )
    }

    @Test
    fun `Should fail when history contains one palliative line and others are curative and (neo)adjuvant treatments started too long before next line`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistory(
                    listOf(recentPalliativeSystemic) + nonRecentCurativeNeoadjuvantAdjuvantTreatments
                )
            )
        )
    }

    @Test
    fun `Should include curative and (neo)adjuvant treatments when started within max months before next line`() {
        listOf(Intent.CURATIVE, Intent.NEOADJUVANT, Intent.ADJUVANT).forEach { intent ->
            val firstTreatment = TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(TreatmentTestFactory.treatment(intent.name, true)),
                intents = setOf(intent),
                startYear = referenceDate.year,
                startMonth = referenceDate.monthValue - 8
            )
            val treatments = listOf(firstTreatment, recentPalliativeSystemic)
            assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
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

    //TODO()// ADD TESTS FOR UNKNOWN DATES SCENARIOS
    //TODO()// ADD TESTS FOR TOXICITY STOP REASON SCENARIOS
    //TODO()// ADD TESTS FOR AMBIGUOUS SCENARIOS (IN 6 MONTHS MARGIN)
}