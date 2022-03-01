package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.ImmutableAllergy;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasAllergyRelatedToStudyMedicationTest {

    @Test
    public void canEvaluate() {
        HasAllergyRelatedToStudyMedication function = new HasAllergyRelatedToStudyMedication();

        List<Allergy> allergies = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withAllergies(allergies)));

        allergies.add(builder().category("some category").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withAllergies(allergies)));

        allergies.add(builder().category(HasAllergyRelatedToStudyMedication.MEDICATION_CATEGORY).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withAllergies(allergies)));

        allergies.add(builder().category(HasAllergyRelatedToStudyMedication.MEDICATION_CATEGORY.toUpperCase())
                .clinicalStatus(HasAllergyRelatedToStudyMedication.CLINICAL_STATUS_ACTIVE.toUpperCase())
                .build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withAllergies(allergies)));
    }

    @NotNull
    private static PatientRecord withAllergies(@NotNull List<Allergy> allergies) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .allergies(allergies)
                        .build())
                .build();
    }

    @NotNull
    private static ImmutableAllergy.Builder builder() {
        return ImmutableAllergy.builder()
                .name(Strings.EMPTY)
                .category(Strings.EMPTY)
                .clinicalStatus(Strings.EMPTY)
                .verificationStatus(Strings.EMPTY)
                .criticality(Strings.EMPTY);
    }
}