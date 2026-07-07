package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate
import org.junit.jupiter.api.Test

class HasHadSomeTreatmentsWithCategoryAndTypeWithIntentsTest {

    private val matchingCategory = TreatmentCategory.TARGETED_THERAPY
    private val matchingTypes = setOf(DrugType.ALK_INHIBITOR, DrugType.EGFR_INHIBITOR)
    private val matchingIntents = setOf(Intent.PALLIATIVE)
    private val minDate = LocalDate.of(2022, 4, 1)
    private val function = HasHadSomeTreatmentsWithCategoryAndTypeWithIntents(matchingCategory, matchingIntents, matchingTypes)
    private val functionWithDate =
        HasHadSomeTreatmentsWithCategoryAndTypeWithIntents(matchingCategory, matchingIntents, matchingTypes, minDate)

    @Test
    fun `Should fail for no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), intents = matchingIntents)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should fail for wrong treatment type`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(drugTreatment("test", matchingCategory, setOf(DrugType.ROS1_INHIBITOR))), intents = matchingIntents)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should fail for correct treatment category with wrong intent`() {
        val treatment = drugTreatment("matching category with wrong intent", category = matchingCategory, types = matchingTypes)
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
    fun `Should pass when treatments with correct category, type and intent`() {
        val treatment = drugTreatment("matching category with correct intent and type", category = matchingCategory, types = matchingTypes)
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = matchingIntents
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patientRecord))
    }

    @Test
    fun `Should pass when treatments with correct category and intent if type not asked`() {
        val treatment =
            drugTreatment("matching", category = matchingCategory, types = setOf(DrugType.ROS1_INHIBITOR))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = matchingIntents
                )
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            HasHadSomeTreatmentsWithCategoryAndTypeWithIntents(matchingCategory, matchingIntents, allowedTypes = null).evaluate(
                patientRecord
            )
        )
    }

    @Test
    fun `Should return undetermined when treatments with correct category and type but no intent`() {
        val treatment = drugTreatment("no intent treatment", category = matchingCategory, types = matchingTypes)
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment), intents = null
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should return undetermined when trial treatments`() {
        val treatment = treatment("trial", isSystemic = true, categories = emptySet())
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment), isTrial = true
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
        assertEvaluation(EvaluationResult.FAIL, functionWithDate.evaluate(patientRecord))
    }

    @Test
    fun `Should pass when date is new enough`() {
        val treatment = drugTreatment("recent matching treatment", category = matchingCategory, types = matchingTypes)
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = matchingIntents,
                    startYear = minDate.year + 1
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, functionWithDate.evaluate(patientRecord))
    }

    @Test
    fun `Should return undetermined when treatments with correct category and intent but unknown date`() {
        val treatment = drugTreatment("matching but unknown date", category = matchingCategory, types = matchingTypes)
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = matchingIntents
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithDate.evaluate(patientRecord))
    }
}