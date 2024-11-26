package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class HasHadSystemicTreatmentInAdvancedOrMetastaticSettingTest {

    private val referenceDate = LocalDate.of(2024, 11, 26)
    private val recentDate = referenceDate.minusMonths(1)
    private val nonRecentDate = recentDate.minusMonths(7)
    private val function = HasHadSystemicTreatmentInAdvancedOrMetastaticSetting(referenceDate)

    @Test
    fun `Should fail if patient has only had systemic treatments with curative intent`() {
        val record = withTreatmentHistory(listOf(createTreatment(Intent.CURATIVE, systemic = true, "Treatment a")))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should pass if patient has had more than two systemic lines with unknown or non-curative intent`() {
        val treatment = createTreatment(
            intent = null,
            systemic = true,
            "Treatment a",
            stopYear = nonRecentDate.year,
            stopMonth = nonRecentDate.monthValue
        )

        val noCurative =
            listOf(treatment, treatment.copy(intents = setOf(Intent.INDUCTION)), treatment.copy(intents = setOf(Intent.ADJUVANT)))
        val oneCurative =
            listOf(treatment, treatment.copy(intents = setOf(Intent.CURATIVE)), treatment.copy(intents = setOf(Intent.ADJUVANT)))

        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(noCurative)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(oneCurative)))
    }

    @Test
    fun `Should pass if patient has had systemic treatment with palliative intent`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE, systemic = true, "Treatment a"),
                createTreatment(Intent.PALLIATIVE, systemic = true, "Treatment b")
            )
        )
        val evaluation = function.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passSpecificMessages)
            .containsExactly("Patient has had prior systemic treatment in advanced or metastatic setting (Treatment a and Treatment b)")
    }

    @Test
    fun `Should pass if patient has had systemic treatment within 6 months with intent other than curative`() {
        listOf(
            createTreatment(Intent.ADJUVANT, systemic = true, "Treatment a", stopYear = recentDate.year, stopMonth = recentDate.monthValue),
            createTreatment(intent = null, systemic = true, "Treatment b", stopYear = recentDate.year, stopMonth = recentDate.monthValue)
        ).forEach {
            assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(listOf(it))))
        }
    }

    @Test
    fun `Should evaluate to undetermined if patient has had systemic treatment longer than 6 months ago with intent other than curative`() {
        listOf(
            createTreatment(Intent.ADJUVANT, systemic = true, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue),
            createTreatment(intent = null, systemic = true, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue)
        ).forEach {
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