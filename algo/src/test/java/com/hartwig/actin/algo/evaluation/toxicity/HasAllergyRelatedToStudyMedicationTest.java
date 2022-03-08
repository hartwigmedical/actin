package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Allergy;

import org.junit.Test;

public class HasAllergyRelatedToStudyMedicationTest {

    @Test
    public void canEvaluate() {
        HasAllergyRelatedToStudyMedication function = new HasAllergyRelatedToStudyMedication();

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withAllergies(Lists.newArrayList())));

        // Random allergy
        Allergy wrongCategory = ToxicityTestFactory.allergy().category("some category").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withAllergy(wrongCategory)));

        // Inactive allergy for medication
        Allergy inactive = ToxicityTestFactory.allergy().category(HasAllergyRelatedToStudyMedication.MEDICATION_CATEGORY).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withAllergy(inactive)));

        // Actual relevant allergy
        Allergy relevantCategory = ToxicityTestFactory.allergy()
                .category(HasAllergyRelatedToStudyMedication.MEDICATION_CATEGORY.toUpperCase())
                .clinicalStatus(HasAllergyRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE.toUpperCase())
                .build();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withAllergy(relevantCategory)));

        // Actual relevant allergy due to DOID
        Allergy relevantDoid = ToxicityTestFactory.allergy()
                .addDoids(HasAllergyRelatedToStudyMedication.DRUG_ALLERGY_DOID)
                .clinicalStatus(HasAllergyRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE)
                .build();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withAllergy(relevantDoid)));
    }
}