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
        assertResultForTreatmentHistory(EvaluationResult.FAIL, emptyList())
    }

    @Test
    fun `Should fail when history contains non-systemic treatments only`() {
        val treatments = listOf("1", "2").map { treatmentEntry(it, StopReason.PROGRESSIVE_DISEASE, systemic = false) }
        assertResultForTreatmentHistory(EvaluationResult.FAIL, treatments)
    }

    @Test
    fun `Should fail when history contains fewer systemic treatments than requested`() {
        assertResultForTreatmentHistory(EvaluationResult.FAIL, listOf(treatmentEntry("1")))
    }

    @Test
    fun `Should fail when history contains only non PD treatments`() {
        val knownDateTreatments = listOf("1", "2").map { treatmentEntry(it, startYear = 2022, stopYear = 2023) }
        val unknownDateTreatment = treatmentEntry("3", startYear = null)
        assertResultForTreatmentHistory(EvaluationResult.FAIL, knownDateTreatments + unknownDateTreatment)
    }

    @Test
    fun `Should be undetermined when requested number of treatments is met only when counting duplicate treatments entries twice`() {
        val treatment = treatmentEntry("1", StopReason.PROGRESSIVE_DISEASE)
        assertResultForTreatmentHistory(EvaluationResult.UNDETERMINED, listOf(treatment, treatment))
    }

    @Test
    fun `Should evaluate to undetermined when had minimum lines of treatment but stop reason toxicity for last line`() {
        val treatments = listOf(
            treatmentEntry("1", StopReason.PROGRESSIVE_DISEASE, stopYear = 2022),
            treatmentEntry("2", StopReason.TOXICITY, startYear = 2022, stopYear = 2024),
        )
        assertResultForTreatmentHistory(EvaluationResult.UNDETERMINED, treatments)
    }

    @Test
    fun `Should be undetermined when had minimum lines of treatment but unknown end date and stop reason for latest line`() {
        val treatments = listOf(
            treatmentEntry("1", StopReason.PROGRESSIVE_DISEASE, stopYear = 2022),
            treatmentEntry("2", startYear = 2022),
        )
        assertResultForTreatmentHistory(EvaluationResult.UNDETERMINED, treatments)
    }

    @Test
    fun `Should be undetermined when had minimum lines of treatment but progressive disease in line with unknown date`() {
        val treatments = listOf(
            treatmentEntry("1", StopReason.PROGRESSIVE_DISEASE, startYear = null),
            treatmentEntry("2", StopReason.TOXICITY, startYear = 2022, stopYear = 2022),
        )
        assertResultForTreatmentHistory(EvaluationResult.UNDETERMINED, treatments)
    }

    @Test
    fun `Should be undetermined when had minimum lines of treatment with some progressive disease but no known dates`() {
        val treatments = listOf(
            treatmentEntry("1", StopReason.PROGRESSIVE_DISEASE, startYear = null),
            treatmentEntry("2", StopReason.TOXICITY, startYear = null),
        )
        assertResultForTreatmentHistory(EvaluationResult.UNDETERMINED, treatments)
    }

    @Test
    fun `Should be undetermined when had minimum treatment lines with PD in last line but multiple lines without date and not all PD`() {
        val unknownDateSomePd = listOf(unknownDateTreatmentWithPD("1"), treatmentEntry("2", StopReason.TOXICITY, startYear = null))
        val knownDateTreatment = treatmentEntry("4", StopReason.PROGRESSIVE_DISEASE, startYear = 2023, stopYear = 2024)

        assertResultForTreatmentHistory(EvaluationResult.UNDETERMINED, unknownDateSomePd + knownDateTreatment)
    }

    @Test
    fun `Should be undetermined when had minimum treatment lines with PD in last line but multiple lines without date or PD`() {
        val unknownDateNonPd = listOf("1", "2").map { treatmentEntry(it, StopReason.TOXICITY, startYear = null) }
        val knownDateTreatment = treatmentEntry("4", StopReason.PROGRESSIVE_DISEASE, startYear = 2023, stopYear = 2024)

        val expectedResult = EvaluationResult.UNDETERMINED
        val treatmentHistory = unknownDateNonPd + knownDateTreatment
        assertResultForTreatmentHistory(expectedResult, treatmentHistory)
    }

    @Test
    fun `Should be undetermined when had minimum treatment lines without PD in last line and some lines without date or PD`() {
        val unknownDateAllPd = listOf("1", "2").map { unknownDateTreatmentWithPD(it) }
        val unknownDateSomePd = listOf(unknownDateTreatmentWithPD("1"), treatmentEntry("2", StopReason.TOXICITY, startYear = null))
        val knownDateNonPd = treatmentEntry("3", StopReason.TOXICITY, startYear = 2022, stopYear = 2022)

        listOf(unknownDateAllPd, unknownDateSomePd).forEach { treatmentList ->
            assertResultForTreatmentHistory(EvaluationResult.UNDETERMINED, treatmentList + knownDateNonPd)
        }
    }

    @Test
    fun `Should fail when had minimum treatment lines with PD before last line and multiple lines without date or PD`() {
        val unknownDateTreatments = listOf("1", "2").map { treatmentEntry(it, null, null) }
        val knownDateTreatments = listOf(
            treatmentEntry("3", StopReason.PROGRESSIVE_DISEASE, startYear = 2022, stopYear = 2022),
            treatmentEntry("4", null, startYear = 2023, stopYear = 2024),
        )
        assertResultForTreatmentHistory(EvaluationResult.FAIL, unknownDateTreatments + knownDateTreatments)
    }

    @Test
    fun `Should pass when had minimum treatment lines with PD in last line and multiple lines without date with PD in all`() {
        val unknownDateTreatments = listOf("1", "2").map { unknownDateTreatmentWithPD(it) }
        val knownDateTreatments = listOf(
            treatmentEntry("3", StopReason.TOXICITY, startYear = 2022, stopYear = 2022),
            treatmentEntry("4", StopReason.PROGRESSIVE_DISEASE, startYear = 2023, stopYear = 2024),
        )
        assertResultForTreatmentHistory(EvaluationResult.PASS, unknownDateTreatments + knownDateTreatments)
    }

    @Test
    fun `Should pass when had minimum lines of treatment and all with PD but one with unknown date`() {
        val treatments = listOf(
            treatmentEntry("1", StopReason.PROGRESSIVE_DISEASE, startYear = null),
            treatmentEntry("2", StopReason.PROGRESSIVE_DISEASE, startYear = 2022, stopYear = 2022),
        )
        assertResultForTreatmentHistory(EvaluationResult.PASS, treatments)
    }

    @Test
    fun `Should pass when had minimum lines of treatment, all with unknown dates and all with stop reason PD`() {
        val treatments = listOf("1,", "2").map { name ->
            treatmentEntry(name, StopReason.PROGRESSIVE_DISEASE, startYear = null)
        }
        assertResultForTreatmentHistory(EvaluationResult.PASS, treatments)
    }

    @Test
    fun `Should pass when had minimum lines of treatment with PD`() {
        val treatments = listOf("1", "2").map { treatmentEntry(it, StopReason.PROGRESSIVE_DISEASE) }
        assertResultForTreatmentHistory(EvaluationResult.PASS, treatments)
    }

    private fun assertResultForTreatmentHistory(expectedResult: EvaluationResult, treatmentHistory: List<TreatmentHistoryEntry>) {
        listOf(false, true).forEach { mustBeRadiological ->
            val function = HasHadPDFollowingSomeSystemicTreatments(2, mustBeRadiological)
            assertEvaluation(expectedResult, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
        }
    }

    private fun treatmentEntry(
        name: String, stopReason: StopReason? = null, startYear: Int? = 2020, systemic: Boolean = true, stopYear: Int? = null
    ): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(
            treatments = listOf(TreatmentTestFactory.treatment(name, systemic)),
            startYear = startYear,
            stopReason = stopReason,
            stopYear = stopYear
        )
    }

    private fun unknownDateTreatmentWithPD(name: String): TreatmentHistoryEntry {
        return treatmentEntry(name, StopReason.PROGRESSIVE_DISEASE, null)
    }
}