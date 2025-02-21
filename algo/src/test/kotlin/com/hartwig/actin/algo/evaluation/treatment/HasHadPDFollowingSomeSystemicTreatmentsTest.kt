package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.Test

class HasHadPDFollowingSomeSystemicTreatmentsTest {

    @Test
    fun `Should fail when treatment history is empty`() {
        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.FAIL, it.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
        }
    }

    @Test
    fun `Should fail when history contains non-systemic treatments only`() {
        val treatments = listOf("1", "2").map { treatmentEntry(it, false, StopReason.PROGRESSIVE_DISEASE) }

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.FAIL, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should fail when history contains less systemic treatments than requested`() {
        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.FAIL,
                it.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentEntry("1", true))))
            )
        }
    }

    @Test
    fun `Should fail when history contains less only non PD treatments`() {
        val knownDateTreatments = listOf("1", "2").map { treatmentEntry(it, true, startYear = 2022, stopYear = 2023) }
        val unknownDateTreatment = treatmentEntry("3", true, startYear = null)

        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.FAIL,
                it.evaluate(TreatmentTestFactory.withTreatmentHistory(knownDateTreatments + unknownDateTreatment))
            )
        }
    }

    @Test
    fun `Should evaluate to undetermined when requested number of treatments is met only when counting duplicate treatments entries twice`() {
        val treatment = treatmentEntry("1", true, StopReason.PROGRESSIVE_DISEASE)

        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                it.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatment, treatment)))
            )
        }
    }

    @Test
    fun `Should evaluate to undetermined when history contains requested number of treatment lines but stop reason toxicity for last line`() {
        val treatments = listOf(
            treatmentEntry("1", true, StopReason.PROGRESSIVE_DISEASE, stopYear = 2022, startYear = 2020),
            treatmentEntry("2", true, StopReason.TOXICITY, stopYear = 2024, startYear = 2022),
        )

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.UNDETERMINED, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should evaluate to undetermined when history contains requested number of treatment lines but unknown end date and stop reason for latest line`() {
        val treatments = listOf(
            treatmentEntry("1", true, StopReason.PROGRESSIVE_DISEASE, stopYear = 2022, startYear = 2020),
            treatmentEntry("2", true, null, stopYear = null, startYear = 2022),
        )

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.UNDETERMINED, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should evaluate to undetermined when history contains requested number of treatment lines but progressive disease in line with unknown date`() {
        val treatments = listOf(
            treatmentEntry("1", true, StopReason.PROGRESSIVE_DISEASE, stopYear = null, startYear = null),
            treatmentEntry("2", true, StopReason.TOXICITY, stopYear = 2022, startYear = 2022),
        )

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.UNDETERMINED, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should evaluate to undetermined when minimal treatment lines is met with PD in last line but multiple lines without date and not all PD`() {
        val unknownDateSomePd = listOf(unknownDateTreatmentWithPD("1"), treatmentEntry("2", true, StopReason.TOXICITY, startYear = null))
        val unknownDateNonPd = listOf("1", "2").map { treatmentEntry(it, true, StopReason.TOXICITY, startYear = null) }
        val knownDateTreatment = treatmentEntry("4", true, StopReason.PROGRESSIVE_DISEASE, stopYear = 2024, startYear = 2023)

        listOf(unknownDateSomePd, unknownDateNonPd).forEach { treatmentList ->
            FUNCTIONS.forEach {
                assertEvaluation(
                    EvaluationResult.UNDETERMINED,
                    it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentList + knownDateTreatment))
                )
            }
        }
    }

    @Test
    fun `Should evaluate to undetermined when minimal treatment lines is met without PD in last line and multiple lines without date with PD in some or all`() {
        val unknownDateAllPd = listOf("1", "2").map { unknownDateTreatmentWithPD(it) }
        val unknownDateSomePd = listOf(unknownDateTreatmentWithPD("1"), treatmentEntry("2", true, StopReason.TOXICITY, startYear = null))
        val knownDateNonPd = treatmentEntry("3", true, StopReason.TOXICITY, stopYear = 2022, startYear = 2022)

        listOf(unknownDateAllPd, unknownDateSomePd).forEach { treatmentList ->
            FUNCTIONS.forEach {
                assertEvaluation(
                    EvaluationResult.UNDETERMINED,
                    it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentList + knownDateNonPd))
                )
            }
        }
    }

    @Test
    fun `Should pass when minimal treatment lines is met with PD in last line and multiple lines without date with PD in all`() {
        val unknownDateTreatments = listOf("1", "2").map { unknownDateTreatmentWithPD(it) }
        val knownDateTreatments = listOf(
            treatmentEntry("3", true, StopReason.TOXICITY, stopYear = 2022, startYear = 2022),
            treatmentEntry("4", true, StopReason.PROGRESSIVE_DISEASE, stopYear = 2024, startYear = 2023),
        )

        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.PASS,
                it.evaluate(TreatmentTestFactory.withTreatmentHistory(unknownDateTreatments + knownDateTreatments))
            )
        }
    }

    @Test
    fun `Should pass when history contains requested number of treatment lines and all with PD but one with unknown date`() {
        val treatments = listOf(
            treatmentEntry("1", true, StopReason.PROGRESSIVE_DISEASE, stopYear = null, startYear = null),
            treatmentEntry("2", true, StopReason.PROGRESSIVE_DISEASE, stopYear = 2022, startYear = 2022),
        )

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.PASS, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should pass when history contains requested number of treatment lines, all with unknown dates and all with stop reason PD`() {
        val treatments =
            listOf("1,", "2").map { treatmentEntry(it, true, StopReason.PROGRESSIVE_DISEASE, stopYear = null, startYear = null) }

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.PASS, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should pass when history contains requested number of treatment lines with PD`() {
        val treatments = listOf("1", "2").map { treatmentEntry(it, true, StopReason.PROGRESSIVE_DISEASE) }
        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.PASS, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    private fun treatmentEntry(
        name: String, systemic: Boolean, stopReason: StopReason? = null, stopYear: Int? = null, startYear: Int? = 2020
    ): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(
            treatments = listOf(TreatmentTestFactory.treatment(name, systemic)),
            startYear = startYear,
            stopReason = stopReason,
            stopYear = stopYear
        )
    }

    private fun unknownDateTreatmentWithPD(name: String): TreatmentHistoryEntry {
        return treatmentEntry(name, true, StopReason.PROGRESSIVE_DISEASE, null, null)
    }

    companion object {
        private val FUNCTION = HasHadPDFollowingSomeSystemicTreatments(2, false)
        private val RADIOLOGICAL_FUNCTION = HasHadPDFollowingSomeSystemicTreatments(2, true)
        private val FUNCTIONS = listOf(FUNCTION, RADIOLOGICAL_FUNCTION)
    }
}