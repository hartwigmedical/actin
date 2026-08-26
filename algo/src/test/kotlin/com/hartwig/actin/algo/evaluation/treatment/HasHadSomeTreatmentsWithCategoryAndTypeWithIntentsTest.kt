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
import org.assertj.core.api.Assertions.assertThat
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
        val evaluation = function.evaluate(withTreatmentHistory(emptyList()))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly(
            "Has not received palliative ALK inhibitor or EGFR inhibitor targeted therapy"
        )
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), intents = matchingIntents)
        val evaluation = function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly(
            "Has not received palliative ALK inhibitor or EGFR inhibitor targeted therapy"
        )
    }

    @Test
    fun `Should fail for wrong treatment type`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(drugTreatment("test", matchingCategory, setOf(DrugType.ROS1_INHIBITOR))), intents = matchingIntents)
        val evaluation = function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry)))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly(
            "Has not received palliative ALK inhibitor or EGFR inhibitor targeted therapy"
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
        val evaluation = function.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly(
            "Has not received palliative ALK inhibitor or EGFR inhibitor targeted therapy"
        )
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
        val evaluation = function.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessagesStrings()).containsExactly(
            "Has received palliative ALK inhibitor and EGFR inhibitor targeted therapy (Matching category with correct intent and type)"
        )
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
        val evaluation = HasHadSomeTreatmentsWithCategoryAndTypeWithIntents(matchingCategory, matchingIntents, allowedTypes = null)
            .evaluate(patientRecord)
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessagesStrings()).containsExactly("Has received palliative targeted therapy (Matching)")
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
        val evaluation = function.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly(
            "Undetermined if received ALK inhibitor or EGFR inhibitor targeted therapy is palliative"
        )
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
        val evaluation = function.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly(
            "Undetermined if treatment received in previous trial included palliative ALK inhibitor or EGFR inhibitor targeted therapy"
        )
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
        val evaluation = function.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly(
            "Has not received palliative ALK inhibitor or EGFR inhibitor targeted therapy"
        )
    }

    @Test
    fun `Should be undetermined when stop date is missing`() {
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
        val evaluation = functionWithDate.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly(
            "Has received palliative targeted therapy (Matching category and intent) but unknown if since 2022-04-01"
        )
    }

    @Test
    fun `Should be undetermined when all dates are missing`() {
        val treatment = drugTreatment("matching but unknown date", category = matchingCategory, types = matchingTypes)
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(treatment),
            intents = matchingIntents
        )
        val patientRecord = withTreatmentHistory(listOf(treatmentHistoryEntry))
        val evaluation = functionWithDate.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly(
            "Has received palliative ALK inhibitor and EGFR inhibitor targeted therapy (${treatment.display()}) but unknown if since 2022-04-01"
        )
    }

    @Test
    fun `Should fail when stop date is known and is too old`() {
        val treatment = treatment("matching category and intent", isSystemic = true, categories = setOf(matchingCategory))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = matchingIntents,
                    startYear = minDate.year - 2,
                    stopYear = minDate.year - 1,
                )
            )
        )
        val evaluation = functionWithDate.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly(
            "Has not received palliative ALK inhibitor or EGFR inhibitor targeted therapy"
        )
    }

    @Test
    fun `Should pass when treatment dates are recent enough`() {
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
        val evaluation = functionWithDate.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessagesStrings()).containsExactly(
            "Has received palliative ALK inhibitor and EGFR inhibitor targeted therapy (Recent matching treatment)"
        )
    }
}