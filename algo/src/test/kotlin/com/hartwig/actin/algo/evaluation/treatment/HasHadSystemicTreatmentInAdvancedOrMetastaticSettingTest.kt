package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
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
        intent = null, systemic = true, "Treatment a", stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue
    )

    @Test
    fun `Should fail if patient has only had systemic treatments with curative intent`() {
        val record = withTreatmentHistory(listOf(createTreatment(Intent.CURATIVE, systemic = true, "Treatment a")))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should pass if patient has had systemic treatment with palliative intent`() {
        val patientRecord =
            withTreatmentHistory(listOf(Intent.PALLIATIVE, Intent.CURATIVE).map { createTreatment(it, systemic = true) })
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patientRecord))
    }

    @Test
    fun `Should pass if patient has had systemic treatment within 6 months with intent other than curative`() {
        val record = withTreatmentHistory(listOf(null, Intent.ADJUVANT, Intent.CURATIVE)
            .map { createTreatment(it, systemic = true, "Treatment", stopYear = recentDate.year, stopMonth = recentDate.monthValue) }
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass if patient has had more than two systemic lines with unknown or non-curative intent`() {
        val noCurative = listOf(null, Intent.ADJUVANT, Intent.INDUCTION).map { nonRecentTreatment.copy(intents = it?.let { setOf(it) }) }
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(noCurative)))
    }

    @Test
    fun `Should return undetermined if patient has had 3 prior systemic lines but 1 with curative intent`() {
        val oneCurative = listOf(null, Intent.CURATIVE, Intent.INDUCTION).map { nonRecentTreatment.copy(intents = it?.let { setOf(it) }) }
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(oneCurative)))
    }

    @Test
    fun `Should evaluate to undetermined if patient has had systemic treatment longer than 6 months ago with intent other than curative`() {
        listOf(Intent.ADJUVANT, null)
            .map { createTreatment(it, systemic = true, "Treatment", stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue) }
            .forEach {
                assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(listOf(it))))
            }
    }

    @Test
    fun `Should evaluate to undetermined if patient has had systemic treatment with unknown stop date and intent other than curative`() {
        val record = withTreatmentHistory(
            listOf(createTreatment(Intent.ADJUVANT, systemic = true, stopYear = null, stopMonth = null))
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should fail for non-systemic treatment`() {
        val patientRecord = withTreatmentHistory(listOf(createTreatment(Intent.PALLIATIVE, systemic = false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    private fun createTreatment(
        intent: Intent?,
        systemic: Boolean,
        name: String = "treatment name",
        stopYear: Int? = recentDate.year,
        stopMonth: Int? = recentDate.monthValue
    ): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment(name, isSystemic = systemic)),
            intents = intent?.let { setOf(it) },
            stopMonth = stopMonth,
            stopYear = stopYear
        )
    }
}