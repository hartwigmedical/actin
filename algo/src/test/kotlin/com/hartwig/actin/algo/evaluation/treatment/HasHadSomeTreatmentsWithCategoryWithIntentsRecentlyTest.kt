package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import org.junit.Test
import java.time.LocalDate

class HasHadSomeTreatmentsWithCategoryWithIntentsRecentlyTest {
    private val matchingCategory = TreatmentCategory.TARGETED_THERAPY
    private val matchingIntents = setOf(Intent.PALLIATIVE)
    private val minDate = LocalDate.of(2022, 4, 1)
    private val function = HasHadSomeTreatmentsWithCategoryWithIntentsRecently(matchingCategory, matchingIntents, minDate)

    @Test
    fun `Should fail for no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should fail for correct treatment category with wrong intent`() {
        val treatment = treatment("matching category with wrong intent", isSystemic = true, categories = setOf(matchingCategory))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = setOf(Intent.CONSOLIDATION)
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should pass when treatments with correct category and intent`() {
        val treatment = treatment("matching category and intent", isSystemic = true, categories = setOf(matchingCategory))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = matchingIntents,
                    startYear = minDate.year + 1
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patientRecord))
    }

    @Test
    fun `Should return undetermined when treatments with correct category and no intent`() {
        val treatment = treatment("matching category", isSystemic = true, categories = setOf(matchingCategory))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment), intents = null,
                    startYear = minDate.year + 1
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should return undetermined when trial treatments`() {
        val treatment = treatment("trial", isSystemic = true, categories = setOf(matchingCategory))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment), isTrial = true,
                    startYear = minDate.year + 1
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val treatment = treatment("trial", isSystemic = true, categories = setOf(TreatmentCategory.TRANSPLANTATION))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment)
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should fail when date is too old`() {
        val treatment = treatment("matching category and intent", isSystemic = true, categories = setOf(matchingCategory))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = matchingIntents,
                    startYear = minDate.year - 1
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should pass when date is new enough`() {
        val treatment = treatment("matching category and intent", isSystemic = true, categories = setOf(matchingCategory))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = matchingIntents,
                    startYear = minDate.year + 1
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patientRecord))
    }
}
