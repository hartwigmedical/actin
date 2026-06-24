package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import org.junit.jupiter.api.Test

class HasHadAtMostSystemicTreatmentLinesInSpecificSettingTest {

    private val referenceDate = LocalDate.of(2024, 11, 26)
    private val recentDate = referenceDate.minusMonths(1)
    private val nonRecentDate = referenceDate.minusMonths(8)
    private val function = HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
        referenceDate = referenceDate,
        intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
        settingDescription = "metastatic",
        maximumLines = 2
    )

    @Test
    fun `Should pass with no prior systemic treatment`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should pass with only excluded intent treatments`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(listOf(createTreatment(Intent.CURATIVE))))
        )
    }

    @Test
    fun `Should fail if palliative lines alone exceed maximum`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.PALLIATIVE, Intent.PALLIATIVE).map { createTreatment(it) }
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail when probable lines exceed maximum plus one buffer`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(Intent.MAINTENANCE, stopYear = recentDate.year, stopMonth = recentDate.monthValue),
                createTreatment(Intent.INDUCTION, stopYear = recentDate.year, stopMonth = recentDate.monthValue),
                createTreatment(null, stopYear = recentDate.year, stopMonth = recentDate.monthValue)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should be undetermined when uncertain lines could push total over maximum`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(Intent.MAINTENANCE, stopYear = recentDate.year, stopMonth = recentDate.monthValue),
                createTreatment(null, stopYear = recentDate.year, stopMonth = recentDate.monthValue)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should be undetermined when non-recent uncertain lines could push total over maximum`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(null, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue),
                createTreatment(null, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should pass when total included lines are at the maximum`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(Intent.MAINTENANCE, stopYear = recentDate.year, stopMonth = recentDate.monthValue)
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass when non-recent uncertain lines stay within maximum`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(null, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue)
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass when included lines are below the maximum`() {
        val record = withTreatmentHistory(listOf(createTreatment(Intent.PALLIATIVE)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    private fun createTreatment(intent: Intent?, stopYear: Int? = null, stopMonth: Int? = null): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("treatment", isSystemic = true)),
            intents = intent?.let { setOf(it) },
            stopYear = stopYear,
            stopMonth = stopMonth
        )
    }
}
