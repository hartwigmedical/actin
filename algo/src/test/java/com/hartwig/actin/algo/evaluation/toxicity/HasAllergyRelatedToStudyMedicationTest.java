package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Intolerance;

import org.junit.Test;

public class HasAllergyRelatedToStudyMedicationTest {

    @Test
    public void canEvaluate() {
        HasAllergyRelatedToStudyMedication function = new HasAllergyRelatedToStudyMedication();

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(Lists.newArrayList())));

        // Random allergy
        Intolerance wrongCategory = ToxicityTestFactory.intolerance().category("some category").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(wrongCategory)));

        // Inactive allergy for medication
        Intolerance inactive = ToxicityTestFactory.intolerance().category(HasAllergyRelatedToStudyMedication.MEDICATION_CATEGORY).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(inactive)));

        // Actual relevant allergy
        Intolerance relevantCategory = ToxicityTestFactory.intolerance()
                .category(HasAllergyRelatedToStudyMedication.MEDICATION_CATEGORY.toUpperCase())
                .clinicalStatus(HasAllergyRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE.toUpperCase())
                .build();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withIntolerance(relevantCategory)));

        // Actual relevant allergy due to DOID
        Intolerance relevantDoid = ToxicityTestFactory.intolerance()
                .addDoids(HasAllergyRelatedToStudyMedication.DRUG_ALLERGY_DOID)
                .clinicalStatus(HasAllergyRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE)
                .build();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withIntolerance(relevantDoid)));
    }
}