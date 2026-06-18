package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingAndLimitedLinesTest {

    private val referenceDate = LocalDate.of(2024, 11, 26)
    private val recentDate = referenceDate.minusMonths(1)
    private val nonRecentDate = recentDate.minusMonths(7)
    private val functionWithoutMaxLines = HasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingAndLimitedLines(
        referenceDate,
        intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
        settingDescription = "metastatic",
        maximumLines = null
    )
    private val functionWithMaxLines = HasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingAndLimitedLines(
        referenceDate,
        intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
        settingDescription = "metastatic",
        maximumLines = 2
    )
    private val nonRecentTreatment = createTreatment(
        intent = null, isSystemic = true, "Treatment a", stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue
    )

    @Test
    fun `Should fail if patient has only had systemic treatments with curative intent`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutMaxLines.evaluate(withTreatmentHistory(listOf(createTreatment(Intent.CURATIVE, isSystemic = true, "Treatment a"))))
        )
    }

    @Test
    fun `Should fail for non-systemic treatment`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutMaxLines.evaluate(withTreatmentHistory(listOf(createTreatment(Intent.PALLIATIVE, isSystemic = false))))
        )
    }

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, functionWithoutMaxLines.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should pass if patient has had systemic treatment with palliative intent`() {
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutMaxLines.evaluate(
                withTreatmentHistory(listOf(Intent.PALLIATIVE, Intent.CURATIVE).map { createTreatment(it, isSystemic = true) })
            )
        )
    }

    @Test
    fun `Should pass if patient has had systemic treatment within 6 months with unknown intent`() {
        val record = withTreatmentHistory(
            listOf(createTreatment(null, isSystemic = true, "Treatment", stopYear = recentDate.year, stopMonth = recentDate.monthValue))
        )
        assertEvaluation(EvaluationResult.PASS, functionWithoutMaxLines.evaluate(record))
    }

    @Test
    fun `Should pass if patient has had systemic treatment within 6 months with intent other than curative, adjuvant or neoadjuvant`() {
        val record = withTreatmentHistory(
            listOf(Intent.INDUCTION, Intent.CURATIVE)
                .map { createTreatment(it, isSystemic = true, "Treatment", stopYear = recentDate.year, stopMonth = recentDate.monthValue) }
        )
        assertEvaluation(EvaluationResult.PASS, functionWithoutMaxLines.evaluate(record))
    }

    @Test
    fun `Should pass if patient has had more than one systemic lines with unknown or non curative or (neo)adjuvant intent`() {
        val record = withTreatmentHistory(
            listOf(null, Intent.MAINTENANCE, Intent.INDUCTION)
                .map { nonRecentTreatment.copy(intents = it?.let { setOf(it) }) }
        )
        assertEvaluation(EvaluationResult.PASS, functionWithoutMaxLines.evaluate(record))
    }

    @Test
    fun `Should pass if patient has had one systemic line with non curative or (neo)adjuvant intent and not followed by radiotherapy or surgery`() {
        val record = withTreatmentHistory(listOf(createTreatment(null, isSystemic = true, stopYear = null, stopMonth = null)))
        assertEvaluation(EvaluationResult.PASS, functionWithoutMaxLines.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined if patient has had prior systemic treatment more than 6 months ago with intent other than curative or (neo)adjuvant followed by radiotherapy`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.MAINTENANCE, isSystemic = true, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue),
                createTreatment(
                    Intent.CURATIVE,
                    isSystemic = false,
                    stopYear = nonRecentDate.year,
                    stopMonth = nonRecentDate.monthValue + 1,
                    categories = setOf(TreatmentCategory.RADIOTHERAPY)
                )
            )
        )
        val evaluation = functionWithoutMaxLines.evaluate(record)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings())
            .containsExactly("Has had prior systemic treatment >6 months ago but undetermined if in metastatic setting (Treatment name)")
    }

    @Test
    fun `Should evaluate to undetermined if patient has had surgery before systemic treatment with intent other than curative or (neo)adjuvant more than 6 months ago`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(
                    Intent.CURATIVE,
                    isSystemic = false,
                    stopYear = nonRecentDate.year,
                    stopMonth = nonRecentDate.monthValue,
                    categories = setOf(TreatmentCategory.SURGERY)
                ),
                createTreatment(
                    Intent.MAINTENANCE,
                    isSystemic = true,
                    stopYear = nonRecentDate.year,
                    stopMonth = nonRecentDate.monthValue + 1
                ),
            )
        )
        val evaluation = functionWithoutMaxLines.evaluate(record)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings())
            .containsExactly("Has had prior systemic treatment >6 months ago but undetermined if in metastatic setting (Treatment name)")
    }

    @Test
    fun `Should evaluate to undetermined if patient has had systemic treatment with unknown date and intent other than curative or (neo)adjuvant and surgery with unknown date`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.MAINTENANCE, isSystemic = true, stopYear = null, stopMonth = null),
                createTreatment(
                    Intent.CURATIVE,
                    isSystemic = false,
                    stopYear = null,
                    stopMonth = null,
                    categories = setOf(TreatmentCategory.SURGERY)
                )
            )
        )
        val evaluation = functionWithoutMaxLines.evaluate(record)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings())
            .containsExactly("Has had prior systemic treatment but undetermined if in metastatic setting (Treatment name)")
    }

    @Test
    fun `Should pass with no prior systemic treatment when maximum lines is set`() {
        assertEvaluation(EvaluationResult.PASS, functionWithMaxLines.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if palliative lines alone exceed maximum`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.PALLIATIVE, Intent.PALLIATIVE).map { nonRecentTreatment.copy(intents = setOf(it)) }
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithMaxLines.evaluate(record))
    }

    @Test
    fun `Should fail when total lines exceed maximum plus one uncertain buffer`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.MAINTENANCE, Intent.INDUCTION, null).map { nonRecentTreatment.copy(intents = it?.let { setOf(it) }) }
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithMaxLines.evaluate(record))
    }

    @Test
    fun `Should be undetermined when uncertain lines could push total over maximum`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.MAINTENANCE, null).map { nonRecentTreatment.copy(intents = it?.let { setOf(it) }) }
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithMaxLines.evaluate(record))
    }

    @Test
    fun `Should pass when palliative lines plus uncertain lines are at the maximum`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.MAINTENANCE)
                .map { nonRecentTreatment.copy(intents = setOf(it)) }
        )
        assertEvaluation(EvaluationResult.PASS, functionWithMaxLines.evaluate(record))
    }

    @Test
    fun `Should pass when palliative lines are at the maximum with no uncertain lines`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.PALLIATIVE)
                .map { nonRecentTreatment.copy(intents = setOf(it)) }
        )
        assertEvaluation(EvaluationResult.PASS, functionWithMaxLines.evaluate(record))
    }

    @Test
    fun `Should pass when palliative lines are below the maximum with no uncertain lines`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE)
                .map { nonRecentTreatment.copy(intents = setOf(it)) }
        )
        assertEvaluation(EvaluationResult.PASS, functionWithMaxLines.evaluate(record))
    }

    private fun createTreatment(
        intent: Intent?,
        isSystemic: Boolean,
        name: String = "treatment name",
        stopYear: Int? = recentDate.year,
        stopMonth: Int? = recentDate.monthValue,
        categories: Set<TreatmentCategory> = emptySet()
    ): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment(name, isSystemic, categories)),
            intents = intent?.let { setOf(it) },
            stopMonth = stopMonth,
            stopYear = stopYear
        )
    }
}