package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.util.ApplicationConfig
import org.junit.Test

class HasIntoleranceRelatedToStudyMedicationTest {
    @Test
    fun canEvaluate() {
        val function = HasIntoleranceRelatedToStudyMedication()

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(emptyList())))

        // Random allergy
        val wrongCategory: Intolerance = ToxicityTestFactory.intolerance().category("some category").build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(wrongCategory)))

        // Inactive allergy for medication
        val inactive: Intolerance =
            ToxicityTestFactory.intolerance().category(HasIntoleranceRelatedToStudyMedication.MEDICATION_CATEGORY).build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(inactive)))

        // Actual relevant allergy
        val relevantCategory: Intolerance = ToxicityTestFactory.intolerance()
            .category(HasIntoleranceRelatedToStudyMedication.MEDICATION_CATEGORY.uppercase(ApplicationConfig.LOCALE))
            .clinicalStatus(HasIntoleranceRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE.uppercase(ApplicationConfig.LOCALE))
            .build()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withIntolerance(relevantCategory)))

        // Actual relevant allergy due to DOID
        val relevantDoid: Intolerance = ToxicityTestFactory.intolerance()
            .addDoids(DoidConstants.DRUG_ALLERGY_DOID)
            .clinicalStatus(HasIntoleranceRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE)
            .build()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withIntolerance(relevantDoid)))
    }
}