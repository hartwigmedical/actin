package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Intolerance
import org.junit.Test

class HasIntoleranceRelatedToStudyMedicationTest {

    @Test
    fun canEvaluate() {
        val function = HasIntoleranceRelatedToStudyMedication()

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(emptyList())))

        // Random allergy
        val wrongCategory = ToxicityTestFactory.intolerance(category = "some category")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(wrongCategory)))

        // Inactive allergy for medication
        val inactive = ToxicityTestFactory.intolerance(category = HasIntoleranceRelatedToStudyMedication.MEDICATION_CATEGORY)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(inactive)))

        // Actual relevant allergy
        val relevantCategory: Intolerance = ToxicityTestFactory.intolerance(
            category = HasIntoleranceRelatedToStudyMedication.MEDICATION_CATEGORY.uppercase(),
            clinicalStatus = HasIntoleranceRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE.uppercase()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withIntolerance(relevantCategory)))

        // Actual relevant allergy due to DOID
        val relevantDoid: Intolerance = ToxicityTestFactory.intolerance(
            doids = setOf(DoidConstants.DRUG_ALLERGY_DOID),
            clinicalStatus = HasIntoleranceRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withIntolerance(relevantDoid)))
    }
}