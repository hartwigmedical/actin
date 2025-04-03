package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.Test
import java.time.LocalDate

class HasHadSystemicTreatmentInAdvancedOrMetastaticSettingTest {

    private val referenceDate = LocalDate.of(2024, 11, 26)
    private val recentDate = referenceDate.minusMonths(1)
    private val nonRecentDate = recentDate.minusMonths(7)
    private val function = HasHadSystemicTreatmentInAdvancedOrMetastaticSetting(referenceDate)
    private val nonRecentTreatment = createTreatment(
        intent = null, isSystemic = true, "Treatment a", stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue
    )

    @Test
    fun `Should fail if patient has only had systemic treatments with curative intent`() {
        val record = withTreatmentHistory(listOf(createTreatment(Intent.CURATIVE, isSystemic = true, "Treatment a")))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should pass if patient has had systemic treatment with palliative intent`() {
        val patientRecord =
            withTreatmentHistory(listOf(Intent.PALLIATIVE, Intent.CURATIVE).map { createTreatment(it, isSystemic = true) })
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patientRecord))
    }

    @Test
    fun `Should pass if patient has had systemic treatment within 6 months with intent other than curative`() {
        val record = withTreatmentHistory(
            listOf(null, Intent.ADJUVANT, Intent.CURATIVE)
                .map { createTreatment(it, isSystemic = true, "Treatment", stopYear = recentDate.year, stopMonth = recentDate.monthValue) }
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass if patient has had more than one systemic lines with unknown or non-curative intent`() {
        val noCurative = listOf(null, Intent.ADJUVANT, Intent.INDUCTION).map { nonRecentTreatment.copy(intents = it?.let { setOf(it) }) }
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(noCurative)))
    }

    @Test
    fun `Should pass if patient has had one systemic line with non-curative intent and not followed by radiotherapy or surgery`() {
        val record = withTreatmentHistory(listOf(createTreatment(null, isSystemic = true, stopYear = null, stopMonth = null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined if patient has had prior systemic treatment more than 6 months ago with intent other than curative followed by radiotherapy`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.ADJUVANT, isSystemic = true, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue),
                createTreatment(
                    Intent.CURATIVE,
                    isSystemic = true,
                    stopYear = nonRecentDate.year,
                    stopMonth = nonRecentDate.monthValue + 1,
                    categories = setOf(TreatmentCategory.RADIOTHERAPY)
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined if patient has had systemic treatment with unknown stop date and intent other than curative followed by surgery with unknown date`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.NEOADJUVANT, isSystemic = true, stopYear = null, stopMonth = null),
                createTreatment(
                    Intent.CURATIVE,
                    isSystemic = true,
                    stopYear = null,
                    stopMonth = null,
                    categories = setOf(TreatmentCategory.SURGERY)
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should fail for non-systemic treatment`() {
        val patientRecord = withTreatmentHistory(listOf(createTreatment(Intent.PALLIATIVE, isSystemic = false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
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