package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import org.junit.Test

class HasHadOncologicalSurgeryInSpecificBodyLocationTest {

    private val MATCHING_CATEGORY = BodyLocationCategory.LUNG
    private val function = HasHadOncologicalSurgeryInSpecificBodyLocation(setOf(MATCHING_CATEGORY, BodyLocationCategory.KIDNEY))
    private val correctHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
        treatments = setOf(TreatmentTestFactory.treatment("lung surgery", false, setOf(TreatmentCategory.SURGERY), emptySet())),
        bodyLocationCategory = setOf(MATCHING_CATEGORY)
    )

    @Test
    fun `Should pass for surgery with target body location category in oncological history`() {
        val record = TreatmentTestFactory.withTreatmentHistoryEntry(correctHistoryEntry)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass if one of the body location categories in an oncological surgery entry matches the target locations`() {
        val record = TreatmentTestFactory.withTreatmentHistoryEntry(
            correctHistoryEntry.copy(
                treatmentHistoryDetails = TreatmentHistoryDetails(
                    bodyLocationCategories = setOf(
                        MATCHING_CATEGORY,
                        BodyLocationCategory.LIVER
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined for surgery in oncological history without body location category specified`() {
        val record = TreatmentTestFactory.withTreatmentHistoryEntry(correctHistoryEntry.copy(treatmentHistoryDetails = null))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should fail for surgery with wrong body location category in oncological history`() {
        val record = TreatmentTestFactory.withTreatmentHistoryEntry(
            correctHistoryEntry.copy(
                treatmentHistoryDetails = TreatmentHistoryDetails(bodyLocationCategories = setOf(BodyLocationCategory.LIVER))
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail for non-surgery entry with correct body location category`() {
        val treatment = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.treatment("radiotherapy", false, setOf(TreatmentCategory.RADIOTHERAPY), emptySet())),
            bodyLocationCategory = setOf(MATCHING_CATEGORY)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatment)))
    }

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }
}